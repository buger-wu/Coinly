package com.coinly.common.context;

/**
 * 当前登录用户上下文。
 *
 * @see com.coinly.business.auth.interceptor.JwtInterceptor
 */
public class UserContext {

    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();

    /**
     * 设置当前用户 ID（由拦截器在请求开始时调用）。
     *
     * @param userId 从 JWT 解析出的用户 ID
     */
    public static void setUserId(Long userId) {
        userIdHolder.set(userId);
    }

    /**
     * 获取当前用户 ID（业务代码调用，用于数据隔离查询）。
     *
     * @return 当前用户 ID，未登录时为 null
     */
    public static Long getUserId() {
        return userIdHolder.get();
    }

    /**
     * 清除当前线程的用户 ID（由拦截器在请求结束时调用，必须执行，防止内存泄漏）。
     */
    public static void clear() {
        userIdHolder.remove();
    }
}