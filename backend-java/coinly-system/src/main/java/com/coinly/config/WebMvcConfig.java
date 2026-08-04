package com.coinly.config;

import com.coinly.business.auth.interceptor.JwtInterceptor;
import com.coinly.common.util.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置。
 * 负责注册 {@link JwtInterceptor}，配置拦截路径和排除路径。
 * CORS 允许的源从 application.yml 配置读取。
 *
 * @see JwtInterceptor
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtUtils jwtUtils;

    @Value("${cors.allowed-origins:http://localhost:5173}")
    private String[] allowedOrigins;

    public WebMvcConfig(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtInterceptor(jwtUtils))
                .addPathPatterns("/v1/**")
                .excludePathPatterns("/v1/auth/register", "/v1/auth/login");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
