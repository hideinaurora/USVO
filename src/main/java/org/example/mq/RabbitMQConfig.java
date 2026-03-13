package org.example.mq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ配置类
 * 配置队列、交换机、绑定关系、死信队列、消息持久化
 *
 * @author RabbitMQ Demo
 * @version 2.0.0
 */
@Configuration
public class RabbitMQConfig {

    // ==================== 队列名称常量 ====================
    public static final String NORMAL_QUEUE = "normal.queue";
    public static final String NORMAL_DLQ = "normal.queue.dlq";
    public static final String NORMAL_EXCHANGE = "normal.exchange";
    public static final String NORMAL_DLX = "normal.dlx";
    public static final String NORMAL_ROUTING_KEY = "normal.routing.key";
    public static final String NORMAL_DLQ_ROUTING_KEY = "normal.dlq.routing.key";

    public static final String DELAYED_QUEUE = "delayed.queue";
    public static final String DELAYED_DLQ = "delayed.queue.dlq";
    public static final String DELAYED_EXCHANGE = "delayed.exchange";
    public static final String DELAYED_DLX = "delayed.dlx";
    public static final String DELAYED_ROUTING_KEY = "delayed.routing.key";
    public static final String DELAYED_DLQ_ROUTING_KEY = "delayed.dlq.routing.key";

    // ==================== 普通队列配置（带死信队列） ====================

    /**
     * 普通死信队列
     * 用于接收普通队列处理失败的消息
     *
     * durable: true - 队列持久化，RabbitMQ重启后队列仍然存在
     */
    @Bean
    public Queue normalDeadLetterQueue() {
        return QueueBuilder
                .durable(NORMAL_DLQ)
                .build();
    }

    /**
     * 普通死信交换机
     */
    @Bean
    public DirectExchange normalDeadLetterExchange() {
        return new DirectExchange(NORMAL_DLX, true, false);
    }

    /**
     * 死信队列绑定
     */
    @Bean
    public Binding normalDeadLetterBinding() {
        return BindingBuilder.bind(normalDeadLetterQueue())
                .to(normalDeadLetterExchange())
                .with(NORMAL_DLQ_ROUTING_KEY);
    }

    /**
     * 普通业务队列（配置死信队列）
     *
     * 关键配置：
     * - durable: true - 队列持久化
     * - x-dead-letter-exchange: 指定死信交换机
     * - x-dead-letter-routing-key: 指定死信路由键
     */
    @Bean
    public Queue normalQueue() {
        Map<String, Object> args = new HashMap<>();
        // 配置死信交换机
        args.put("x-dead-letter-exchange", NORMAL_DLX);
        // 配置死信路由键
        args.put("x-dead-letter-routing-key", NORMAL_DLQ_ROUTING_KEY);
        // 消息过期时间（可选，单位毫秒）
        // args.put("x-message-ttl", 60000);

        return QueueBuilder
                .durable(NORMAL_QUEUE)  // 队列持久化
                .withArguments(args)    // 设置死信队列参数
                .build();
    }

    /**
     * 普通交换机（Direct类型）
     */
    @Bean
    public DirectExchange normalExchange() {
        return new DirectExchange(NORMAL_EXCHANGE, true, false);
    }

    /**
     * 普通队列绑定
     */
    @Bean
    public Binding normalBinding() {
        return BindingBuilder.bind(normalQueue())
                .to(normalExchange())
                .with(NORMAL_ROUTING_KEY);
    }

    // ==================== 延迟队列配置（带死信队列） ====================

    /**
     * 延迟死信队列
     */
    @Bean
    public Queue delayedDeadLetterQueue() {
        return QueueBuilder
                .durable(DELAYED_DLQ)
                .build();
    }

    /**
     * 延迟死信交换机
     */
    @Bean
    public DirectExchange delayedDeadLetterExchange() {
        return new DirectExchange(DELAYED_DLX, true, false);
    }

    /**
     * 延迟死信队列绑定
     */
    @Bean
    public Binding delayedDeadLetterBinding() {
        return BindingBuilder.bind(delayedDeadLetterQueue())
                .to(delayedDeadLetterExchange())
                .with(DELAYED_DLQ_ROUTING_KEY);
    }

    /**
     * 延迟业务队列（配置死信队列）
     */
    @Bean
    public Queue delayedQueue() {
        Map<String, Object> args = new HashMap<>();
        // 配置死信交换机
        args.put("x-dead-letter-exchange", DELAYED_DLX);
        // 配置死信路由键
        args.put("x-dead-letter-routing-key", DELAYED_DLQ_ROUTING_KEY);

        return QueueBuilder
                .durable(DELAYED_QUEUE)
                .withArguments(args)
                .build();
    }

    /**
     * 延迟交换机（x-delayed-message类型）
     */
    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-delayed-type", "direct");

        return new CustomExchange(
                DELAYED_EXCHANGE,
                "x-delayed-message",
                true,
                false,
                args
        );
    }

    /**
     * 延迟队列绑定
     */
    @Bean
    public Binding delayedBinding() {
        return BindingBuilder.bind(delayedQueue())
                .to(delayedExchange())
                .with(DELAYED_ROUTING_KEY)
                .noargs();
    }

    // ==================== 消息持久化配置 ====================

    /**
     * JSON消息转换器
     * 将Java对象自动转换为JSON格式进行传输
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置RabbitTemplate
     *
     * 关键配置：
     * - 使用JSON转换器
     * - 设置消息持久化（deliveryMode=2）
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());

        // 发送确认回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("✅ 消息发送成功 - CorrelationData: " + correlationData);
            } else {
                System.err.println("❌ 消息发送失败 - Cause: " + cause);
            }
        });

        // 返回确认回调（消息无法路由时）
        rabbitTemplate.setReturnsCallback(returned -> {
            System.err.println("⚠️ 消息无法路由 - Message: " + returned.getMessage());
            System.err.println("   ReplyCode: " + returned.getReplyCode());
            System.err.println("   ReplyText: " + returned.getReplyText());
            System.err.println("   Exchange: " + returned.getExchange());
            System.err.println("   RoutingKey: " + returned.getRoutingKey());
        });

        return rabbitTemplate;
    }
}
