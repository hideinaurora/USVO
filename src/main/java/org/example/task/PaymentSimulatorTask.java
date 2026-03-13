package org.example.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.PaymentCallbackDTO;
import org.example.entity.activity.apply.ApplyPayEntity;
import org.example.mq.normal.NormalProducer;
import org.example.service.activity.apply.ApplyPayService;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 支付模拟定时任务
 * 定时查询待支付订单，模拟支付回调通知
 *
 * @author ckd
 * @since 2026-03-13
 */
@Configuration
@EnableScheduling
@Slf4j
public class PaymentSimulatorTask {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private ApplyPayService applyPayService;

    @Resource
    private NormalProducer normalProducer;

    /**
     * 定时模拟支付回调
     * 每10分钟执行一次，查询2笔待支付订单并模拟支付成功
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void simulatePaymentCallback() {
        try {
            log.info("========================================");
            log.info("【支付模拟任务】开始执行");
            log.info("执行时间: {}", LocalDateTime.now().format(DATE_TIME_FORMATTER));
            log.info("========================================");

            // 1. 查询待支付订单（限制2笔）
            QueryWrapper<ApplyPayEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("pay_status", 0); // 未支付
            queryWrapper.orderByAsc("gmt_create"); // 按创建时间升序，处理最早的订单
            queryWrapper.last("LIMIT 2"); // 限制2笔

            List<ApplyPayEntity> unpaidOrders = applyPayService.list(queryWrapper);

            if (unpaidOrders == null || unpaidOrders.isEmpty()) {
                log.info("【支付模拟任务】没有待支付订单，跳过本次处理");
                return;
            }

            log.info("【支付模拟任务】找到 {} 笔待支付订单", unpaidOrders.size());

            // 2. 为每笔订单生成模拟支付回调通知
            for (ApplyPayEntity order : unpaidOrders) {
                try {
                    // 生成支付回调DTO
                    PaymentCallbackDTO callbackDTO = generatePaymentCallback(order);

                    log.info("【支付模拟任务】生成模拟支付回调：订单ID={}, 商户订单号={}, 金额={}分",
                            order.getId(), order.getMerOrderId(), order.getTotalAmount());

                    // 发送到普通队列
                    normalProducer.sendObjectMessage(callbackDTO);

                    log.info("【支付模拟任务】✅ 订单ID={} 的模拟支付回调已发送到队列", order.getId());

                } catch (Exception e) {
                    log.error("【支付模拟任务】❌ 处理订单ID={} 失败", order.getId(), e);
                }
            }

            log.info("========================================");
            log.info("【支付模拟任务】执行完成，共处理 {} 笔订单", unpaidOrders.size());
            log.info("========================================");

        } catch (Exception e) {
            log.error("【支付模拟任务】❌ 执行失败", e);
        }
    }

    /**
     * 生成模拟支付回调数据
     *
     * @param order 支付订单
     * @return 支付回调DTO
     */
    private PaymentCallbackDTO generatePaymentCallback(ApplyPayEntity order) {
        PaymentCallbackDTO callbackDTO = new PaymentCallbackDTO();

        // 商户订单号
        callbackDTO.setOutTradeNo(order.getMerOrderId());

        // 支付平台交易号（生成模拟交易号）
        String simulatedTradeNo = "ALIPAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        callbackDTO.setTradeNo(simulatedTradeNo);

        // 交易状态：交易支付成功
        callbackDTO.setTradeStatus("TRADE_SUCCESS");

        // 交易金额（将分转换为元）
        String totalAmountYuan = String.format("%.2f", order.getTotalAmount() / 100.0);
        callbackDTO.setTotalAmount(totalAmountYuan);

        // 支付时间（当前时间）
        callbackDTO.setGmtPayment(LocalDateTime.now().format(DATE_TIME_FORMATTER));

        // 买家支付账号（模拟）
        callbackDTO.setBuyerLogonId("user_" + order.getUserId() + "@example.com");

        // 应用ID（模拟）
        callbackDTO.setAppId("2021001234567890");

        // 通知时间（当前时间）
        callbackDTO.setNotifyTime(LocalDateTime.now().format(DATE_TIME_FORMATTER));

        // 通知类型
        callbackDTO.setNotifyType("trade_status_sync");

        // 签名类型
        callbackDTO.setSignType("RSA2");

        return callbackDTO;
    }
}
