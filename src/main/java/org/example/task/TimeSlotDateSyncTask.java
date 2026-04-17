package org.example.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.booking.TimeSlotEntity;
import org.example.mapper.booking.TimeSlotMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class TimeSlotDateSyncTask implements ApplicationRunner {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Resource
    private TimeSlotMapper timeSlotMapper;

    @Override
    public void run(ApplicationArguments args) {
        log.info("【时间片日期同步任务】项目启动，执行初始同步");
        syncTimeSlotDates();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void syncTimeSlotDates() {
        log.info("【时间片日期同步任务】开始执行");
        LocalDate today = LocalDate.now();

        QueryWrapper<TimeSlotEntity> queryWrapper = new QueryWrapper<>();

        List<TimeSlotEntity> allSlots = timeSlotMapper.selectList(queryWrapper);

        if (allSlots == null || allSlots.isEmpty()) {
            log.info("【时间片日期同步任务】没有时间片");
            return;
        }

        int updateCount = 0;
        for (TimeSlotEntity slot : allSlots) {
            LocalDate slotDate = slot.getSlotDate();

            if (slotDate.equals(today)) {
                continue;
            }

            LocalTime startTime = slot.getStartTime().toLocalTime();
            LocalTime endTime = slot.getEndTime().toLocalTime();

            LambdaUpdateWrapper<TimeSlotEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(TimeSlotEntity::getId, slot.getId())
                    .set(TimeSlotEntity::getSlotDate, today)
                    .set(TimeSlotEntity::getStartTime, LocalDateTime.of(today, startTime))
                    .set(TimeSlotEntity::getEndTime, LocalDateTime.of(today, endTime));

            timeSlotMapper.update(null, updateWrapper);
            updateCount++;

            log.info("【时间片日期同步任务】更新时间片ID: {}, {} -> {}",
                    slot.getId(), slotDate.format(DATE_FMT), today.format(DATE_FMT));
        }

        log.info("【时间片日期同步任务】执行完成，共更新{}个时间片", updateCount);
    }
}