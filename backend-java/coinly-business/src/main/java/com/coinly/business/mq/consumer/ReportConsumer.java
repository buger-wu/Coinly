package com.coinly.business.mq.consumer;

import com.coinly.business.mq.dto.ReportMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import com.rabbitmq.client.Channel;

import java.io.IOException;

/**
 * 月度报表消费者。
 * 记账后异步生成当月快照，避免阻塞主流程。
 */
@Component
public class ReportConsumer {

    private static final Logger log = LoggerFactory.getLogger(ReportConsumer.class);

    @RabbitListener(queues = "coinly.report.queue")
    public void handleReport(ReportMessage message, Channel channel,
                             @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            log.info("[报表消费者] 收到消息: userId={}, bookId={}, month={}, txId={}",
                    message.getUserId(), message.getBookId(), message.getMonth(), message.getTransactionId());

            // TODO: 生成月度快照（查询当月汇总数据，写入 biz_monthly_snapshot 表）
            // 当前仅记录日志，模拟异步处理
            log.info("[报表消费者] 月度快照生成完成: userId={}, month={}", message.getUserId(), message.getMonth());

            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("[报表消费者] 处理失败: {}", e.getMessage(), e);
            // requeue=false，失败后进入死信队列（如果有配置）
            channel.basicNack(tag, false, false);
        }
    }
}
