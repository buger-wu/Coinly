package com.coinly.business.security.login;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * V9: 登录失败处理服务。
 * 使用 Redis 记录登录失败次数，超过5次锁定账号15分钟。
 */
@Slf4j
@Service
public class LoginAttemptService {

    private static final String FAIL_KEY_PREFIX = "login:fail:";
    private static final String LOCK_KEY_PREFIX = "login:lock:";
    private static final int MAX_FAIL_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 15;

    private final RedisTemplate<String, Object> redisTemplate;

    public LoginAttemptService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 检查账号是否已被锁定。
     */
    public boolean isLocked(String username) {
        String lockKey = LOCK_KEY_PREFIX + username;
        Boolean exists = redisTemplate.hasKey(lockKey);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 记录一次登录失败。
     * 首次失败时设置15分钟TTL，达到5次时设置锁定Key。
     *
     * @return 当前失败次数
     */
    public long recordFailure(String username) {
        String failKey = FAIL_KEY_PREFIX + username;
        Long attempts = redisTemplate.opsForValue().increment(failKey);
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(failKey, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        }
        if (attempts != null && attempts >= MAX_FAIL_ATTEMPTS) {
            String lockKey = LOCK_KEY_PREFIX + username;
            redisTemplate.opsForValue().set(lockKey, "LOCKED", LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
            log.warn("账号 {} 登录失败 {} 次，已锁定 {} 分钟", username, attempts, LOCK_DURATION_MINUTES);
        }
        return attempts != null ? attempts : 0;
    }

    /**
     * 登录成功时清除失败记录。
     */
    public void recordSuccess(String username) {
        String failKey = FAIL_KEY_PREFIX + username;
        redisTemplate.delete(failKey);
    }
}
