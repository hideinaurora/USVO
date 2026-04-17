package org.example.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.booking.TimeSlotEntity;
import org.example.mapper.booking.TimeSlotMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class TimeSlotStatusCheckTask {

    @Resource
    private TimeSlotMapper timeSlotMapper;

    @Scheduled(cron = "0 */10 * * * ?")
    public void checkAndReleaseExpiredSlots() {
        log.info("【时间片状态检查任务】开始执行");
        LocalDateTime now = LocalDateTime.now();

        QueryWrapper<TimeSlotEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 2);

        List<TimeSlotEntity> bookedSlots = timeSlotMapper.selectList(queryWrapper);

        if (bookedSlots == null || bookedSlots.isEmpty()) {
            log.info("【时间片状态检查任务】没有已预约的时间片");
            return;
        }

        int releaseCount = 0;
        for (TimeSlotEntity slot : bookedSlots) {
            if (slot.getEndTime() == null) {
                continue;
            }

            if (now.isAfter(slot.getEndTime())) {
                LambdaUpdateWrapper<TimeSlotEntity> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(TimeSlotEntity::getId, slot.getId())
                        .set(TimeSlotEntity::getStatus, 0)
                        .set(TimeSlotEntity::getBookingId, null);
                timeSlotMapper.update(null, updateWrapper);
                releaseCount++;

                log.info("【时间片状态检查任务】释放过期时间片: id={}, endTime={}",
                        slot.getId(), slot.getEndTime());
            }
        }

        log.info("【时间片状态检查任务】执行完成，共释放{}个过期时间片", releaseCount);
    }
}