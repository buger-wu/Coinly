package com.coinly.business.security.xss;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;

import java.io.IOException;

/**
 * V9: Jackson 字符串反序列化器，对字符串做 HTML 转义防止 XSS。
 * 仅转义危险字符 < > & "，保留中文等其他字符不变。
 * 在 ObjectMapper 配置中注册，所有 JSON String 字段自动过滤。
 */
public class XssStringDeserializer extends StringDeserializer {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = super.deserialize(p, ctxt);
        if (value == null || value.isEmpty()) {
            return value;
        }
        return escapeHtml(value);
    }

    /**
     * 仅转义 HTML 危险字符，避免中文被误转义。
     */
    private String escapeHtml(String value) {
        StringBuilder sb = new StringBuilder(value.length());
        for (char c : value.toCharArray()) {
            switch (c) {
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '&' -> sb.append("&amp;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#x27;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
