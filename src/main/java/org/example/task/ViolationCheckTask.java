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

    @Transactional(rollbackFor = Exception.class)
    public boolean checkAndProcessBreach(Long bookingId) {
        log.info("【违约检查任务】开始执行, bookingId={}", bookingId);

        BookingEntity booking = bookingMapper.selectById(bookingId);
        if (booking == null) {
            log.warn("【违约检查任务】预约不存在, bookingId={}", bookingId);
            return false;
        }

        if (booking.getStatus() == null || booking.getStatus() != 1) {
            log.warn("【违约检查任务】预约状态不是已预约, bookingId={}, status={}", bookingId, booking.getStatus());
            return false;
        }

        if (booking.getEndTime() == null) {
            log.warn("【违约检查任务】预约结束时间为空, bookingId={}", bookingId);
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkinTime = booking.getCheckinTime();
        LocalDateTime graceEndTime = booking.getEndTime().plusHours(1);

        boolean isBreach = (checkinTime == null && now.isAfter(graceEndTime))
                || (checkinTime != null && checkinTime.isAfter(graceEndTime));

        if (isBreach) {
            processBreach(booking);
            log.info("【违约检查任务】已处理违约, bookingId={}", bookingId);
            return true;
        } else {
            log.info("【违约检查任务】不构成违约, bookingId={}", bookingId);
            return false;
        }
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
                int currentCreditScore = user.getCreditScore() != null ? user.getCreditScore() : 100;
                int newCreditScore = Math.max(0, currentCreditScore - 10);

                LambdaUpdateWrapper<UserEntity> uw = new LambdaUpdateWrapper<>();
                uw.eq(UserEntity::getId, user.getId())
                        .set(UserEntity::getViolationCount, currentViolation + 1)
                        .set(UserEntity::getCreditScore, newCreditScore);

                if (newCreditScore <= 0) {
                    uw.set(UserEntity::getUserStatus, 0);
                    log.warn("【违约检查任务】用户信誉积分归零，已被禁用: userId={}", user.getId());
                }

                userMapper.update(null, uw);

                log.info("【违约检查任务】用户违约+1, 积分-10: userId={}, 违约次数={}, 信誉积分={}",
                        user.getId(), currentViolation + 1, newCreditScore);
            }
        }
    }
}