package com.coinly.business.statistics.service.impl;

import com.coinly.business.cache.StatisticsCacheService;
import com.coinly.business.statistics.dto.CategoryStatDTO;
import com.coinly.business.statistics.dto.MonthlySummaryDTO;
import com.coinly.business.statistics.service.StatisticsService;
import com.coinly.business.transaction.mapper.TransactionMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.*;

/**
 * 统计 Service 实现。
 * V7: 接入 Redis 缓存，月度/分类统计结果缓存 5 分钟，记账时自动清除。
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final TransactionMapper transactionMapper;
    private final StatisticsCacheService cacheService;

    public StatisticsServiceImpl(TransactionMapper transactionMapper, StatisticsCacheService cacheService) {
        this.transactionMapper = transactionMapper;
        this.cacheService = cacheService;
    }

    @Override
    public MonthlySummaryDTO getMonthlySummary(Long userId, Long bookId, String month) {
        // 1. 查缓存
        MonthlySummaryDTO cached = cacheService.getMonthlyStats(userId, bookId, month);
        if (cached != null) {
            return cached;
        }

        // 2. 缓存未命中，查数据库
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        BigDecimal totalIncome = transactionMapper.sumByType(userId, bookId, 1, startDate, endDate);
        BigDecimal totalExpense = transactionMapper.sumByType(userId, bookId, 0, startDate, endDate);

        MonthlySummaryDTO result = new MonthlySummaryDTO(totalIncome, totalExpense);

        // 3. 写入缓存
        cacheService.cacheMonthlyStats(userId, bookId, month, result);

        return result;
    }

    @Override
    public List<CategoryStatDTO> getCategoryStats(Long userId, Long bookId, String month) {
        // 1. 查缓存
        List<CategoryStatDTO> cached = cacheService.getCategoryStats(userId, bookId, month);
        if (cached != null) {
            return cached;
        }

        // 2. 查数据库
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<TransactionMapper.CategoryAmountDTO> rawData = transactionMapper.sumByCategory(userId, bookId, startDate, endDate);

        BigDecimal total = rawData.stream()
                .map(TransactionMapper.CategoryAmountDTO::totalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryStatDTO> result = new ArrayList<>();
        for (TransactionMapper.CategoryAmountDTO dto : rawData) {
            CategoryStatDTO stat = new CategoryStatDTO(dto.name(), dto.totalAmount());
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                stat.setPercentage(dto.totalAmount().multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP));
            }
            result.add(stat);
        }

        // 3. 写入缓存
        cacheService.cacheCategoryStats(userId, bookId, month, result);

        return result;
    }

    @Override
    public List<Map<String, Object>> getYearlyTrend(Long userId, Long bookId, String year) {
        int y = Year.parse(year).getValue();
        List<Map<String, Object>> trend = new ArrayList<>();

        for (int m = 1; m <= 12; m++) {
            YearMonth ym = YearMonth.of(y, m);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();

            BigDecimal income = transactionMapper.sumByType(userId, bookId, 1, start, end);
            BigDecimal expense = transactionMapper.sumByType(userId, bookId, 0, start, end);

            Map<String, Object> item = new HashMap<>();
            item.put("month", String.format("%d-%02d", y, m));
            item.put("income", income != null ? income : BigDecimal.ZERO);
            item.put("expense", expense != null ? expense : BigDecimal.ZERO);
            trend.add(item);
        }

        return trend;
    }

    /**
     * 近N个月收支趋势（支持跨年）。
     *
     * <p>从当前月份往前推 N 个月，每月统计收入和支出。
     * 与 yearly 不同，此方法支持跨年场景（如当前8月，近6个月为3-8月）。
     *
     * @param userId 用户 ID
     * @param bookId 账本 ID，null 表示所有账本汇总
     * @param months 近几个月
     * @return 每月 month/income/expense，按月份正序
     */
    @Override
    public List<Map<String, Object>> getRecentTrend(Long userId, Long bookId, int months) {
        YearMonth currentMonth = YearMonth.now();
        List<Map<String, Object>> trend = new ArrayList<>();

        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = currentMonth.minusMonths(i);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();

            BigDecimal income = transactionMapper.sumByType(userId, bookId, 1, start, end);
            BigDecimal expense = transactionMapper.sumByType(userId, bookId, 0, start, end);

            Map<String, Object> item = new HashMap<>();
            item.put("month", ym.toString());
            item.put("income", income != null ? income : BigDecimal.ZERO);
            item.put("expense", expense != null ? expense : BigDecimal.ZERO);
            trend.add(item);
        }

        return trend;
    }

    /**
     * 账本余额汇总。
     *
     * <p>一条 SQL 查出用户所有账本的总收入和总支出，余额 = 收入 - 支出。
     * 统计全部历史数据（非按月），排除已删除记录。
     *
     * @param userId 用户 ID
     * @return 各账本的 bookId/bookName/totalIncome/totalExpense 列表
     */
    @Override
    public List<TransactionMapper.BookBalanceDTO> getBookBalances(Long userId) {
        return transactionMapper.sumBalanceByBook(userId);
    }
}