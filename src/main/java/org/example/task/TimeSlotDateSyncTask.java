package org.example.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.CourtEntity;
import org.example.entity.VenueEntity;
import org.example.entity.booking.TimeSlotEntity;
import org.example.mapper.booking.TimeSlotMapper;
import org.example.mapper.venue.CourtMapper;
import org.example.mapper.venue.VenueMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TimeSlotDateSyncTask implements ApplicationRunner {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final int FUTURE_DAYS = 7;
    private static final int DEFAULT_DURATION_MINUTES = 60;

    @Resource
    private TimeSlotMapper timeSlotMapper;
    @Resource
    private CourtMapper courtMapper;
    @Resource
    private VenueMapper venueMapper;

    @Override
    public void run(ApplicationArguments args) {
        log.info("【时间片同步任务】项目启动，执行初始同步");
        syncTimeSlotDates();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void syncTimeSlotDates() {
        log.info("【时间片同步任务】开始执行");
        LocalDate today = LocalDate.now();

        QueryWrapper<TimeSlotEntity> courtQuery = new QueryWrapper<>();
        courtQuery.select("DISTINCT court_id");
        List<Object> distinctCourtIds = timeSlotMapper.selectObjs(courtQuery);

        if (distinctCourtIds == null || distinctCourtIds.isEmpty()) {
            log.info("【时间片同步任务】没有时间片数据，跳过");
            return;
        }

        int totalGenerated = 0;
        for (Object courtIdObj : distinctCourtIds) {
            Long courtId = ((Number) courtIdObj).longValue();
            int generated = syncCourtTimeSlots(courtId, today);
            totalGenerated += generated;
        }

        log.info("【时间片同步任务】执行完成，共生成{}个时间片", totalGenerated);
    }
//223
    private int syncCourtTimeSlots(Long courtId, LocalDate today) {
        CourtEntity court = courtMapper.selectById(courtId);
        if (court == null) {
            log.warn("【时间片同步任务】场地不存在, courtId={}", courtId);
            return 0;
        }

        VenueEntity venue = venueMapper.selectById(court.getVenueId());
        if (venue == null) {
            log.warn("【时间片同步任务】场馆不存在, courtId={}, venueId={}", courtId, court.getVenueId());
            return 0;
        }

        QueryWrapper<TimeSlotEntity> dateQuery = new QueryWrapper<>();
        dateQuery.select("DISTINCT slot_date")
                .eq("court_id", courtId)
                .ge("slot_date", today)
                .le("slot_date", today.plusDays(FUTURE_DAYS));
        List<Object> existingDates = timeSlotMapper.selectObjs(dateQuery);

        Set<LocalDate> existingDateSet = new HashSet<>();
        if (existingDates != null) {
            for (Object dateObj : existingDates) {
                if (dateObj instanceof LocalDate) {
                    existingDateSet.add((LocalDate) dateObj);
                } else if (dateObj instanceof java.sql.Date) {
                    existingDateSet.add(((java.sql.Date) dateObj).toLocalDate());
                }
            }
        }

        boolean todayExists = existingDateSet.contains(today);

        Set<LocalDate> datesToGenerate = new HashSet<>();

        if (!todayExists) {
            for (int i = 0; i <= FUTURE_DAYS; i++) {
                datesToGenerate.add(today.plusDays(i));
            }
        } else {
            for (int i = 0; i <= FUTURE_DAYS; i++) {
                LocalDate targetDate = today.plusDays(i);
                if (!existingDateSet.contains(targetDate)) {
                    datesToGenerate.add(targetDate);
                }
            }
        }

        if (datesToGenerate.isEmpty()) {
            log.info("【时间片同步任务】场地时间片已完整, courtId={}", courtId);
            return 0;
        }

        log.info("【时间片同步任务】需要生成时间片的日期: {}, courtId={}", datesToGenerate, courtId);

        return generateTimeSlotsForCourt(court, venue, datesToGenerate);
    }

    private int generateTimeSlotsForCourt(CourtEntity court, VenueEntity venue, Set<LocalDate> datesToGenerate) {
        LocalTime venueOpenTime = LocalTime.parse(venue.getOpenTime(), TIME_FMT);
        LocalTime venueCloseTime = LocalTime.parse(venue.getCloseTime(), TIME_FMT);

        LocalTime dayStartTime = venueOpenTime;
        LocalTime dayEndTime = venueCloseTime;
        int duration = DEFAULT_DURATION_MINUTES;

        int generatedCount = 0;

        for (LocalDate targetDate : datesToGenerate) {
            LocalTime slotStart = dayStartTime;
            while (slotStart.plusMinutes(duration).compareTo(dayEndTime) <= 0) {
                LocalTime slotEnd = slotStart.plusMinutes(duration);

                QueryWrapper<TimeSlotEntity> existWrapper = new QueryWrapper<>();
                existWrapper.eq("court_id", court.getId())
                        .eq("slot_date", targetDate)
                        .eq("start_time", LocalDateTime.of(targetDate, slotStart))
                        .eq("end_time", LocalDateTime.of(targetDate, slotEnd));

                Long existingCount = timeSlotMapper.selectCount(existWrapper);

                if (existingCount == 0) {
                    TimeSlotEntity slot = new TimeSlotEntity();
                    slot.setCourtId(court.getId());
                    slot.setSlotDate(targetDate);
                    slot.setStartTime(LocalDateTime.of(targetDate, slotStart));
                    slot.setEndTime(LocalDateTime.of(targetDate, slotEnd));
                    slot.setStatus(0);
                    slot.setCreateTime(LocalDateTime.now());
                    timeSlotMapper.insert(slot);
                    generatedCount++;
                }

                slotStart = slotEnd;
            }
        }

        log.info("【时间片同步任务】场地生成时间片完成, courtId={}, 生成数量={}", court.getId(), generatedCount);
        return generatedCount;
    }
}