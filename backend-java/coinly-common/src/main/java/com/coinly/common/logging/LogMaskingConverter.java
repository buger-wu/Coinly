package com.coinly.common.logging;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.util.regex.Pattern;

/**
 * V9: 日志脱敏转换器。
 * 在 logback.xml 中通过 %msgMask 引用，对日志消息中的敏感字段做脱敏。
 *
 * 脱敏规则：
 * - password=xxx -> password=***
 * - "password":"xxx" -> "password":"***"
 * - token=eyJhbG... -> token=eyJh****
 * - Authorization: Bearer xxx -> Authorization: Bearer ***
 */
public class LogMaskingConverter extends MessageConverter {

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "(?i)(password[\"']?\\s*[:=]\\s*[\"']?)([^\"'\\s,}]+)");
    private static final Pattern TOKEN_PATTERN = Pattern.compile(
            "(?i)(token[\"']?\\s*[:=]\\s*[\"']?)([A-Za-z0-9._-]{8,})");
    private static final Pattern AUTHORIZATION_PATTERN = Pattern.compile(
            "(?i)(authorization[\"']?\\s*[:=]\\s*[\"']?Bearer\\s+)([A-Za-z0-9._-]+)");

    @Override
    public String convert(ILoggingEvent event) {
        String msg = event.getFormattedMessage();
        if (msg == null || msg.isEmpty()) {
            return msg;
        }
        msg = PASSWORD_PATTERN.matcher(msg).replaceAll("$1***");
        msg = TOKEN_PATTERN.matcher(msg).replaceAll("$1****");
        msg = AUTHORIZATION_PATTERN.matcher(msg).replaceAll("$1***");
        return msg;
    }
}
