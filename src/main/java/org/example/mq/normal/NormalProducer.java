package org.example.mq.normal;

import lombok.extern.slf4j.Slf4j;
import org.example.mq.RabbitMQConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 普通队列消息发送者
 * 演示基本的RabbitMQ消息发送，支持消息持久化
 *
 * @author RabbitMQ Demo
 * @version 2.0.0
 */
@Slf4j
@Component
public class NormalProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送对象消息（持久化，自动转换为JSON）
     *
     * @param object 消息对象
     */
    public void sendObjectMessage(Object object) {
        String messageId = UUID.randomUUID().toString();

        log.info("【普通队列】发送持久化对象消息 - ID: {}, 对象: {}", messageId, object);

        // 使用JSON转换器自动序列化，消息默认持久化
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NORMAL_EXCHANGE,
                RabbitMQConfig.NORMAL_ROUTING_KEY,
                object,
                message -> {
                    message.getMessageProperties().setMessageId(messageId);
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                }
        );
        log.info("【普通队列】✅ 持久化对象消息发送成功");
    }
}
