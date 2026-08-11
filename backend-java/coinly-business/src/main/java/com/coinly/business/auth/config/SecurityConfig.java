package com.coinly.business.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置。
 *
 * @see com.coinly.business.auth.interceptor.JwtInterceptor
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 密码加密器 Bean。
     * 注册和修改密码时通过构造函数注入使用，避免在每个 Controller 里 new。
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring Security 过滤链配置。
     * 第一版所有请求都 permitAll，实际鉴权交给 JWT 拦截器处理。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/v1/auth/register", "/v1/auth/login").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().permitAll());
        return http.build();
    }
}