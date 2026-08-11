package com.coinly.business.mq.producer;

import com.coinly.business.mq.config.RabbitMQConfig;
import com.coinly.business.mq.dto.AlertMessage;
import com.coinly.business.mq.dto.ReportMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 消息生产者。
 * 记账后发报表消息，预算超支发预警消息。
 */
@Component
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public MessageProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendReportMessage(ReportMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.REPORT_ROUTING_KEY,
                message
        );
    }

    public void sendAlertMessage(AlertMessage message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ALERT_ROUTING_KEY,
                message
        );
    }
}
