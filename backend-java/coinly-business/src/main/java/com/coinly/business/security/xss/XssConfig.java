package com.coinly.business.security.xss;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * V9: Jackson XSS 防护配置。
 * 注册自定义反序列化器，对所有 JSON String 字段做 HTML 转义。
 * 使用 addModules 保留 Spring Boot 自动注册的 JavaTimeModule 等模块。
 */
@Configuration
public class XssConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer xssCustomizer() {
        return builder -> {
            SimpleModule xssModule = new SimpleModule("XssModule");
            xssModule.addDeserializer(String.class, new XssStringDeserializer());
            builder.modulesToInstall(xssModule);
        };
    }
}
