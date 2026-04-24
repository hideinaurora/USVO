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
import org.example.task.ViolationCheckTask;
import org.example.vo.booking.AvailableSlotVO;
import org.example.vo.booking.BookingCreateResultVO;
import org.example.vo.booking.BookingDetailVO;
import org.example.vo.booking.BookingListItemVO;
import org.example.vo.booking.BookingPaymentVO;
import org.example.vo.booking.CourtSlotsVO;
import org.example.vo.booking.PaymentPayResultVO;
import org.example.vo.booking.SlotItemVO;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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
import java.util.*;
import java.util.concurrent.TimeUnit;
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
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private ViolationCheckTask violationCheckTask;

    /**
     * 创建场馆预约
     *
     * @param userId 用户ID
     * @param dto 预约信息，包含场地ID和时间片ID列表
     * @return 预约结果，包含生成的预约ID、总金额、定金等
     * @throws CommonJsonException 参数错误、场地不可用、时间片已被预约等情况下抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingCreateResultVO createBooking(Long userId, BookingCreateDTO dto) {
        // 1. 基础校验
        List<Long> slotIds = dto.getSlotIds();
        if (slotIds == null || slotIds.isEmpty()) {
            throw new CommonJsonException(new OpResultDTO(400L, "时间片ID列表不能为空"));
        }

        // 2. 校验场地状态
        CourtEntity court = courtMapper.selectById(dto.getCourtId());
        if (court == null || (court.getStatus() != null && court.getStatus() != 1)) {
            throw new CommonJsonException(new OpResultDTO(404L, "场地不存在或已停用"));
        }

        // 3. 准备分布式锁的Keys，防止并发预约同一时间片
        Set<String> lockKeys = new HashSet<>();
        for (Long slotId : slotIds) {
            lockKeys.add("lock:booking:slot:" + slotId);
        }

        List<RLock> acquiredLocks = new ArrayList<>();
        try {
            // 4. 尝试获取所有时间片的锁
            for (String lockKey : lockKeys) {
                RLock lock = redissonClient.getLock(lockKey);
                boolean locked = lock.tryLock(5, 30, TimeUnit.SECONDS);
                if (!locked) {
                    throw new CommonJsonException(new OpResultDTO(409L, "系统繁忙，请稍后重试"));
                }
                acquiredLocks.add(lock);
            }

            // 5. 校验时间片是否存在且属于该场地，以及是否已被预约
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

            // 6. 循环处理每个时间片，生成预约记录并更新时间片状态
            for (Long slotId : slotIds) {
                TimeSlotEntity slot = timeSlotMapper.selectById(slotId);

                if (slot.getStartTime() == null || slot.getEndTime() == null) {
                    throw new CommonJsonException(new OpResultDTO(500L, "时间片数据异常"));
                }
                LocalDateTime start = slot.getStartTime();
                LocalDateTime end = slot.getEndTime();

                // 计算费用
                BigDecimal hours = durationHours(start, end);
                BigDecimal slotAmount = court.getPricePerHour().multiply(hours).setScale(2, RoundingMode.HALF_UP);
                totalAmount = totalAmount.add(slotAmount);

                // 创建预约实体
                BookingEntity booking = new BookingEntity();
                booking.setUserId(userId);
                booking.setCourtId(dto.getCourtId());
                booking.setStartTime(start);
                booking.setEndTime(end);
                booking.setTotalAmount(slotAmount);
                booking.setDepositAmount(slotAmount.multiply(DEPOSIT_RATIO).setScale(2, RoundingMode.HALF_UP));
                booking.setStatus(0); // 待支付
                bookingMapper.insert(booking);
                bookingIds.add(booking.getId());

                // 更新时间片为“锁定中”，并关联预约ID
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

            // 7. 发送延迟消息，15分钟不支付自动取消预约
            long timeoutMillis = 15 * 60 * 1000L;
            for (Long bookingId : bookingIds) {
                delayedProducer.sendDelayedMessage(
                        "BOOKING_TIMEOUT:" + bookingId,
                        timeoutMillis
                );
                log.info("已发送超时违约延迟消息: bookingId={}, 延迟={}ms", bookingId, timeoutMillis);
            }

            // 8. 构建并返回结果
            BookingCreateResultVO vo = new BookingCreateResultVO();
            vo.setBookingIds(bookingIds);
            vo.setCourtName(court.getName());
            vo.setSlots(slotInfos);
            vo.setTotalAmount(totalAmount);
            vo.setDepositAmount(depositAmount);
            vo.setStatus(0);
            vo.setExpireTime(LocalDateTime.now().plusMinutes(15));
            return vo;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommonJsonException(new OpResultDTO(500L, "预约过程被中断，请重试"));
        } finally {
            // 9. 释放所有已获取的分布式锁
            for (RLock lock : acquiredLocks) {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }

    /**
     * 支付预约定金
     *
     * @param userId 用户ID
     * @param dto 支付信息，包含预约ID和支付类型
     * @return 支付结果，包含流水号和状态
     * @throws CommonJsonException 预约不存在、状态不符或已支付等情况下抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaymentPayResultVO payDeposit(Long userId, PaymentPayDTO dto) {
        String lockKey = "lock:booking:pay:" + dto.getBookingId();
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 1. 获取支付锁
            lock.tryLock(5, 30, TimeUnit.SECONDS);
            
            // 2. 校验预约信息及状态
            BookingEntity booking = bookingMapper.selectById(dto.getBookingId());
            if (booking == null || !Objects.equals(booking.getUserId(), userId)) {
                throw new CommonJsonException(new OpResultDTO(404L, "预约不存在"));
            }
            if (booking.getStatus() == null || booking.getStatus() != 0) {
                throw new CommonJsonException(new OpResultDTO(400L, "当前状态不可支付"));
            }

            // 3. 校验是否已存在支付成功的记录
            QueryWrapper<PaymentEntity> paidQ = new QueryWrapper<>();
            paidQ.eq("booking_id", dto.getBookingId()).eq("status", 1);
            if (paymentMapper.selectCount(paidQ) > 0) {
                throw new CommonJsonException(new OpResultDTO(400L, "该预约已支付"));
            }

            // 4. 创建支付流水记录
            PaymentEntity payment = new PaymentEntity();
            payment.setBookingId(dto.getBookingId());
            payment.setUserId(userId);
            payment.setAmount(booking.getDepositAmount());
            payment.setPayType(dto.getPayType());
            payment.setStatus(1); // 已支付
            payment.setTransactionNo("TXN" + System.currentTimeMillis());
            paymentMapper.insert(payment);

            // 5. 更新预约状态为“已预约”
            BookingEntity bu = new BookingEntity();
            bu.setId(booking.getId());
            bu.setStatus(1);
            bookingMapper.updateById(bu);

            // 6. 更新对应时间片状态为“已预约”
            LambdaUpdateWrapper<TimeSlotEntity> su = new LambdaUpdateWrapper<>();
            su.eq(TimeSlotEntity::getBookingId, booking.getId())
                    .set(TimeSlotEntity::getStatus, 2);
            timeSlotMapper.update(null, su);

            // 7. 发送预约结束时的延迟消息，用于后续判定违约或标记完成
            long delayMillis = Duration.between(LocalDateTime.now(), booking.getEndTime()).toMillis();
            if (delayMillis > 0) {
                delayedProducer.sendDelayedMessage(
                        "BOOKING_COMPLETE:" + booking.getId(),
                        delayMillis
                );
                log.info("已发送预约完成延迟消息: bookingId={}, 延迟={}ms", booking.getId(), delayMillis);
            } else {
                // 如果预约已经结束，直接执行完成逻辑
                bookingComplete(booking.getId());
            }

            PaymentPayResultVO vo = new PaymentPayResultVO();
            vo.setPaymentId(payment.getId());
            vo.setBookingId(booking.getId());
            vo.setAmount(payment.getAmount());
            vo.setStatus(payment.getStatus());
            vo.setTransactionNo(payment.getTransactionNo());
            return vo;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommonJsonException(new OpResultDTO(500L, "支付过程被中断，请重试"));
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 取消预约
     *
     * @param userId 用户ID
     * @param dto 包含要取消的预约ID列表
     * @throws CommonJsonException 预约不存在、不属于该用户或状态不可取消时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelBooking(Long userId, BookingCancelDTO dto) {
        // 1. 基础校验
        List<Long> bookingIds = dto.getBookingIds();
        if (bookingIds == null || bookingIds.isEmpty()) {
            throw new CommonJsonException(new OpResultDTO(400L, "预约ID列表不能为空"));
        }

        // 2. 准备分布式锁，防止并发取消/支付冲突
        Set<String> lockKeys = new HashSet<>();
        for (Long bookingId : bookingIds) {
            lockKeys.add("lock:booking:cancel:" + bookingId);
        }

        List<RLock> acquiredLocks = new ArrayList<>();
        try {
            // 3. 获取锁
            for (String lockKey : lockKeys) {
                RLock lock = redissonClient.getLock(lockKey);
                boolean locked = lock.tryLock(5, 30, TimeUnit.SECONDS);
                if (!locked) {
                    throw new CommonJsonException(new OpResultDTO(409L, "系统繁忙，请稍后重试"));
                }
                acquiredLocks.add(lock);
            }

            // 4. 批量查询预约记录并进行权限和状态校验
            List<BookingEntity> bookings = bookingMapper.selectBatchIds(bookingIds);
            if (bookings == null || bookings.size() != bookingIds.size()) {
                throw new CommonJsonException(new OpResultDTO(404L, "部分预约不存在"));
            }

            for (BookingEntity booking : bookings) {
                // 校验所属权
                if (!Objects.equals(booking.getUserId(), userId)) {
                    throw new CommonJsonException(new OpResultDTO(403L, "预约[" + booking.getId() + "]不属于当前用户"));
                }
                // 校验状态：仅“待支付”和“已预约”可取消
                Integer st = booking.getStatus();
                if (st == null || st == 2 || st == 3 || st == 4) {
                    throw new CommonJsonException(new OpResultDTO(400L, "预约[" + booking.getId() + "]当前状态不可取消"));
                }
            }

            // 5. 执行取消逻辑
            LocalDateTime now = LocalDateTime.now();
            for (Long bookingId : bookingIds) {
                BookingEntity booking = bookingMapper.selectById(bookingId);
                Integer st = booking.getStatus();

                // 更新预约表状态为“已取消”
                BookingEntity bu = new BookingEntity();
                bu.setId(bookingId);
                bu.setStatus(2);
                bu.setCancelTime(now);
                bookingMapper.updateById(bu);

                // 释放对应的时间片资源
                LambdaUpdateWrapper<TimeSlotEntity> su = new LambdaUpdateWrapper<>();
                su.eq(TimeSlotEntity::getBookingId, bookingId)
                        .set(TimeSlotEntity::getStatus, 0)
                        .set(TimeSlotEntity::getBookingId, null);
                timeSlotMapper.update(null, su);

                // 如果已经支付过定金，则将支付流水标记为“已退款”（此处简化处理，实际应对接退款接口）
                if (st != null && st == 1) {
                    LambdaUpdateWrapper<PaymentEntity> pu = new LambdaUpdateWrapper<>();
                    pu.eq(PaymentEntity::getBookingId, bookingId).eq(PaymentEntity::getStatus, 1)
                            .set(PaymentEntity::getStatus, 2);
                    paymentMapper.update(null, pu);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommonJsonException(new OpResultDTO(500L, "取消过程被中断，请重试"));
        } finally {
            // 6. 释放所有分布式锁
            for (RLock lock : acquiredLocks) {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }

    /**
     * 分页查询我的预约列表
     *
     * @param userId 用户ID
     * @param status 预约状态（可选）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Override
    public PageResult<BookingListItemVO> pageMyBookings(Long userId, Integer status, Integer pageNum, Integer pageSize) {
        // 1. 初始化分页参数
        int pNum = (pageNum == null || pageNum <= 0) ? 1 : pageNum;
        int pSize = (pageSize == null || pageSize <= 0) ? 10 : pageSize;

        // 2. 构建查询条件
        Page<BookingEntity> page = new Page<>(pNum, pSize);
        QueryWrapper<BookingEntity> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        if (status != null) {
            qw.eq("status", status);
        }
        qw.orderByDesc("create_time");
        
        // 3. 执行分页查询
        Page<BookingEntity> result = bookingMapper.selectPage(page, qw);

        List<BookingEntity> records = result.getRecords();
        if (records == null || records.isEmpty()) {
            return PageResult.of(pNum, pSize, result.getTotal(), Collections.emptyList());
        }

        // 4. 批量获取关联的场地和场馆信息，避免循环内查询数据库
        List<Long> courtIds = records.stream().map(BookingEntity::getCourtId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<CourtEntity> courts = courtIds.isEmpty() ? Collections.emptyList() : courtMapper.selectBatchIds(courtIds);
        Map<Long, CourtEntity> courtMap = courts.stream().collect(Collectors.toMap(CourtEntity::getId, c -> c, (a, b) -> a));

        List<Long> venueIds = courts.stream().map(CourtEntity::getVenueId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<VenueEntity> venues = venueIds.isEmpty() ? Collections.emptyList() : venueMapper.selectBatchIds(venueIds);
        Map<Long, VenueEntity> venueMap = venues.stream().collect(Collectors.toMap(VenueEntity::getId, v -> v, (a, b) -> a));

        // 5. 转换 Entity 为 VO 并填充冗余展示字段
        List<BookingListItemVO> list = records.stream().map(b -> toListItemVO(b, courtMap, venueMap)).collect(Collectors.toList());
        return PageResult.of(pNum, pSize, result.getTotal(), list);
    }

    /**
     * 获取预约详情
     *
     * @param userId 用户ID
     * @param bookingId 预约ID
     * @return 预约详情对象
     */
    @Override
    public BookingDetailVO getBookingDetail(Long userId, Long bookingId) {
        // 1. 获取预约基础信息并校验权限
        BookingEntity booking = bookingMapper.selectById(bookingId);
        if (booking == null || !Objects.equals(booking.getUserId(), userId)) {
            return null;
        }
        
        // 2. 级联获取场地、场馆和用户信息
        CourtEntity court = courtMapper.selectById(booking.getCourtId());
        if (court == null) {
            return null;
        }
        VenueEntity venue = court.getVenueId() == null ? null : venueMapper.selectById(court.getVenueId());
        UserEntity user = userService.getById(booking.getUserId());

        // 3. 组装 VO 数据
        BookingDetailVO vo = new BookingDetailVO();
        BeanUtils.copyProperties(booking, vo);
        vo.setUsername(user != null ? user.getUserName() : null);
        vo.setCourtName(court.getName());
        vo.setVenueName(venue != null ? venue.getName() : null);

        // 4. 获取关联的支付流水信息（优先展示成功的流水）
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

    /**
     * 预约签到
     *
     * @param userId 用户ID
     * @param dto 包含预约ID、签到方式、设备ID等
     * @throws CommonJsonException 预约不存在、状态不符或已签到时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkin(Long userId, BookingCheckinDTO dto) {
        // 1. 存在性及权限校验
        BookingEntity booking = bookingMapper.selectById(dto.getBookingId());
        if (booking == null || !Objects.equals(booking.getUserId(), userId)) {
            throw new CommonJsonException(new OpResultDTO(404L, "预约不存在"));
        }
        
        // 2. 状态校验：只有“已预约（已支付）”状态才允许签到
        if (booking.getStatus() == null || booking.getStatus() != 1) {
            throw new CommonJsonException(new OpResultDTO(400L, "仅已预约状态可签到"));
        }
        
        // 3. 幂等校验：防止重复签到
        if (booking.getCheckinTime() != null) {
            throw new CommonJsonException(new OpResultDTO(400L, "已签到"));
        }

        // 4. 更新预约表的签到时间
        LocalDateTime now = LocalDateTime.now();
        BookingEntity bu = new BookingEntity();
        bu.setId(booking.getId());
        bu.setCheckinTime(now);
        bookingMapper.updateById(bu);

        // 5. 插入详细的签到日志
        CheckinLogEntity log = new CheckinLogEntity();
        log.setBookingId(booking.getId());
        log.setUserId(userId);
        log.setCheckinTime(now);
        log.setCheckinType(dto.getCheckinType());
        log.setDeviceId(dto.getDeviceId());
        checkinLogMapper.insert(log);
    }

    /**
     * 获取指定场地在指定日期的所有时间片状态
     *
     * @param courtId 场地ID
     * @param date 日期字符串 (yyyy-MM-dd)
     * @return 场地时间片视图对象
     */
    @Override
    public CourtSlotsVO getCourtSlots(Long courtId, String date) {
        // 1. 参数校验
        if (StringUtils.isBlank(date)) {
            throw new CommonJsonException(new OpResultDTO(400L, "日期不能为空"));
        }
        
        // 2. 场地存在性及状态校验
        CourtEntity court = courtMapper.selectById(courtId);
        if (court == null || (court.getStatus() != null && court.getStatus() != 1)) {
            throw new CommonJsonException(new OpResultDTO(404L, "场地不存在或已停用"));
        }
        
        // 3. 查询该场地当天的所有时间片，按开始时间排序
        LocalDate slotDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        QueryWrapper<TimeSlotEntity> qw = new QueryWrapper<>();
        qw.eq("court_id", courtId).eq("slot_date", slotDate).orderByAsc("start_time");
        List<TimeSlotEntity> slots = timeSlotMapper.selectList(qw);

        // 4. 组装返回 VO
        CourtSlotsVO vo = new CourtSlotsVO();
        vo.setCourtId(courtId);
        vo.setCourtName(court.getName());
        vo.setPricePerHour(court.getPricePerHour());
        vo.setDate(date);
        if (slots == null || slots.isEmpty()) {
            vo.setSlots(Collections.emptyList());
            return vo;
        }
        
        // 5. 转换时间片实体为展示项
        List<SlotItemVO> items = slots.stream().map(this::toSlotItemVO).collect(Collectors.toList());
        vo.setSlots(items);
        return vo;
    }

    /**
     * 根据时间范围筛选可用时间片
     *
     * @param courtId 场地ID
     * @param date 日期
     * @param startTime 开始时间筛选 (HH:mm:ss)
     * @param endTime 结束时间筛选 (HH:mm:ss)
     * @return 可用时间片列表
     */
    @Override
    public List<AvailableSlotVO> getAvailableSlots(Long courtId, String date, String startTime, String endTime) {
        // 1. 基础校验
        if (StringUtils.isBlank(date)) {
            throw new CommonJsonException(new OpResultDTO(400L, "日期不能为空"));
        }
        CourtEntity court = courtMapper.selectById(courtId);
        if (court == null || (court.getStatus() != null && court.getStatus() != 1)) {
            throw new CommonJsonException(new OpResultDTO(404L, "场地不存在或已停用"));
        }
        
        // 2. 查询该场地当天的全量时间片
        LocalDate slotDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
        QueryWrapper<TimeSlotEntity> qw = new QueryWrapper<>();
        qw.eq("court_id", courtId).eq("slot_date", slotDate).orderByAsc("start_time");
        List<TimeSlotEntity> slots = timeSlotMapper.selectList(qw);
        if (slots == null || slots.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 解析时间过滤范围
        LocalTime filterStart = StringUtils.isNotBlank(startTime) ? LocalTime.parse(startTime, TIME_FMT) : null;
        LocalTime filterEnd = StringUtils.isNotBlank(endTime) ? LocalTime.parse(endTime, TIME_FMT) : null;

        // 4. 遍历并执行内存过滤
        List<AvailableSlotVO> out = new ArrayList<>();
        for (TimeSlotEntity s : slots) {
            LocalTime st = s.getStartTime().toLocalTime();
            LocalTime et = s.getEndTime().toLocalTime();
            
            // 如果不在指定的时间段内，则跳过
            if (filterStart != null && st.isBefore(filterStart)) {
                continue;
            }
            if (filterEnd != null && et.isAfter(filterEnd)) {
                continue;
            }
            
            // 组装 VO，并标记是否可预约（状态为 0 表示可预约）
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
        // 1. 实例化展示 VO 对象
        SlotItemVO vo = new SlotItemVO();
        // 2. 映射基础字段，将时间格式化为 HH:mm:ss 格式
        vo.setId(s.getId());
        vo.setStartTime(s.getStartTime().toLocalTime().format(TIME_FMT));
        vo.setEndTime(s.getEndTime().toLocalTime().format(TIME_FMT));
        // 3. 设置状态码及对应的状态文本
        vo.setStatus(s.getStatus());
        vo.setStatusText(slotStatusText(s.getStatus()));
        return vo;
    }

    private BookingListItemVO toListItemVO(BookingEntity b, Map<Long, CourtEntity> courtMap, Map<Long, VenueEntity> venueMap) {
        // 1. 实例化列表项 VO 并拷贝基础属性
        BookingListItemVO vo = new BookingListItemVO();
        BeanUtils.copyProperties(b, vo);
        // 2. 设置预约状态的文本描述
        vo.setStatusText(bookingStatusText(b.getStatus()));
        // 3. 关联查询场地信息，并从预加载的 Map 中获取
        CourtEntity c = courtMap.get(b.getCourtId());
        if (c != null) {
            vo.setCourtName(c.getName());
            // 4. 根据场地信息进一步关联场馆名称
            VenueEntity v = c.getVenueId() == null ? null : venueMap.get(c.getVenueId());
            vo.setVenueName(v != null ? v.getName() : null);
        }
        return vo;
    }

    private static BigDecimal durationHours(LocalDateTime start, LocalDateTime end) {
        // 1. 计算两个时间点之间的分钟数差值
        long minutes = Duration.between(start, end).toMinutes();
        if (minutes <= 0) {
            throw new CommonJsonException(new OpResultDTO(400L, "结束时间必须晚于开始时间"));
        }
        // 2. 将分钟转换为小时，保留4位小数并进行四舍五入
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 4, RoundingMode.HALF_UP);
    }

    private static String bookingStatusText(Integer status) {
        // 1. 状态为空时返回未知
        if (status == null) {
            return "未知";
        }
        // 2. 根据状态码返回对应的业务文本
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
        // 1. 状态为空时返回未知
        if (status == null) {
            return "未知";
        }
        // 2. 根据时间片状态码返回对应的展示文本
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

    /**
     * 处理退款逻辑（由MQ或回调触发）
     *
     * @param bookingId 预约ID
     * @param userId 用户ID
     * @param amount 退款金额
     * @param refundType 退款类型
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refund(Long bookingId, Long userId, BigDecimal amount, String refundType) {
        String lockKey = "lock:booking:refund:" + bookingId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            // 1. 获取退款锁，防止并发回调处理
            lock.tryLock(5, 30, TimeUnit.SECONDS);
            log.info("MQ退款回调: bookingId={}, userId={}, amount={}, refundType={}", bookingId, userId, amount, refundType);
            
            // 2. 校验预约记录是否存在
            BookingEntity booking = bookingMapper.selectById(bookingId);
            if (booking == null) {
                log.warn("退款回调失败: 预约不存在, bookingId={}", bookingId);
                return;
            }
            
            // 3. 校验状态：只有处于待支付或已预约状态的订单才处理退款逻辑（根据具体业务调整）
            if (booking.getStatus() == null || (booking.getStatus() != 0 && booking.getStatus() != 1)) {
                log.warn("退款回调失败: 预约状态不是待支付或已预约, bookingId={}, status={}", bookingId, booking.getStatus());
                return;
            }
            
            // 4. 更新支付记录状态为“已退款”
            LambdaUpdateWrapper<PaymentEntity> pw = new LambdaUpdateWrapper<>();
            pw.eq(PaymentEntity::getBookingId, bookingId)
                    .eq(PaymentEntity::getStatus, 1) // 仅更新已支付的记录
                    .set(PaymentEntity::getStatus, 2);
            paymentMapper.update(null, pw);
            log.info("退款回调成功: 标记payment为已退款, bookingId={}", bookingId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("退款回调被中断: bookingId={}", bookingId);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 处理超时未支付取消（由MQ延迟消息触发）
     *
     * @param bookingId 预约ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void timeoutCancel(Long bookingId) {
        log.info("MQ超时取消回调: bookingId={}", bookingId);
        
        // 1. 获取预约记录
        BookingEntity booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            log.warn("超时取消回调失败: 预约不存在, bookingId={}", bookingId);
            return;
        }
        
        // 2. 状态校验：只有仍处于“待支付”状态的订单才执行自动取消
        if (booking.getStatus() == null || booking.getStatus() != 0) {
            log.warn("超时取消回调失败: 预约状态不是待支付, bookingId={}, status={}", bookingId, booking.getStatus());
            return;
        }
        
        // 3. 更新预约状态为“已取消”，并记录取消时间
        LocalDateTime now = LocalDateTime.now();
        BookingEntity bu = new BookingEntity();
        bu.setId(bookingId);
        bu.setStatus(2);
        bu.setCancelTime(now);
        bookingMapper.updateById(bu);
        
        // 4. 释放关联的时间片资源，使其重新变为“可预约”
        LambdaUpdateWrapper<TimeSlotEntity> su = new LambdaUpdateWrapper<>();
        su.eq(TimeSlotEntity::getBookingId, bookingId)
                .set(TimeSlotEntity::getStatus, 0)
                .set(TimeSlotEntity::getBookingId, null);
        timeSlotMapper.update(null, su);
        log.info("超时取消回调成功: 预约已取消, 时间片已释放, bookingId={}", bookingId);
    }

    /**
     * 处理超时未支付且判定为违约（由MQ延迟消息触发）
     *
     * @param bookingId 预约ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void timeoutCancelAsBreach(Long bookingId) {
        log.info("MQ超时违约回调: bookingId={}", bookingId);
        
        // 1. 获取预约记录
        BookingEntity booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            log.warn("超时违约回调失败: 预约不存在, bookingId={}", bookingId);
            return;
        }
        
        // 2. 状态校验：仅针对待支付订单
        if (booking.getStatus() == null || booking.getStatus() != 0) {
            log.warn("超时违约回调失败: 预约状态不是待支付, bookingId={}, status={}", bookingId, booking.getStatus());
            return;
        }
        
        // 3. 更新预约状态为“违约”，并记录处理时间
        LocalDateTime now = LocalDateTime.now();
        BookingEntity bu = new BookingEntity();
        bu.setId(bookingId);
        bu.setStatus(4); // 4-违约
        bu.setCancelTime(now);
        bookingMapper.updateById(bu);
        
        // 4. 释放时间片资源
        LambdaUpdateWrapper<TimeSlotEntity> su = new LambdaUpdateWrapper<>();
        su.eq(TimeSlotEntity::getBookingId, bookingId)
                .set(TimeSlotEntity::getStatus, 0)
                .set(TimeSlotEntity::getBookingId, null);
        timeSlotMapper.update(null, su);
        log.info("超时违约回调成功: 预约状态已改为违约(4), 时间片已释放, bookingId={}", bookingId);
    }

    /**
     * 处理预约结束自动完成逻辑（由MQ延迟消息触发）
     *
     * @param bookingId 预约ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bookingComplete(Long bookingId) {
        log.info("MQ预约完成回调: bookingId={}", bookingId);
        
        // 1. 获取预约记录
        BookingEntity booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            log.warn("预约完成回调失败: 预约不存在, bookingId={}", bookingId);
            return;
        }
        
        // 2. 状态校验：只有“已预约”状态的订单在结束后才执行完成逻辑
        if (booking.getStatus() == null || booking.getStatus() != 1) {
            log.warn("预约完成回调失败: 预约状态不是已预约, bookingId={}, status={}", bookingId, booking.getStatus());
            return;
        }

        // 3. 违约判定：检查用户是否按时签到，若未签到则执行违约处理逻辑
        boolean isBreach = violationCheckTask.checkAndProcessBreach(bookingId);
        if (isBreach) {
            log.info("预约完成回调: 用户违约已处理, bookingId={}", bookingId);
            return;
        }

        // 4. 若无违约，更新预约状态为“已完成”
        BookingEntity bu = new BookingEntity();
        bu.setId(bookingId);
        bu.setStatus(3); // 3-已完成
        bookingMapper.updateById(bu);
        
        // 5. 释放关联的时间片资源
        LambdaUpdateWrapper<TimeSlotEntity> su = new LambdaUpdateWrapper<>();
        su.eq(TimeSlotEntity::getBookingId, bookingId)
                .set(TimeSlotEntity::getStatus, 0)
                .set(TimeSlotEntity::getBookingId, null);
        timeSlotMapper.update(null, su);
        log.info("预约完成回调成功: 预约状态已改为已完成(3), 时间片已释放, bookingId={}", bookingId);
    }
}
