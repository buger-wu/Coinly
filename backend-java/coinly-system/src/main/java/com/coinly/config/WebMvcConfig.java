package com.coinly.config;

import com.coinly.business.auth.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置。
 * 负责注册 {@link JwtInterceptor}，配置拦截路径和排除路径。
 *
 * @see JwtInterceptor
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 注册 JwtInterceptor 为 Spring Bean。
     * 使用 @Bean 方式注册，保证拦截器是单例。
     */
    @Bean
    public JwtInterceptor jwtInterceptor() {
        return new JwtInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor())
                .addPathPatterns("/v1/**")
                .excludePathPatterns("/v1/auth/register", "/v1/auth/login");
    }

    //TODO nginx
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}