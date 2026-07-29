package com.coinly.business.statistics.controller;

import com.coinly.business.statistics.dto.CategoryStatDTO;
import com.coinly.business.statistics.dto.MonthlySummaryDTO;
import com.coinly.business.statistics.service.StatisticsService;
import com.coinly.common.context.UserContext;
import com.coinly.common.domain.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/monthly")
    public CommonResponse<MonthlySummaryDTO> getMonthlySummary(@RequestParam(required = false) Long bookId,
                                                               @RequestParam String month) {
        Long userId = UserContext.getUserId();
        MonthlySummaryDTO summary = statisticsService.getMonthlySummary(userId, bookId, month);
        return CommonResponse.success(summary);
    }

    @GetMapping("/category")
    public CommonResponse<List<CategoryStatDTO>> getCategoryStats(@RequestParam(required = false) Long bookId,
                                                                  @RequestParam String month) {
        Long userId = UserContext.getUserId();
        List<CategoryStatDTO> stats = statisticsService.getCategoryStats(userId, bookId, month);
        return CommonResponse.success(stats);
    }

    @GetMapping("/yearly")
    public CommonResponse<List<Map<String, Object>>> getYearlyTrend(@RequestParam(required = false) Long bookId,
                                                                     @RequestParam String year) {
        Long userId = UserContext.getUserId();
        List<Map<String, Object>> trend = statisticsService.getYearlyTrend(userId, bookId, year);
        return CommonResponse.success(trend);
    }
}