package org.example.mq.delayed;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.activity.apply.ApplyPayEntity;
import org.example.entity.failed.delayed.DelayedMessageEntity;
import org.example.service.ApplyLockService;
import org.example.service.activity.apply.ApplyPayService;
import org.example.service.failed.delayed.DelayedMessageService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 延迟队列消费者
 * 演示延迟消息的消费、重试机制、死信队列
 *
 * @author RabbitMQ Demo
 * @version 2.0.0
 */
@Slf4j
@Component
public class DelayedConsumer {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 重试次数计数器
    private static final java.util.concurrent.ConcurrentHashMap<String, Integer> RETRY_COUNT_MAP =
            new java.util.concurrent.ConcurrentHashMap<>();

    private static final int MAX_RETRY_COUNT = 3;
    @Resource
    private ApplyPayService applyPayService;
    @Resource
    private DelayedMessageService delayedMessageService;
    @Resource
    private ApplyLockService applyLockService;

    /**
     * 监听延迟队列
     * 注意：这里监听的队列本身并不"延迟"
     * 延迟功能是通过延迟交换机实现的
     * 消息会先存储在交换机中，到达指定时间后才被投递到这个队列
     *
     * @param message 消息对象
     * @param channel RabbitMQ通道
     * @throws IOException
     */
    @RabbitListener(queues = "${spring.rabbitmq.config.delayed-queue:delayed.queue}")
    public void consumeDelayedMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String messageId = message.getMessageProperties().getMessageId();
        String messageBody = new String(message.getBody());

