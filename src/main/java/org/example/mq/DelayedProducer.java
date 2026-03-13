package org.example.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 延迟队列消息发送者
 * 演示延迟消息的发送（需要安装延迟插件），支持消息持久化
 *
 * @author RabbitMQ Demo
 * @version 2.0.0
 */
@Slf4j
@Component
public class DelayedProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送延迟消息（持久化）
     *
     * @param message     消息内容
     * @param delayMillis 延迟时间（毫秒）
     */
    public void sendDelayedMessage(String message, long delayMillis) {
        String messageId = UUID.randomUUID().toString();

        log.info("【延迟队列】发送持久化延迟消息 - ID: {}, 内容: {}, 延迟: {}ms",
                messageId, message, delayMillis);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DELAYED_EXCHANGE,     // 延迟交换机
                RabbitMQConfig.DELAYED_ROUTING_KEY,  // 路由键
                message,
                msg -> {
                    // 设置消息ID
                    msg.getMessageProperties().setMessageId(messageId);
                    // 设置持久化
                    msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    // 设置延迟时间（单位：毫秒）
                    msg.getMessageProperties().setDelay((int) delayMillis);
                    return msg;
                }
        );

        log.info("【延迟队列】✅ 持久化延迟消息将在 {} 毫秒后被消费", delayMillis);
    }

    /**
     * 发送延迟对象消息（持久化）
     *
     * @param object      消息对象
     * @param delayMillis 延迟时间（毫秒）
     */
    public void sendDelayedObjectMessage(Object object, long delayMillis) {
        String messageId = UUID.randomUUID().toString();

        log.info("【延迟队列】发送持久化延迟对象消息 - ID: {}, 对象: {}, 延迟: {}ms",
                messageId, object, delayMillis);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.DELAYED_EXCHANGE,
                RabbitMQConfig.DELAYED_ROUTING_KEY,
                object,
                msg -> {
                    msg.getMessageProperties().setMessageId(messageId);
                    msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    msg.getMessageProperties().setDelay((int) delayMillis);
                    return msg;
                }
        );

        log.info("【延迟队列】✅ 持久化对象延迟消息将在 {} 毫秒后被消费", delayMillis);
    }

    /**
     * 发送订单超时取消示例（持久化）
     *
     * @param orderId    订单ID
     * @param delayMinutes 延迟分钟数
     */
    public void sendOrderCancelMessage(String orderId, int delayMinutes) {
        String message = String.format("订单 %s 超时未支付，自动取消", orderId);
        long delayMillis = delayMinutes * 60 * 1000L;

        log.info("【延迟队列】订单超时取消 - 订单ID: {}, 超时时间: {} 分钟",
                orderId, delayMinutes);

        sendDelayedMessage(message, delayMillis);
    }

    /**
     * 演示多个不同延迟时间的消息（持久化）
     */
    public void sendMultipleDelayedMessages() {
        log.info("【延迟队列】演示：发送多条不同延迟时间的持久化消息");

        // 立即执行
        sendDelayedMessage("立即执行的任务", 0);

        // 5秒后执行
        sendDelayedMessage("5秒后执行的任务", 5000);

        // 10秒后执行
        sendDelayedMessage("10秒后执行的任务", 10000);

        // 30秒后执行
        sendDelayedMessage("30秒后执行的任务", 30000);

        log.info("【延迟队列】✅ 所有持久化延迟消息已发送，将按延迟时间依次到达");
    }
}
