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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TimeSlotGenerateResultVO generateTimeSlots(TimeSlotGenerateDTO dto) {
        CourtEntity court = courtMapper.selectById(dto.getCourtId());
        if (court == null) {
            throw new CommonJsonException(new OpResultDTO(404L, "场地不存在"));
        }
        VenueEntity venue = venueMapper.selectById(court.getVenueId());
        if (venue == null) {
            throw new CommonJsonException(new OpResultDTO(404L, "场馆不存在"));
        }

        LocalDate startDate = LocalDate.parse(dto.getStartDate(), DATE_FMT);
        LocalDate endDate = LocalDate.parse(dto.getEndDate(), DATE_FMT);
        if (startDate.isAfter(endDate)) {
            throw new CommonJsonException(new OpResultDTO(400L, "开始日期不能晚于结束日期"));
        }

        int duration = dto.getSlotDurationMinutes() != null ? dto.getSlotDurationMinutes() : 60;
        if (duration <= 0 || duration > 1440) {
            throw new CommonJsonException(new OpResultDTO(400L, "时间粒度必须在1-1440分钟之间"));
        }

        LocalTime openTime = LocalTime.parse(venue.getOpenTime(), TIME_FMT);
        LocalTime closeTime = LocalTime.parse(venue.getCloseTime(), TIME_FMT);

        boolean ignoreExisting = dto.getIgnoreExisting() != null && dto.getIgnoreExisting();

        List<TimeSlotGenerateResultVO.SlotInfo> generatedSlots = new ArrayList<>();
        int totalGenerated = 0;
        int skipped = 0;

        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            LocalTime slotStart = openTime;
            while (slotStart.plusMinutes(duration).compareTo(closeTime) <= 0) {
                LocalTime slotEnd = slotStart.plusMinutes(duration);

                QueryWrapper<TimeSlotEntity> existWrapper = new QueryWrapper<>();
                existWrapper.eq("court_id", dto.getCourtId())
                        .eq("slot_date", current)
                        .eq("start_time", LocalDateTime.of(current, slotStart))
                        .eq("end_time", LocalDateTime.of(current, slotEnd));

                Long existingCount = timeSlotMapper.selectCount(existWrapper);

                if (existingCount > 0) {
                    if (ignoreExisting) {
                        skipped++;
                    } else {
                        throw new CommonJsonException(new OpResultDTO(400L,
                                "时间片已存在: " + current + " " + slotStart.format(TIME_FMT) + "-" + slotEnd.format(TIME_FMT)));
                    }
                } else {
                    TimeSlotEntity slot = new TimeSlotEntity();
                    slot.setCourtId(dto.getCourtId());
                    slot.setSlotDate(current);
                    slot.setStartTime(LocalDateTime.of(current, slotStart));
                    slot.setEndTime(LocalDateTime.of(current, slotEnd));
                    slot.setStatus(0);
                    slot.setCreateTime(LocalDateTime.now());
                    timeSlotMapper.insert(slot);

                    TimeSlotGenerateResultVO.SlotInfo slotInfo = new TimeSlotGenerateResultVO.SlotInfo();
                    slotInfo.setId(slot.getId());
                    slotInfo.setSlotDate(current.format(DATE_FMT));
                    slotInfo.setStartTime(LocalDateTime.of(current, slotStart).format(DT_FMT));
                    slotInfo.setEndTime(LocalDateTime.of(current, slotEnd).format(DT_FMT));
                    slotInfo.setStatus(0);
                    generatedSlots.add(slotInfo);
                    totalGenerated++;
                }
                slotStart = slotEnd;
            }
            current = current.plusDays(1);
        }

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

    @Override
    public Map<String, Object> getTimeSlotList(Long courtId, Long venueId, String startDate, String endDate, Integer status, Integer page, Integer size) {
        int pageNum = (page == null || page <= 0) ? 1 : page;
        int pageSize = (size == null || size <= 0) ? 20 : size;

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
        queryWrapper.orderByDesc("slot_date", "start_time");
        Page<TimeSlotEntity> result = timeSlotMapper.selectPage(pageParam, queryWrapper);
        List<TimeSlotEntity> records = result.getRecords();

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

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", result.getTotal());
        resultMap.put("page", pageNum);
        resultMap.put("size", pageSize);
        resultMap.put("list", list);
        return resultMap;
    }

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