        try {
            String currentTime = LocalDateTime.now().format(FORMATTER);

            log.info("");
            log.info("╔════════════════════════════════════════════════════════════════╗");
            log.info("║           【延迟队列消费者】收到延迟消息                        ║");
            log.info("╠════════════════════════════════════════════════════════════════╣");
            log.info("║  消息ID:      {}", messageId);
            log.info("║  当前时间:    {}", currentTime);
            log.info("║  消息内容:    {}", messageBody);
            log.info("╚════════════════════════════════════════════════════════════════╝");
            log.info("");

            // 获取重试次数
            int retryCount = getRetryCount(message);

            // 检查重试次数
            if (retryCount >= MAX_RETRY_COUNT) {
                log.warn("【延迟队列消费者】⚠️ 消息重试次数已达上限，将进入死信队列");
                channel.basicNack(deliveryTag, false, false);
                RETRY_COUNT_MAP.remove(messageId);
                return;
            }

            // 处理延迟消息的业务逻辑
            processDelayedMessageWithRetry(messageBody, retryCount, messageId);

            // 处理成功，确认消息
            channel.basicAck(deliveryTag, false);
            log.info("【延迟队列消费者】✅ 延迟消息处理完成并确认");

            // 清除重试计数
            RETRY_COUNT_MAP.remove(messageId);

        } catch (Exception e) {
            log.error("【延迟队列消费者】❌ 消息处理失败", e);

            // 增加重试次数
            int currentRetry = incrementRetryCount(message);
            log.warn("【延迟队列消费者】⚠️ 消息处理失败，当前重试次数: {}/{}", currentRetry, MAX_RETRY_COUNT);

            if (currentRetry >= MAX_RETRY_COUNT) {
                // 超过最大重试次数，进入死信队列
                log.error("【延迟队列消费者】❌ 超过最大重试次数，消息进入死信队列");
                channel.basicNack(deliveryTag, false, false);
                RETRY_COUNT_MAP.remove(messageId);
            } else {
                // 重新入队
                channel.basicNack(deliveryTag, false, true);
            }
        }
    }

    /**
     * 处理延迟消息的业务逻辑（带重试）
     *
     * @param messageBody 消息内容
     * @param retryCount  当前重试次数
     * @param messageId   消息ID
     */
    private void processDelayedMessageWithRetry(String messageBody, int retryCount, String messageId) {
        log.info("【延迟队列】开始处理延迟消息，第 {} 次尝试", retryCount + 1);
        // 根据消息内容执行不同的业务逻辑
        handleOrderTimeoutWithRetry(messageBody, retryCount);
        log.info("【延迟队列】✅ 延迟消息处理成功");
    }

    /**
     * 处理订单超时取消（带重试）
     */
    private void handleOrderTimeoutWithRetry(String message, int retryCount) {
        log.info("【业务处理】执行订单超时取消逻辑: {},重试次数：{}", message, retryCount);
        ApplyPayEntity pay = applyPayService.getOne(
                Wrappers.<ApplyPayEntity>lambdaQuery()
                        .eq(ApplyPayEntity::getMerOrderId, message)
        );
        if (pay == null) {
            log.error("【订单取消】订单号不存在");
            return;
        }
        if (pay.getPayStatus() == 1) {
            log.info("【订单取消】订单已支付");
            return;
        }
        // 增强：如果使用第三方支付，需要额外查询第三方支付状态判断
        // 并建议调用第三方关闭订单接口
        // 释放锁定报名
        applyLockService.increaseApply(pay.getApplyId(), 1L);
        log.info("【订单取消】✅ 订单超时取消成功:{}", message);
    }

    /**
     * 获取消息的重试次数
     */
    private int getRetryCount(Message message) {
        String messageId = message.getMessageProperties().getMessageId();
        if (messageId == null) {
            messageId = String.valueOf(message.getMessageProperties().getDeliveryTag());
        }
        return RETRY_COUNT_MAP.getOrDefault(messageId, 0);
    }

    /**
     * 增加重试次数
     */
    private int incrementRetryCount(Message message) {
        String messageId = message.getMessageProperties().getMessageId();
        if (messageId == null) {
            messageId = String.valueOf(message.getMessageProperties().getDeliveryTag());
        }
        return RETRY_COUNT_MAP.merge(messageId, 1, Integer::sum);
    }

    /**
     * 延迟死信队列消费者
     * 处理失败的延迟消息
     */
    @RabbitListener(queues = "${spring.rabbitmq.config.delayed-dlq:delayed.queue.dlq}")
    public void consumeDelayedDeadLetterMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String messageId = message.getMessageProperties().getMessageId();
        String messageBody = new String(message.getBody());

        try {
            log.error("");
            log.error("╔════════════════════════════════════════════════════════════════╗");
            log.error("║        【延迟死信队列消费者】收到失败延迟消息                    ║");
            log.error("╠════════════════════════════════════════════════════════════════╣");
            log.error("║  消息ID:      {}", messageId);
            log.error("║  原始内容:    {}", messageBody);
            log.error("║  失败原因:    延迟消息处理失败，重试次数已达上限");
            log.error("║  建议操作:    1. 检查延迟时间设置是否合理");
            log.error("║              2. 检查业务依赖服务是否正常");
            log.error("║              3. 考虑增加重试次数或人工处理");
            log.error("╚════════════════════════════════════════════════════════════════╝");
            log.error("");

            // 死信队列处理逻辑
            saveFailedDelayedMessage(messageId, messageBody);

            // 确认死信消息
            channel.basicAck(deliveryTag, false);
            log.error("【延迟死信队列消费者】✅ 死信消息已处理");

        } catch (Exception e) {
            log.error("【延迟死信队列消费者】❌ 处理失败", e);
            channel.basicAck(deliveryTag, false);
        }
    }

    /**
     * 保存失败的延迟消息
     */
    private void saveFailedDelayedMessage(String messageId, String messageBody) {
        // 实际应用中，应该保存到数据库或发送告警
        log.warn("【延迟死信处理】保存失败消息 - ID: {}, 内容: {}", messageId, messageBody);

        DelayedMessageEntity failedMessage = new DelayedMessageEntity();
        failedMessage.setMessageId(messageId);
        failedMessage.setContent(messageBody);
        failedMessage.setFailedTime(LocalDateTime.now());
        delayedMessageService.save(failedMessage);

    }
}
