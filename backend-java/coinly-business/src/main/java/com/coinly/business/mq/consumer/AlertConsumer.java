package com.coinly.business.mq.consumer;

import com.coinly.business.budget.entity.BudgetAlertEntity;
import com.coinly.business.budget.mapper.BudgetAlertMapper;
import com.coinly.business.mq.dto.AlertMessage;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * V9.1: 预算预警消费者。
 * 预算超阈值时异步记录预警到 biz_budget_alert 表。
 */
@Component
public class AlertConsumer {

    private static final Logger log = LoggerFactory.getLogger(AlertConsumer.class);

    private final BudgetAlertMapper budgetAlertMapper;

    public AlertConsumer(BudgetAlertMapper budgetAlertMapper) {
        this.budgetAlertMapper = budgetAlertMapper;
    }

    @RabbitListener(queues = "coinly.alert.queue")
    public void handleAlert(AlertMessage message, Channel channel,
                            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            log.warn("[预警消费者] 收到消息: userId={}, category={}, budget=¥{}, used=¥{}, usage={}%, level={}",
                    message.getUserId(),
                    message.getCategoryName(),
                    message.getBudgetAmount(),
                    message.getUsedAmount(),
                    message.getPercentage(),
                    message.getLevel());

            BudgetAlertEntity alert = new BudgetAlertEntity();
            alert.setUserId(message.getUserId());
            alert.setBudgetId(message.getBudgetId());
            alert.setCategoryId(message.getCategoryId());
            alert.setCategoryName(message.getCategoryName());
            alert.setBudgetMonth(message.getMonth());
            alert.setBudgetAmount(message.getBudgetAmount());
            alert.setUsedAmount(message.getUsedAmount());
            alert.setPercentage(message.getPercentage());
            alert.setAlertLevel(message.getLevel());
            alert.setIsRead(0);
            alert.setCreateTime(LocalDateTime.now());
            alert.setUpdateTime(LocalDateTime.now());

            budgetAlertMapper.insert(alert);

            log.warn("[预警消费者] 预警已落库: userId={}, level={}, category={}",
                    message.getUserId(), message.getLevel(), message.getCategoryName());

            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("[预警消费者] 处理失败: {}", e.getMessage(), e);
            channel.basicNack(tag, false, false);
        }
    }
}
