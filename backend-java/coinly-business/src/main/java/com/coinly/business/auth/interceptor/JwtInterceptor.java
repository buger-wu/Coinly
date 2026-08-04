package com.coinly.business.auth.interceptor;

import com.coinly.common.context.UserContext;
import com.coinly.common.exception.BusinessException;
import com.coinly.common.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 鉴权拦截器。
 * @see JwtUtils
 * @see UserContext
 */
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    public JwtInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new BusinessException(401, "未授权，请先登录");
        }

        // 去掉 "Bearer " 前缀，获取纯 Token
        token = token.substring(7);
        if (!jwtUtils.isValidToken(token)) {
            throw new BusinessException(401, "Token无效或已过期");
        }

        // 解析用户 ID 并存入 ThreadLocal，供业务代码使用
        Long userId = jwtUtils.parseUserId(token);
        UserContext.setUserId(userId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 必须清理 ThreadLocal，防止线程池复用导致用户数据串号
        UserContext.clear();
    }
}
