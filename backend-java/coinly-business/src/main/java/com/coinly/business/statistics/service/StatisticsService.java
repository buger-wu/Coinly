package com.coinly.business.statistics.service;

import com.coinly.business.statistics.dto.CategoryStatDTO;
import com.coinly.business.statistics.dto.MonthlySummaryDTO;

import java.util.List;
import java.util.Map;

public interface StatisticsService {

    MonthlySummaryDTO getMonthlySummary(Long userId, Long bookId, String month);

    List<CategoryStatDTO> getCategoryStats(Long userId, Long bookId, String month);

    List<Map<String, Object>> getYearlyTrend(Long userId, Long bookId, String year);
}