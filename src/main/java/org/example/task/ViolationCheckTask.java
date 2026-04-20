package org.example.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.basic.user.UserEntity;
import org.example.entity.booking.BookingEntity;
import org.example.entity.booking.PaymentEntity;
import org.example.entity.booking.TimeSlotEntity;
import org.example.mapper.basic.user.UserMapper;
import org.example.mapper.booking.BookingMapper;
import org.example.mapper.booking.PaymentMapper;
import org.example.mapper.booking.TimeSlotMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class ViolationCheckTask {

    private static final int BREACH_STATUS = 4;

    @Resource
    private BookingMapper bookingMapper;
    @Resource
    private TimeSlotMapper timeSlotMapper;
    @Resource
    private PaymentMapper paymentMapper;
    @Resource
    private UserMapper userMapper;

    @Scheduled(cron = "0 */10 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void checkNoShowBookings() {
        log.info("【违约检查任务】开始执行");
        LocalDateTime now = LocalDateTime.now();

        QueryWrapper<BookingEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);

        List<BookingEntity> activeBookings = bookingMapper.selectList(queryWrapper);

        if (activeBookings == null || activeBookings.isEmpty()) {
            log.info("【违约检查任务】没有待处理的预约");
            return;
        }

        int breachCount = 0;
        for (BookingEntity booking : activeBookings) {
            if (booking.getEndTime() == null) {
                continue;
            }

            LocalDateTime checkinTime = booking.getCheckinTime();
            LocalDateTime graceEndTime = booking.getEndTime().plusHours(1);

            boolean isBreach = (checkinTime == null && now.isAfter(graceEndTime))
                    || (checkinTime != null && checkinTime.isAfter(graceEndTime));

            if (isBreach) {
                processBreach(booking);
                breachCount++;
            }
        }

        log.info("【违约检查任务】执行完成，共处理{}个违约预约", breachCount);
    }

    private void processBreach(BookingEntity booking) {
        log.info("【违约检查任务】检测到违约预约: bookingId={}, userId={}, checkinTime={}, endTime={}",
                booking.getId(), booking.getUserId(), booking.getCheckinTime(), booking.getEndTime());

        booking.setStatus(BREACH_STATUS);
        booking.setCancelTime(LocalDateTime.now());
        bookingMapper.updateById(booking);

        LambdaUpdateWrapper<PaymentEntity> pw = new LambdaUpdateWrapper<>();
        pw.eq(PaymentEntity::getBookingId, booking.getId())
                .eq(PaymentEntity::getStatus, 1)
                .set(PaymentEntity::getStatus, 3);
        paymentMapper.update(null, pw);
        log.info("【违约检查任务】押金已扣除: bookingId={}", booking.getId());

        LambdaUpdateWrapper<TimeSlotEntity> tw = new LambdaUpdateWrapper<>();
        tw.eq(TimeSlotEntity::getBookingId, booking.getId())
                .set(TimeSlotEntity::getStatus, 0)
                .set(TimeSlotEntity::getBookingId, null);
        timeSlotMapper.update(null, tw);

        if (booking.getUserId() != null) {
            UserEntity user = userMapper.selectById(booking.getUserId());
            if (user != null) {
                int currentViolation = user.getViolationCount() != null ? user.getViolationCount() : 0;
                LambdaUpdateWrapper<UserEntity> uw = new LambdaUpdateWrapper<>();
                uw.eq(UserEntity::getId, user.getId())
                        .set(UserEntity::getViolationCount, currentViolation + 1);
                userMapper.update(null, uw);
                log.info("【违约检查任务】用户违约次数+1: userId={}, 当前违约次数={}",
                        user.getId(), currentViolation + 1);
            }
        }
    }
}