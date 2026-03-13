package org.example.mq.normal;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.failed.delayed.DelayedMessageEntity;
import org.example.service.failed.delayed.DelayedMessageService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 普通队列消费者
 * 演示基本的消息消费、手动确认、消息重试、死信队列
 *
 * @author RabbitMQ Demo
 * @version 2.0.0
 */
@Slf4j
@Component
public class NormalConsumer {

    @Resource
    private DelayedMessageService delayedMessageService;

    // 重试次数计数器（生产环境应使用Redis等存储）
    private static final java.util.concurrent.ConcurrentHashMap<String, Integer> RETRY_COUNT_MAP =
            new java.util.concurrent.ConcurrentHashMap<>();

    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 监听普通队列
     * queues: 指定监听的队列名称
     * containerFactory: 使用RabbitMQ容器工厂
     *
     * 消息处理流程：
     * 1. 收到消息
     * 2. 检查重试次数
     * 3. 业务处理
     * 4. 成功则确认，失败则重试或进入死信队列
     *
     * @param message 消息对象
     * @param channel RabbitMQ通道
     * @throws IOException
     */
    @RabbitListener(queues = "${spring.rabbitmq.config.normal-queue:normal.queue}")
    public void consumeMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String messageId = message.getMessageProperties().getMessageId();
        String messageBody = new String(message.getBody());

        try {
            log.info("========================================");
            log.info("【普通队列消费者】收到消息");
            log.info("消息ID: {}", messageId);
            log.info("消息内容: {}", messageBody);
            log.info("消息时间: {}", message.getMessageProperties().getTimestamp());
            log.info("DeliveryTag: {}", deliveryTag);
            log.info("========================================");

            // 获取重试次数
            int retryCount = getRetryCount(message);

            // 检查重试次数
            if (retryCount >= MAX_RETRY_COUNT) {
                log.warn("【普通队列消费者】消息重试次数已达上限({}次)，将进入死信队列", MAX_RETRY_COUNT);
                // 拒绝消息，不重新入队，进入死信队列
                channel.basicNack(deliveryTag, false, false);
                // 清除重试计数
                RETRY_COUNT_MAP.remove(messageId);
                return;
            }

            // 模拟业务处理（带重试）
            processMessageWithRetry(messageBody, retryCount, messageId);

            // 处理成功，确认消息
            channel.basicAck(deliveryTag, false);
            log.info("【普通队列消费者】✅ 消息处理完成，已确认");

            // 清除重试计数
            RETRY_COUNT_MAP.remove(messageId);

        } catch (Exception e) {
            log.error("【普通队列消费者】❌ 消息处理失败", e);

            // 增加重试次数
            int currentRetry = incrementRetryCount(message);
            log.warn("【普通队列消费者】⚠️ 消息处理失败，当前重试次数: {}/{}", currentRetry, MAX_RETRY_COUNT);

            if (currentRetry >= MAX_RETRY_COUNT) {
                // 超过最大重试次数，进入死信队列
                log.error("【普通队列消费者】❌ 超过最大重试次数，消息进入死信队列");
                channel.basicNack(deliveryTag, false, false);
                RETRY_COUNT_MAP.remove(messageId);
            } else {
                // 重新入队，等待下次消费
                channel.basicNack(deliveryTag, false, true);
            }
        }
    }

    /**
     * 带重试的业务处理逻辑
     *
     * @param messageBody 消息内容
     * @param retryCount 当前重试次数
     * @param messageId 消息ID
     */
    private void processMessageWithRetry(String messageBody, int retryCount, String messageId) {
        log.info("【业务处理】开始处理消息，当前是第 {} 次尝试", retryCount + 1);

        log.info("【业务处理】✅ 处理成功，消息内容: {}", messageBody);
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
     * 死信队列消费者
     * 处理失败的消息，可以进行告警、记录、人工干预等
     */
    @RabbitListener(queues = "${spring.rabbitmq.config.normal-dlq:normal.queue.dlq}")
    public void consumeDeadLetterMessage(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String messageId = message.getMessageProperties().getMessageId();
        String messageBody = new String(message.getBody());

        try {
            log.error("");
            log.error("╔════════════════════════════════════════════════════════════════╗");
            log.error("║           【死信队列消费者】收到失败消息                          ║");
            log.error("╠════════════════════════════════════════════════════════════════╣");
            log.error("║  消息ID:      {}", messageId);
            log.error("║  原始内容:    {}", messageBody);
            log.error("║  失败原因:    消息处理失败，重试次数已达上限");
            log.error("║  建议操作:    1. 检查消息格式是否正确");
            log.error("║              2. 检查业务逻辑是否有异常");
            log.error("║              3. 确认依赖服务是否正常");
            log.error("╚════════════════════════════════════════════════════════════════╝");
            log.error("");

            // 死信队列处理逻辑：
            // 1. 记录到数据库
            // 2. 发送告警通知
            // 3. 记录日志文件
            // 4. 人工介入处理

            // 这里只是简单示例，实际应该根据业务需求处理
            saveFailedMessage(messageId, messageBody);

            // 确认死信消息
            channel.basicAck(deliveryTag, false);
            log.error("【死信队列消费者】✅ 死信消息已处理并确认");

        } catch (Exception e) {
            log.error("【死信队列消费者】❌ 处理死信消息失败", e);
            // 死信队列处理失败，记录日志但不重试
            channel.basicAck(deliveryTag, false);
        }
    }

    /**
     * 保存失败的消息到持久化存储
     */
    private void saveFailedMessage(String messageId, String messageBody) {
        // 实际应用中，应该保存到数据库或文件
        log.warn("【死信处理】保存失败消息 - ID: {}, 内容: {}", messageId, messageBody);

        DelayedMessageEntity failedMessage = new DelayedMessageEntity();
        failedMessage.setMessageId(messageId);
        failedMessage.setContent(messageBody);
        failedMessage.setFailedTime(LocalDateTime.now());
        delayedMessageService.save(failedMessage);
    }
}
