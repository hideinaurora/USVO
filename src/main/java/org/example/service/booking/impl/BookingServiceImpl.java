package org.example.service.booking.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.common.PageResult;
import org.example.config.exception.CommonJsonException;
import org.example.dto.OpResultDTO;
import org.example.dto.booking.BookingCancelDTO;
import org.example.dto.booking.BookingCheckinDTO;
import org.example.dto.booking.BookingCreateDTO;
import org.example.dto.booking.PaymentPayDTO;
import org.example.entity.CourtEntity;
import org.example.entity.VenueEntity;
import org.example.entity.basic.user.UserEntity;
import org.example.entity.booking.BookingEntity;
import org.example.entity.booking.CheckinLogEntity;
import org.example.entity.booking.PaymentEntity;
import org.example.entity.booking.TimeSlotEntity;
import org.example.mapper.booking.BookingMapper;
import org.example.mapper.booking.CheckinLogMapper;
import org.example.mapper.booking.PaymentMapper;
import org.example.mapper.booking.TimeSlotMapper;
import org.example.mapper.venue.CourtMapper;
import org.example.mapper.venue.VenueMapper;
import org.example.service.basic.user.UserService;
import org.example.service.booking.BookingService;
import org.example.vo.booking.AvailableSlotVO;
import org.example.vo.booking.BookingCreateResultVO;
import org.example.vo.booking.BookingDetailVO;
import org.example.vo.booking.BookingListItemVO;
import org.example.vo.booking.BookingPaymentVO;
import org.example.vo.booking.CourtSlotsVO;
import org.example.vo.booking.PaymentPayResultVO;
import org.example.vo.booking.SlotItemVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingServiceImpl implements BookingService {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final BigDecimal DEPOSIT_RATIO = new BigDecimal("0.4");

    @Resource
    private BookingMapper bookingMapper;
    @Resource
    private TimeSlotMapper timeSlotMapper;
    @Resource
    private PaymentMapper paymentMapper;
    @Resource
    private CheckinLogMapper checkinLogMapper;
    @Resource
    private CourtMapper courtMapper;
    @Resource
    private VenueMapper venueMapper;
    @Resource
    private UserService userService;
    @Resource
    private org.example.mq.delayed.DelayedProducer delayedProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingCreateResultVO createBooking(Long userId, BookingCreateDTO dto) {
        List<Long> slotIds = dto.getSlotIds();
        if (slotIds == null || slotIds.isEmpty()) {
            throw new CommonJsonException(new OpResultDTO(400L, "时间片ID列表不能为空"));
        }

        CourtEntity court = courtMapper.selectById(dto.getCourtId());
        if (court == null || (court.getStatus() != null && court.getStatus() != 1)) {
            throw new CommonJsonException(new OpResultDTO(404L, "场地不存在或已停用"));
        }

        List<TimeSlotEntity> slots = timeSlotMapper.selectBatchIds(slotIds);
        if (slots == null || slots.size() != slotIds.size()) {
            throw new CommonJsonException(new OpResultDTO(404L, "部分时间片不存在"));
        }

        for (TimeSlotEntity slot : slots) {
            if (!Objects.equals(slot.getCourtId(), dto.getCourtId())) {
                throw new CommonJsonException(new OpResultDTO(400L, "时间片[" + slot.getId() + "]不属于该场地"));
            }
            if (slot.getStatus() == null || slot.getStatus() != 0) {
                throw new CommonJsonException(new OpResultDTO(409L, "时间片[" + slot.getId() + "]已被预约，请重新选择"));
            }
        }

        List<Long> bookingIds = new ArrayList<>();
        List<BookingCreateResultVO.SlotInfo> slotInfos = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (Long slotId : slotIds) {
            TimeSlotEntity slot = timeSlotMapper.selectById(slotId);

            if (slot.getStartTime() == null || slot.getEndTime() == null) {
                throw new CommonJsonException(new OpResultDTO(500L, "时间片数据异常"));
            }
            LocalDateTime start = slot.getStartTime();
            LocalDateTime end = slot.getEndTime();

            BigDecimal hours = durationHours(start, end);
            BigDecimal slotAmount = court.getPricePerHour().multiply(hours).setScale(2, RoundingMode.HALF_UP);
            totalAmount = totalAmount.add(slotAmount);

            BookingEntity booking = new BookingEntity();
            booking.setUserId(userId);
            booking.setCourtId(dto.getCourtId());
            booking.setStartTime(start);
            booking.setEndTime(end);
            booking.setTotalAmount(slotAmount);
            booking.setDepositAmount(slotAmount.multiply(DEPOSIT_RATIO).setScale(2, RoundingMode.HALF_UP));
            booking.setStatus(0);
            bookingMapper.insert(booking);
            bookingIds.add(booking.getId());

            LambdaUpdateWrapper<TimeSlotEntity> uw = new LambdaUpdateWrapper<>();
            uw.eq(TimeSlotEntity::getId, slotId)
                    .eq(TimeSlotEntity::getStatus, 0)
                    .set(TimeSlotEntity::getStatus, 1)
                    .set(TimeSlotEntity::getBookingId, booking.getId());
            int rows = timeSlotMapper.update(null, uw);
            if (rows == 0) {
                throw new CommonJsonException(new OpResultDTO(409L, "时间片[" + slotId + "]已被预约，请重新选择"));
            }

            BookingCreateResultVO.SlotInfo slotInfo = new BookingCreateResultVO.SlotInfo();
            slotInfo.setSlotId(slotId);
            slotInfo.setStartTime(start);
            slotInfo.setEndTime(end);
            slotInfo.setAmount(slotAmount);
            slotInfos.add(slotInfo);
        }

        BigDecimal depositAmount = totalAmount.multiply(DEPOSIT_RATIO).setScale(2, RoundingMode.HALF_UP);

        long timeoutMillis = 15 * 60 * 1000L;
        for (Long bookingId : bookingIds) {
            delayedProducer.sendDelayedMessage(
                    "BOOKING_TIMEOUT:" + bookingId,
                    timeoutMillis
            );
            log.info("已发送超时违约延迟消息: bookingId={}, 延迟={}ms", bookingId, timeoutMillis);
        }

        BookingCreateResultVO vo = new BookingCreateResultVO();
        vo.setBookingIds(bookingIds);
        vo.setCourtName(court.getName());
        vo.setSlots(slotInfos);
        vo.setTotalAmount(totalAmount);
        vo.setDepositAmount(depositAmount);
        vo.setStatus(0);
        vo.setExpireTime(LocalDateTime.now().plusMinutes(15));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentPayResultVO payDeposit(Long userId, PaymentPayDTO dto) {
        BookingEntity booking = bookingMapper.selectById(dto.getBookingId());
        if (booking == null || !Objects.equals(booking.getUserId(), userId)) {
            throw new CommonJsonException(new OpResultDTO(404L, "预约不存在"));
        }
        if (booking.getStatus() == null || booking.getStatus() != 0) {
            throw new CommonJsonException(new OpResultDTO(400L, "当前状态不可支付"));
        }

        QueryWrapper<PaymentEntity> paidQ = new QueryWrapper<>();
        paidQ.eq("booking_id", dto.getBookingId()).eq("status", 1);
        if (paymentMapper.selectCount(paidQ) > 0) {
            throw new CommonJsonException(new OpResultDTO(400L, "该预约已支付"));
        }

        PaymentEntity payment = new PaymentEntity();
        payment.setBookingId(dto.getBookingId());
        payment.setUserId(userId);
        payment.setAmount(booking.getDepositAmount());
        payment.setPayType(dto.getPayType());
        payment.setStatus(1);
        payment.setTransactionNo("TXN" + System.currentTimeMillis());
        paymentMapper.insert(payment);

        BookingEntity bu = new BookingEntity();
        bu.setId(booking.getId());
        bu.setStatus(1);
        bookingMapper.updateById(bu);

        LambdaUpdateWrapper<TimeSlotEntity> su = new LambdaUpdateWrapper<>();
        su.eq(TimeSlotEntity::getBookingId, booking.getId())
                .set(TimeSlotEntity::getStatus, 2);
        timeSlotMapper.update(null, su);

        long delayMillis = Duration.between(LocalDateTime.now(), booking.getEndTime()).toMillis();
        if (delayMillis > 0) {
            delayedProducer.sendDelayedMessage(
                    "BOOKING_COMPLETE:" + booking.getId(),
                    delayMillis
            );
            log.info("已发送预约完成延迟消息: bookingId={}, 延迟={}ms", booking.getId(), delayMillis);
        } else {
            bookingComplete(booking.getId());
        }

        PaymentPayResultVO vo = new PaymentPayResultVO();
        vo.setPaymentId(payment.getId());
        vo.setBookingId(booking.getId());
        vo.setAmount(payment.getAmount());
        vo.setStatus(payment.getStatus());
        vo.setTransactionNo(payment.getTransactionNo());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long userId, BookingCancelDTO dto) {
        List<Long> bookingIds = dto.getBookingIds();
        if (bookingIds == null || bookingIds.isEmpty()) {
            throw new CommonJsonException(new OpResultDTO(400L, "预约ID列表不能为空"));
        }

        List<BookingEntity> bookings = bookingMapper.selectBatchIds(bookingIds);
        if (bookings == null || bookings.size() != bookingIds.size()) {
            throw new CommonJsonException(new OpResultDTO(404L, "部分预约不存在"));
        }

        for (BookingEntity booking : bookings) {
            if (!Objects.equals(booking.getUserId(), userId)) {
                throw new CommonJsonException(new OpResultDTO(403L, "预约[" + booking.getId() + "]不属于当前用户"));
            }
            Integer st = booking.getStatus();
            if (st == null || st == 2 || st == 3 || st == 4) {
                throw new CommonJsonException(new OpResultDTO(400L, "预约[" + booking.getId() + "]当前状态不可取消"));
            }
        }

        LocalDateTime now = LocalDateTime.now();
        for (Long bookingId : bookingIds) {
            BookingEntity booking = bookingMapper.selectById(bookingId);
            Integer st = booking.getStatus();

            BookingEntity bu = new BookingEntity();
            bu.setId(bookingId);
            bu.setStatus(2);
            bu.setCancelTime(now);
            bookingMapper.updateById(bu);

            LambdaUpdateWrapper<TimeSlotEntity> su = new LambdaUpdateWrapper<>();
            su.eq(TimeSlotEntity::getBookingId, bookingId)
                    .set(TimeSlotEntity::getStatus, 0)
                    .set(TimeSlotEntity::getBookingId, null);
            timeSlotMapper.update(null, su);

            if (st != null && st == 1) {
                LambdaUpdateWrapper<PaymentEntity> pu = new LambdaUpdateWrapper<>();
                pu.eq(PaymentEntity::getBookingId, bookingId).eq(PaymentEntity::getStatus, 1)
                        .set(PaymentEntity::getStatus, 2);
                paymentMapper.update(null, pu);
            }
        }
    }

    @Override
    public PageResult<BookingListItemVO> pageMyBookings(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        int pNum = (pageNum == null || pageNum <= 0) ? 1 : pageNum;
        int pSize = (pageSize == null || pageSize <= 0) ? 10 : pageSize;

        Page<BookingEntity> page = new Page<>(pNum, pSize);
        QueryWrapper<BookingEntity> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        if (status != null) {
            qw.eq("status", status);
        }
        qw.orderByDesc("create_time");
        Page<BookingEntity> result = bookingMapper.selectPage(page, qw);

        List<BookingEntity> records = result.getRecords();
        if (records == null || records.isEmpty()) {
            return PageResult.of(pNum, pSize, result.getTotal(), Collections.emptyList());
        }

        List<Long> courtIds = records.stream().map(BookingEntity::getCourtId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<CourtEntity> courts = courtIds.isEmpty() ? Collections.emptyList() : courtMapper.selectBatchIds(courtIds);
        Map<Long, CourtEntity> courtMap = courts.stream().collect(Collectors.toMap(CourtEntity::getId, c -> c, (a, b) -> a));

        List<Long> venueIds = courts.stream().map(CourtEntity::getVenueId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<VenueEntity> venues = venueIds.isEmpty() ? Collections.emptyList() : venueMapper.selectBatchIds(venueIds);
        Map<Long, VenueEntity> venueMap = venues.stream().collect(Collectors.toMap(VenueEntity::getId, v -> v, (a, b) -> a));

        List<BookingListItemVO> list = records.stream().map(b -> toListItemVO(b, courtMap, venueMap)).collect(Collectors.toList());
        return PageResult.of(pNum, pSize, result.getTotal(), list);
    }

    @Override
    public BookingDetailVO getBookingDetail(Long userId, Long bookingId) {
        BookingEntity booking = bookingMapper.selectById(bookingId);
        if (booking == null || !Objects.equals(booking.getUserId(), userId)) {
            return null;
        }
        CourtEntity court = courtMapper.selectById(booking.getCourtId());
        if (court == null) {
            return null;
        }
        VenueEntity venue = court.getVenueId() == null ? null : venueMapper.selectById(court.getVenueId());
        UserEntity user = userService.getById(booking.getUserId());

        BookingDetailVO vo = new BookingDetailVO();
        BeanUtils.copyProperties(booking, vo);
        vo.setUsername(user != null ? user.getUserName() : null);
        vo.setCourtName(court.getName());
        vo.setVenueName(venue != null ? venue.getName() : null);

        QueryWrapper<PaymentEntity> pq = new QueryWrapper<>();
        pq.eq("booking_id", bookingId).orderByDesc("id");
        List<PaymentEntity> pays = paymentMapper.selectList(pq);
        PaymentEntity show = pays.stream().filter(p -> p.getStatus() != null && p.getStatus() == 1).findFirst()
                .orElse(pays.isEmpty() ? null : pays.get(0));
        if (show != null) {
            BookingPaymentVO pv = new BookingPaymentVO();
            pv.setId(show.getId());
            pv.setAmount(show.getAmount());
            pv.setPayType(show.getPayType());
            pv.setStatus(show.getStatus());
            pv.setTransactionNo(show.getTransactionNo());
            vo.setPayment(pv);
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkin(Long userId, BookingCheckinDTO dto) {
        BookingEntity booking = bookingMapper.selectById(dto.getBookingId());
        if (booking == null || !Objects.equals(booking.getUserId(), userId)) {
            throw new CommonJsonException(new OpResultDTO(404L, "预约不存在"));
        }
        if (booking.getStatus() == null || booking.getStatus() != 1) {
            throw new CommonJsonException(new OpResultDTO(400L, "仅已预约状态可签到"));
        }
        if (booking.getCheckinTime() != null) {
            throw new CommonJsonException(new OpResultDTO(400L, "已签到"));
        }

        LocalDateTime now = LocalDateTime.now();
        BookingEntity bu = new BookingEntity();
        bu.setId(booking.getId());
        bu.setCheckinTime(now);
        bookingMapper.updateById(bu);

        CheckinLogEntity log = new CheckinLogEntity();
        log.setBookingId(booking.getId());
        log.setUserId(userId);
        log.setCheckinTime(now);
        log.setCheckinType(dto.getCheckinType());
        log.setDeviceId(dto.getDeviceId());
        checkinLogMapper.insert(log);
    }

    @Override
    public CourtSlotsVO getCourtSlots(Long courtId, String date) {
        if (StringUtils.isBlank(date)) {
            throw new CommonJsonException(new OpResultDTO(400L, "日期不能为空"));
        }
        CourtEntity court = courtMapper.selectById(courtId);
        if (court == null || (court.getStatus() != null && court.getStatus() != 1)) {
            throw new CommonJsonException(new OpResultDTO(404L, "场地不存在或已停用"));
        }
        LocalDate slotDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        QueryWrapper<TimeSlotEntity> qw = new QueryWrapper<>();
        qw.eq("court_id", courtId).eq("slot_date", slotDate).orderByAsc("start_time");
        List<TimeSlotEntity> slots = timeSlotMapper.selectList(qw);

        CourtSlotsVO vo = new CourtSlotsVO();
        vo.setCourtId(courtId);
        vo.setCourtName(court.getName());
        vo.setPricePerHour(court.getPricePerHour());
        vo.setDate(date);
        if (slots == null || slots.isEmpty()) {
            vo.setSlots(Collections.emptyList());
            return vo;
        }
        List<SlotItemVO> items = slots.stream().map(this::toSlotItemVO).collect(Collectors.toList());
        vo.setSlots(items);
        return vo;
    }

    @Override
    public List<AvailableSlotVO> getAvailableSlots(Long courtId, String date, String startTime, String endTime) {
        if (StringUtils.isBlank(date)) {
            throw new CommonJsonException(new OpResultDTO(400L, "日期不能为空"));
        }
        CourtEntity court = courtMapper.selectById(courtId);
        if (court == null || (court.getStatus() != null && court.getStatus() != 1)) {
            throw new CommonJsonException(new OpResultDTO(404L, "场地不存在或已停用"));
        }
        LocalDate slotDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        QueryWrapper<TimeSlotEntity> qw = new QueryWrapper<>();
        qw.eq("court_id", courtId).eq("slot_date", slotDate).orderByAsc("start_time");
        List<TimeSlotEntity> slots = timeSlotMapper.selectList(qw);
        if (slots == null || slots.isEmpty()) {
            return Collections.emptyList();
        }

        LocalTime filterStart = StringUtils.isNotBlank(startTime) ? LocalTime.parse(startTime, TIME_FMT) : null;
        LocalTime filterEnd = StringUtils.isNotBlank(endTime) ? LocalTime.parse(endTime, TIME_FMT) : null;

        List<AvailableSlotVO> out = new ArrayList<>();
        for (TimeSlotEntity s : slots) {
            LocalTime st = s.getStartTime().toLocalTime();
            LocalTime et = s.getEndTime().toLocalTime();
            if (filterStart != null && st.isBefore(filterStart)) {
                continue;
            }
            if (filterEnd != null && et.isAfter(filterEnd)) {
                continue;
            }
            AvailableSlotVO av = new AvailableSlotVO();
            av.setSlotId(s.getId());
            av.setStartTime(st.format(TIME_FMT));
            av.setEndTime(et.format(TIME_FMT));
            av.setIsAvailable(s.getStatus() != null && s.getStatus() == 0);
            av.setPrice(court.getPricePerHour());
            out.add(av);
        }
        return out;
    }

    private SlotItemVO toSlotItemVO(TimeSlotEntity s) {
        SlotItemVO vo = new SlotItemVO();
        vo.setId(s.getId());
        vo.setStartTime(s.getStartTime().toLocalTime().format(TIME_FMT));
        vo.setEndTime(s.getEndTime().toLocalTime().format(TIME_FMT));
        vo.setStatus(s.getStatus());
        vo.setStatusText(slotStatusText(s.getStatus()));
        return vo;
    }

    private BookingListItemVO toListItemVO(BookingEntity b, Map<Long, CourtEntity> courtMap, Map<Long, VenueEntity> venueMap) {
        BookingListItemVO vo = new BookingListItemVO();
        BeanUtils.copyProperties(b, vo);
        vo.setStatusText(bookingStatusText(b.getStatus()));
        CourtEntity c = courtMap.get(b.getCourtId());
        if (c != null) {
            vo.setCourtName(c.getName());
            VenueEntity v = c.getVenueId() == null ? null : venueMap.get(c.getVenueId());
            vo.setVenueName(v != null ? v.getName() : null);
        }
        return vo;
    }

    private static BigDecimal durationHours(LocalDateTime start, LocalDateTime end) {
        long minutes = Duration.between(start, end).toMinutes();
        if (minutes <= 0) {
            throw new CommonJsonException(new OpResultDTO(400L, "结束时间必须晚于开始时间"));
        }
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
    }

    private static String bookingStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "待支付";
            case 1:
                return "已预约";
            case 2:
                return "已取消";
            case 3:
                return "已完成";
            case 4:
                return "违约";
            default:
                return "未知";
        }
    }

    private static String slotStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "可预约";
            case 1:
                return "锁定中";
            case 2:
                return "已预约";
            default:
                return "未知";
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(Long bookingId, Long userId, BigDecimal amount, String refundType) {
        log.info("MQ退款回调: bookingId={}, userId={}, amount={}, refundType={}", bookingId, userId, amount, refundType);
        BookingEntity booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            log.warn("退款回调失败: 预约不存在, bookingId={}", bookingId);
            return;
        }
        if (booking.getStatus() == null || (booking.getStatus() != 0 && booking.getStatus() != 1)) {
            log.warn("退款回调失败: 预约状态不是待支付或已预约, bookingId={}, status={}", bookingId, booking.getStatus());
            return;
        }
        LambdaUpdateWrapper<PaymentEntity> pw = new LambdaUpdateWrapper<>();
        pw.eq(PaymentEntity::getBookingId, bookingId)
                .eq(PaymentEntity::getStatus, 1)
                .set(PaymentEntity::getStatus, 2);
        paymentMapper.update(null, pw);
        log.info("退款回调成功: 标记payment为已退款, bookingId={}", bookingId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void timeoutCancel(Long bookingId) {
        log.info("MQ超时取消回调: bookingId={}", bookingId);
        BookingEntity booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            log.warn("超时取消回调失败: 预约不存在, bookingId={}", bookingId);
            return;
        }
        if (booking.getStatus() == null || booking.getStatus() != 0) {
            log.warn("超时取消回调失败: 预约状态不是待支付, bookingId={}, status={}", bookingId, booking.getStatus());
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        BookingEntity bu = new BookingEntity();
        bu.setId(bookingId);
        bu.setStatus(2);
        bu.setCancelTime(now);
        bookingMapper.updateById(bu);
        LambdaUpdateWrapper<TimeSlotEntity> su = new LambdaUpdateWrapper<>();
        su.eq(TimeSlotEntity::getBookingId, bookingId)
                .set(TimeSlotEntity::getStatus, 0)
                .set(TimeSlotEntity::getBookingId, null);
        timeSlotMapper.update(null, su);
        log.info("超时取消回调成功: 预约已取消, 时间片已释放, bookingId={}", bookingId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void timeoutCancelAsBreach(Long bookingId) {
        log.info("MQ超时违约回调: bookingId={}", bookingId);
        BookingEntity booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            log.warn("超时违约回调失败: 预约不存在, bookingId={}", bookingId);
            return;
        }
        if (booking.getStatus() == null || booking.getStatus() != 0) {
            log.warn("超时违约回调失败: 预约状态不是待支付, bookingId={}, status={}", bookingId, booking.getStatus());
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        BookingEntity bu = new BookingEntity();
        bu.setId(bookingId);
        bu.setStatus(4);
        bu.setCancelTime(now);
        bookingMapper.updateById(bu);
        LambdaUpdateWrapper<TimeSlotEntity> su = new LambdaUpdateWrapper<>();
        su.eq(TimeSlotEntity::getBookingId, bookingId)
                .set(TimeSlotEntity::getStatus, 0)
                .set(TimeSlotEntity::getBookingId, null);
        timeSlotMapper.update(null, su);
        log.info("超时违约回调成功: 预约状态已改为违约(4), 时间片已释放, bookingId={}", bookingId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bookingComplete(Long bookingId) {
        log.info("MQ预约完成回调: bookingId={}", bookingId);
        BookingEntity booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            log.warn("预约完成回调失败: 预约不存在, bookingId={}", bookingId);
            return;
        }
        if (booking.getStatus() == null || booking.getStatus() != 1) {
            log.warn("预约完成回调失败: 预约状态不是已预约, bookingId={}, status={}", bookingId, booking.getStatus());
            return;
        }
        BookingEntity bu = new BookingEntity();
        bu.setId(bookingId);
        bu.setStatus(3);
        bookingMapper.updateById(bu);
        LambdaUpdateWrapper<TimeSlotEntity> su = new LambdaUpdateWrapper<>();
        su.eq(TimeSlotEntity::getBookingId, bookingId)
                .set(TimeSlotEntity::getStatus, 0)
                .set(TimeSlotEntity::getBookingId, null);
        timeSlotMapper.update(null, su);
        log.info("预约完成回调成功: 预约状态已改为已完成(3), 时间片已释放, bookingId={}", bookingId);
    }
}
