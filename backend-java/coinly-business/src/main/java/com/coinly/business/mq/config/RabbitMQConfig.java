package com.coinly.business.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置。
 * 定义交换机、队列、绑定关系和消息转换器。
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "coinly.exchange";
    public static final String REPORT_QUEUE = "coinly.report.queue";
    public static final String ALERT_QUEUE = "coinly.alert.queue";
    public static final String REPORT_ROUTING_KEY = "coinly.report";
    public static final String ALERT_ROUTING_KEY = "coinly.alert";

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public TopicExchange coinlyExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue reportQueue() {
        return QueueBuilder.durable(REPORT_QUEUE).build();
    }

    @Bean
    public Queue alertQueue() {
        return QueueBuilder.durable(ALERT_QUEUE).build();
    }

    @Bean
    public Binding reportBinding() {
        return BindingBuilder.bind(reportQueue()).to(coinlyExchange()).with(REPORT_ROUTING_KEY);
    }

    @Bean
    public Binding alertBinding() {
        return BindingBuilder.bind(alertQueue()).to(coinlyExchange()).with(ALERT_ROUTING_KEY);
    }
}
