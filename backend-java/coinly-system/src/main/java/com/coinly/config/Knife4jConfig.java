package com.coinly.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Coinly API 文档")
                        .version("v1.0.0")
                        .description("Coinly 轻量级个人记账应用接口文档")
                        .contact(new Contact().name("Coinly Team")));
    }
}
