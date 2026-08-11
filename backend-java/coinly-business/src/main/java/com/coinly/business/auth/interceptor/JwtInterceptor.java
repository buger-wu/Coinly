package com.coinly.business.auth.interceptor;

import com.coinly.business.cache.TokenBlacklistService;
import com.coinly.common.context.UserContext;
import com.coinly.common.exception.BusinessException;
import com.coinly.common.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 鉴权拦截器。
 * V7: 增加 Token 黑名单检查，退出登录的 Token 会被拒绝。
 * @see JwtUtils
 * @see UserContext
 * @see TokenBlacklistService
 */
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtInterceptor(JwtUtils jwtUtils, TokenBlacklistService tokenBlacklistService) {
        this.jwtUtils = jwtUtils;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            throw new BusinessException(401, "未授权，请先登录");
        }

        token = token.substring(7);
        if (!jwtUtils.isValidToken(token)) {
            throw new BusinessException(401, "Token无效或已过期");
        }

        // V7: 检查 Token 是否在黑名单中（已退出登录）
        if (tokenBlacklistService.isBlacklisted(token)) {
            throw new BusinessException(401, "Token已失效，请重新登录");
        }

        Long userId = jwtUtils.parseUserId(token);
        UserContext.setUserId(userId);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
