package com.coinly.business.statistics.service;

import com.coinly.business.statistics.dto.CategoryStatDTO;
import com.coinly.business.statistics.dto.MonthlySummaryDTO;
import com.coinly.business.transaction.mapper.TransactionMapper;

import java.util.List;
import java.util.Map;

public interface StatisticsService {

    MonthlySummaryDTO getMonthlySummary(Long userId, Long bookId, String month);

    List<CategoryStatDTO> getCategoryStats(Long userId, Long bookId, String month);

    List<Map<String, Object>> getYearlyTrend(Long userId, Long bookId, String year);

    /**
     * 近N个月收支趋势（支持跨年）。
     *
     * @param userId 用户 ID
     * @param bookId 账本 ID，null 表示所有账本汇总
     * @param months 近几个月，默认6
     * @return 每月 month/income/expense，按月份正序
     */
    List<Map<String, Object>> getRecentTrend(Long userId, Long bookId, int months);

    /**
     * 账本余额汇总。
     *
     * @param userId 用户 ID
     * @return 各账本的总收入、总支出、余额列表
     */
    List<TransactionMapper.BookBalanceDTO> getBookBalances(Long userId);
}
