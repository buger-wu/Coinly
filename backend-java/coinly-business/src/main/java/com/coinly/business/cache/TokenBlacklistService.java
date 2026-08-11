package com.coinly.business.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Token 黑名单服务。
 * 用户退出登录时把 JWT Token 加入黑名单，鉴权时检查是否在黑名单中。
 * TTL 与 JWT 过期时间一致，过期后自动清理。
 * Redis 不可用时降级（返回 false），不阻塞用户请求。
 */
@Service
public class TokenBlacklistService {

    private static final Logger log = LoggerFactory.getLogger(TokenBlacklistService.class);
    private static final String KEY_PREFIX = "blacklist:";

    private final RedisTemplate<String, Object> redisTemplate;

    public TokenBlacklistService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklist(String token, long expiration) {
        try {
            redisTemplate.opsForValue().set(KEY_PREFIX + token, "1", expiration, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis 不可用，跳过 Token 黑名单写入: {}", e.getMessage());
        }
    }

    public boolean isBlacklisted(String token) {
        try {
            Boolean exists = redisTemplate.hasKey(KEY_PREFIX + token);
            return Boolean.TRUE.equals(exists);
        } catch (Exception e) {
            log.warn("Redis 不可用，跳过 Token 黑名单检查: {}", e.getMessage());
            return false;
        }
    }
}
