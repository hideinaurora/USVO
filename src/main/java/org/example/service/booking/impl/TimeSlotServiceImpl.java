package org.example.service.booking.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.config.exception.CommonJsonException;
import org.example.dto.OpResultDTO;
import org.example.dto.admin.TimeSlotBatchDeleteDTO;
import org.example.dto.admin.TimeSlotGenerateDTO;
import org.example.dto.admin.TimeSlotLockDTO;
import org.example.dto.admin.TimeSlotUpdateDTO;
import org.example.entity.CourtEntity;
import org.example.entity.VenueEntity;
import org.example.entity.booking.BookingEntity;
import org.example.entity.booking.TimeSlotEntity;
import org.example.entity.basic.user.UserEntity;
import org.example.mapper.booking.BookingMapper;
import org.example.mapper.booking.TimeSlotMapper;
import org.example.mapper.basic.user.UserMapper;
import org.example.mapper.venue.CourtMapper;
import org.example.mapper.venue.VenueMapper;
import org.example.service.booking.TimeSlotService;
import org.example.vo.admin.TimeSlotBatchDeleteResultVO;
import org.example.vo.admin.TimeSlotGenerateResultVO;
import org.example.vo.admin.TimeSlotListItemVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TimeSlotServiceImpl implements TimeSlotService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private TimeSlotMapper timeSlotMapper;
    @Resource
    private CourtMapper courtMapper;
    @Resource
    private VenueMapper venueMapper;
    @Resource
    private BookingMapper bookingMapper;
    @Resource
    private UserMapper userMapper;

    /**
     * 批量生成场地的可用时间片
     *
     * @param dto 包含场地ID、日期范围、时间段、时间粒度等参数
     * @return 生成结果，包含生成数量、跳过数量和详细的时间片信息
     * @throws CommonJsonException 场地或场馆不存在、日期或时间逻辑错误时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TimeSlotGenerateResultVO generateTimeSlots(TimeSlotGenerateDTO dto) {
        // 1. 获取场地及其所属场馆信息，确保数据基础存在
        CourtEntity court = courtMapper.selectById(dto.getCourtId());
        if (court == null) {
            throw new CommonJsonException(new OpResultDTO(404L, "场地不存在"));
        }
        VenueEntity venue = venueMapper.selectById(court.getVenueId());
        if (venue == null) {
            throw new CommonJsonException(new OpResultDTO(404L, "场馆不存在"));
        }

        // 2. 确定生成的日期范围：默认今天，或者 DTO 指定的范围
        LocalDate today = LocalDate.now();
        LocalDate startDate = StringUtils.isNotBlank(dto.getStartDate())
                ? LocalDate.parse(dto.getStartDate(), DATE_FMT) : today;
        LocalDate endDate = StringUtils.isNotBlank(dto.getEndDate())
                ? LocalDate.parse(dto.getEndDate(), DATE_FMT) : today;

        if (startDate.isAfter(endDate)) {
            throw new CommonJsonException(new OpResultDTO(400L, "开始日期不能晚于结束日期"));
        }

        // 3. 校验并设置时间分段粒度（如 60 分钟一段）
        int duration = dto.getSlotDurationMinutes() != null ? dto.getSlotDurationMinutes() : 60;
        if (duration <= 0 || duration > 1440) {
            throw new CommonJsonException(new OpResultDTO(400L, "时间粒度必须在1-1440分钟之间"));
        }

        // 4.   获取场馆营业时间，作为生成的上下界限制
        LocalTime venueOpenTime = LocalTime.parse(venue.getOpenTime(), TIME_FMT);
        LocalTime venueCloseTime = LocalTime.parse(venue.getCloseTime(), TIME_FMT);

        // 5.   确定每日生成的时间窗口：取用户指定时间与场馆营业时间的交集
        LocalTime dayStartTime = StringUtils.isNotBlank(dto.getStartTime())
                ? LocalTime.parse(dto.getStartTime(), TIME_FMT) : venueOpenTime;
        LocalTime dayEndTime = StringUtils.isNotBlank(dto.getEndTime())
                ? LocalTime.parse(dto.getEndTime(), TIME_FMT) : venueCloseTime;

        if (dayStartTime.isBefore(venueOpenTime)) {
            dayStartTime = venueOpenTime;
        }
        if (dayEndTime.isAfter(venueCloseTime)) {
            dayEndTime = venueCloseTime;
        }
        if (dayStartTime.compareTo(dayEndTime) >= 0) {
            throw new CommonJsonException(new OpResultDTO(400L, "开始时间不能晚于结束时间"));
        }

        boolean ignoreExisting = dto.getIgnoreExisting() != null && dto.getIgnoreExisting();

        List<TimeSlotGenerateResultVO.SlotInfo> generatedSlots = new ArrayList<>();
        int totalGenerated = 0;
        int skipped = 0;

        // 6.   核心生成逻辑：双层循环（日期 -> 时间段）
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            LocalTime slotStart = dayStartTime;
            // 按照粒度步进生成时间片段
            while (slotStart.plusMinutes(duration).compareTo(dayEndTime) <= 0) {
                LocalTime slotEnd = slotStart.plusMinutes(duration);

                // 7. 冲突检测：检查该时段是否已经存在时间片
                QueryWrapper<TimeSlotEntity> existWrapper = new QueryWrapper<>();
                existWrapper.eq("court_id", dto.getCourtId())
                        .eq("slot_date", current)
                        .eq("start_time", LocalDateTime.of(current, slotStart))
                        .eq("end_time", LocalDateTime.of(current, slotEnd));

                Long existingCount = timeSlotMapper.selectCount(existWrapper);

                if (existingCount > 0) {
                    // 已存在时的处理策略：跳过或报错
                    if (ignoreExisting) {
                        skipped++;
                    } else {
                        throw new CommonJsonException(new OpResultDTO(400L,
                                "时间片已存在: " + current + " " + slotStart.format(TIME_FMT) + "-" + slotEnd.format(TIME_FMT)));
                    }
                } else {
                    // 8. 创建并持久化新时间片
                    TimeSlotEntity slot = new TimeSlotEntity();
                    slot.setCourtId(dto.getCourtId());
                    slot.setSlotDate(current);
                    slot.setStartTime(LocalDateTime.of(current, slotStart));
                    slot.setEndTime(LocalDateTime.of(current, slotEnd));
                    slot.setStatus(0); // 默认：0-可预约
                    slot.setCreateTime(LocalDateTime.now());
                    timeSlotMapper.insert(slot);

                    // 收集生成结果用于前端展示
                    TimeSlotGenerateResultVO.SlotInfo slotInfo = new TimeSlotGenerateResultVO.SlotInfo();
                    slotInfo.setId(slot.getId());
                    slotInfo.setSlotDate(current.format(DATE_FMT));
                    slotInfo.setStartTime(LocalDateTime.of(current, slotStart).format(DT_FMT));
                    slotInfo.setEndTime(LocalDateTime.of(current, slotEnd).format(DT_FMT));
                    slotInfo.setStatus(0);
                    generatedSlots.add(slotInfo);
                    totalGenerated++;
                }
                slotStart = slotEnd; // 时间步进
            }
            current = current.plusDays(1); // 日期步进
        }

        // 9. 封装生成报告并返回
        TimeSlotGenerateResultVO result = new TimeSlotGenerateResultVO();
        result.setCourtId(court.getId());
        result.setCourtName(court.getName());
        result.setVenueName(venue.getName());

        TimeSlotGenerateResultVO.DateRange dateRange = new TimeSlotGenerateResultVO.DateRange();
        dateRange.setStartDate(dto.getStartDate());
        dateRange.setEndDate(dto.getEndDate());
        result.setDateRange(dateRange);

        result.setTotalGenerated(totalGenerated);
        result.setSkipped(skipped);
        result.setSlots(generatedSlots);
        return result;
    }

    /**
     * 管理端分页查询时间片列表
     *
     * @param courtId 场地ID（可选）
     * @param venueId 场馆ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @param status 状态（可选）
     * @param page 页码
     * @param size 每页大小
     * @return 包含总数、分页信息和时间片列表的Map
     */
    @Override
    public Map<String, Object> getTimeSlotList(Long courtId, Long venueId, String startDate, String endDate, Integer status, Integer page, Integer size) {
        // 1. 设置分页参数
        int pageNum = (page == null || page <= 0) ? 1 : page;
        int pageSize = (size == null || size <= 0) ? 20 : size;

        // 2. 构建多条件动态查询
        Page<TimeSlotEntity> pageParam = new Page<>(pageNum, pageSize);
        QueryWrapper<TimeSlotEntity> queryWrapper = new QueryWrapper<>();

        if (courtId != null) {
            queryWrapper.eq("court_id", courtId);
        }
        if (StringUtils.isNotBlank(startDate)) {
            queryWrapper.ge("slot_date", startDate);
        }
        if (StringUtils.isNotBlank(endDate)) {
            queryWrapper.le("slot_date", endDate);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        // 按日期和开始时间倒序排列
        queryWrapper.orderByDesc("slot_date", "start_time");
        
        // 3. 执行分页查询
        Page<TimeSlotEntity> result = timeSlotMapper.selectPage(pageParam, queryWrapper);
        List<TimeSlotEntity> records = result.getRecords();

        // 4. 数据填充：批量获取场地、场馆、预约人等关联信息，避免 N+1 查询
        List<Long> courtIds = records.stream().map(TimeSlotEntity::getCourtId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, CourtEntity> courtMap = new HashMap<>();
        if (!courtIds.isEmpty()) {
            List<CourtEntity> courts = courtMapper.selectBatchIds(courtIds);
            for (CourtEntity c : courts) {
                courtMap.put(c.getId(), c);
            }
        }

        List<Long> venueIds = courtMap.values().stream().map(CourtEntity::getVenueId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, VenueEntity> venueMap = new HashMap<>();
        if (!venueIds.isEmpty()) {
            List<VenueEntity> venues = venueMapper.selectBatchIds(venueIds);
            for (VenueEntity v : venues) {
                venueMap.put(v.getId(), v);
            }
        }

        List<Long> bookingIds = records.stream().map(TimeSlotEntity::getBookingId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, BookingEntity> bookingMap = new HashMap<>();
        Map<Long, String> bookingUserMap = new HashMap<>();
        if (!bookingIds.isEmpty()) {
            List<BookingEntity> bookings = bookingMapper.selectBatchIds(bookingIds);
            for (BookingEntity b : bookings) {
                bookingMap.put(b.getId(), b);
                if (b.getUserId() != null) {
                    UserEntity user = userMapper.selectById(b.getUserId());
                    bookingUserMap.put(b.getId(), user != null ? user.getUserName() : null);
                }
            }
        }

        // 5. 将 Entity 转换为 VO，填充展示所需的文本信息
        List<TimeSlotListItemVO> list = records.stream().map(slot -> {
            TimeSlotListItemVO vo = new TimeSlotListItemVO();
            vo.setId(slot.getId());
            vo.setCourtId(slot.getCourtId());
            CourtEntity court = courtMap.get(slot.getCourtId());
            vo.setCourtName(court != null ? court.getName() : null);
            VenueEntity venue = court != null ? venueMap.get(court.getVenueId()) : null;
            vo.setVenueName(venue != null ? venue.getName() : null);
            vo.setSlotDate(slot.getSlotDate() != null ? slot.getSlotDate().format(DATE_FMT) : null);
            vo.setStartTime(slot.getStartTime() != null ? slot.getStartTime().format(DT_FMT) : null);
            vo.setEndTime(slot.getEndTime() != null ? slot.getEndTime().format(DT_FMT) : null);
            vo.setStatus(slot.getStatus());
            vo.setStatusText(getStatusText(slot.getStatus()));
            vo.setBookingId(slot.getBookingId());
            if (slot.getBookingId() != null) {
                vo.setBookingUser(bookingUserMap.get(slot.getBookingId()));
            }
            vo.setCreateTime(slot.getCreateTime() != null ? slot.getCreateTime().format(DT_FMT) : null);
            return vo;
        }).collect(Collectors.toList());

        // 6. 返回结果封装
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", result.getTotal());
        resultMap.put("page", pageNum);
        resultMap.put("size", pageSize);
        resultMap.put("list", list);
        return resultMap;
    }

    /**
     * 更新单个时间片信息
     *
     * @param slotId 时间片ID
     * @param dto 包含要更新的状态等信息
     * @throws CommonJsonException 时间片不存在或试图将已预约的时间片改为可预约时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTimeSlot(Long slotId, TimeSlotUpdateDTO dto) {
        TimeSlotEntity slot = timeSlotMapper.selectById(slotId);
        if (slot == null) {
            throw new CommonJsonException(new OpResultDTO(404L, "时间片不存在"));
        }
        if (dto.getStatus() != null) {
            if (slot.getStatus() == 2 && dto.getStatus() == 0) {
                throw new CommonJsonException(new OpResultDTO(400L, "该时间片已被预约，无法改为可预约状态"));
            }
            LambdaUpdateWrapper<TimeSlotEntity> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(TimeSlotEntity::getId, slotId).set(TimeSlotEntity::getStatus, dto.getStatus());
            timeSlotMapper.update(null, wrapper);
        }
    }

    /**
     * 删除单个时间片
     *
     * @param slotId 时间片ID
     * @param force 是否强制删除（即使已被预约）
     * @throws CommonJsonException 时间片不存在或已被预约且未强制删除时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTimeSlot(Long slotId, Boolean force) {
        TimeSlotEntity slot = timeSlotMapper.selectById(slotId);
        if (slot == null) {
            throw new CommonJsonException(new OpResultDTO(404L, "时间片不存在"));
        }
        if (slot.getStatus() == 2 && (force == null || !force)) {
            throw new CommonJsonException(new OpResultDTO(400L, "该时间片已被预约，无法删除。如需删除请先取消预约或使用强制删除"));
        }
        timeSlotMapper.deleteById(slotId);
    }

    /**
     * 批量删除时间片
     *
     * @param dto 包含时间片ID列表和强制删除标识
     * @return 批量删除结果，包含成功和失败的数量及失败原因
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TimeSlotBatchDeleteResultVO batchDeleteTimeSlots(TimeSlotBatchDeleteDTO dto) {
        List<Long> slotIds = dto.getSlotIds();
        if (slotIds == null || slotIds.isEmpty()) {
            throw new CommonJsonException(new OpResultDTO(400L, "时间片ID列表不能为空"));
        }

        boolean force = dto.getForce() != null && dto.getForce();
        List<TimeSlotEntity> slots = timeSlotMapper.selectBatchIds(slotIds);
        List<Long> successIds = new ArrayList<>();
        List<Long> failedIds = new ArrayList<>();
        String failReason = null;

        for (TimeSlotEntity slot : slots) {
            if (slot.getStatus() == 2 && !force) {
                failedIds.add(slot.getId());
                if (failReason == null) {
                    failReason = "时间片" + slot.getId() + "已被预约";
                }
            } else {
                timeSlotMapper.deleteById(slot.getId());
                successIds.add(slot.getId());
            }
        }

        TimeSlotBatchDeleteResultVO result = new TimeSlotBatchDeleteResultVO();
        result.setSuccessCount(successIds.size());
        result.setFailCount(failedIds.size());
        result.setFailedIds(failedIds.isEmpty() ? null : failedIds);
        result.setFailReason(failReason);
        return result;
    }

    /**
     * 锁定或解锁时间片
     *
     * @param slotId 时间片ID
     * @param dto 包含锁定标识
     * @throws CommonJsonException 时间片不存在或已被预约时抛出异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockTimeSlot(Long slotId, TimeSlotLockDTO dto) {
        TimeSlotEntity slot = timeSlotMapper.selectById(slotId);
        if (slot == null) {
            throw new CommonJsonException(new OpResultDTO(404L, "时间片不存在"));
        }
        if (slot.getStatus() == 2) {
            throw new CommonJsonException(new OpResultDTO(400L, "该时间片已被预约，无法锁定"));
        }
        LambdaUpdateWrapper<TimeSlotEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(TimeSlotEntity::getId, slotId)
                .set(TimeSlotEntity::getStatus, dto.getLocked() ? 1 : 0);
        timeSlotMapper.update(null, wrapper);
    }

    private String getStatusText(Integer status) {
        if (status == null) return null;
        switch (status) {
            case 0: return "可预约";
            case 1: return "锁定中";
            case 2: return "已预约";
            default: return "未知";
        }
    }
}