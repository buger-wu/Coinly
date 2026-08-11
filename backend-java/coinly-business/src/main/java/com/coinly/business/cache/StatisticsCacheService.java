package com.coinly.business.cache;

import com.coinly.business.statistics.dto.MonthlySummaryDTO;
import com.coinly.business.statistics.dto.CategoryStatDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 统计数据缓存服务。
 * 缓存月度收支和分类占比统计结果，TTL 5 分钟。
 * 缓存 key 包含 bookId，避免不同账本统计数据串读。
 * 记账/删除交易时清除缓存保证一致性。
 */
@Service
public class StatisticsCacheService {

    private static final String MONTHLY_KEY = "stat:monthly:";
    private static final String CATEGORY_KEY = "stat:category:";
    private static final long TTL = 300; // 5 分钟

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public StatisticsCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 获取月度统计缓存。
     */
    public MonthlySummaryDTO getMonthlyStats(Long userId, Long bookId, String month) {
        try {
            Object val = redisTemplate.opsForValue().get(buildMonthlyKey(userId, bookId, month));
            if (val == null) return null;
            return objectMapper.convertValue(val, MonthlySummaryDTO.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 缓存月度统计。
     */
    public void cacheMonthlyStats(Long userId, Long bookId, String month, MonthlySummaryDTO data) {
        try {
            redisTemplate.opsForValue().set(buildMonthlyKey(userId, bookId, month), data, TTL, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }
    }

    /**
     * 获取分类统计缓存。
     */
    public List<CategoryStatDTO> getCategoryStats(Long userId, Long bookId, String month) {
        try {
            Object val = redisTemplate.opsForValue().get(buildCategoryKey(userId, bookId, month));
            if (val == null) return null;
            return objectMapper.convertValue(val, new TypeReference<List<CategoryStatDTO>>() {});
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 缓存分类统计。
     */
    public void cacheCategoryStats(Long userId, Long bookId, String month, List<CategoryStatDTO> data) {
        try {
            redisTemplate.opsForValue().set(buildCategoryKey(userId, bookId, month), data, TTL, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }
    }

    /**
     * 清除用户某月的所有统计缓存（含全部账本）。
     * 记账/删除交易时调用，用通配符匹配确保清除干净。
     */
    public void evictStats(Long userId, String month) {
        try {
            Set<String> monthlyKeys = redisTemplate.keys(MONTHLY_KEY + userId + ":*:" + month);
            Set<String> categoryKeys = redisTemplate.keys(CATEGORY_KEY + userId + ":*:" + month);
            if (monthlyKeys != null && !monthlyKeys.isEmpty()) {
                redisTemplate.delete(monthlyKeys);
            }
            if (categoryKeys != null && !categoryKeys.isEmpty()) {
                redisTemplate.delete(categoryKeys);
            }
        } catch (Exception ignored) {
        }
    }

    private String buildMonthlyKey(Long userId, Long bookId, String month) {
        return MONTHLY_KEY + userId + ":" + (bookId != null ? bookId : "all") + ":" + month;
    }

    private String buildCategoryKey(Long userId, Long bookId, String month) {
        return CATEGORY_KEY + userId + ":" + (bookId != null ? bookId : "all") + ":" + month;
    }
}
