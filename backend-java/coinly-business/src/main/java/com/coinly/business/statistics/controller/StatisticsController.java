package com.coinly.business.statistics.controller;

import com.coinly.business.statistics.dto.CategoryStatDTO;
import com.coinly.business.statistics.dto.MonthlySummaryDTO;
import com.coinly.business.statistics.service.StatisticsService;
import com.coinly.business.transaction.mapper.TransactionMapper;
import com.coinly.common.context.UserContext;
import com.coinly.common.domain.CommonResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;

@Tag(name = "统计模块")
@RestController
@RequestMapping("/v1/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @Operation(summary = "月度收支总览")
    @GetMapping("/monthly")
    public CommonResponse<MonthlySummaryDTO> getMonthlySummary(@RequestParam(required = false) Long bookId,
                                                               @RequestParam String month) {
        Long userId = UserContext.getUserId();
        MonthlySummaryDTO summary = statisticsService.getMonthlySummary(userId, bookId, month);
        return CommonResponse.success(summary);
    }

    @Operation(summary = "分类支出占比")
    @GetMapping("/category")
    public CommonResponse<List<CategoryStatDTO>> getCategoryStats(@RequestParam(required = false) Long bookId,
                                                                  @RequestParam String month) {
        Long userId = UserContext.getUserId();
        List<CategoryStatDTO> stats = statisticsService.getCategoryStats(userId, bookId, month);
        return CommonResponse.success(stats);
    }

    @Operation(summary = "年度收支趋势")
    @GetMapping("/yearly")
    public CommonResponse<List<Map<String, Object>>> getYearlyTrend(@RequestParam(required = false) Long bookId,
                                                                     @RequestParam String year) {
        Long userId = UserContext.getUserId();
        List<Map<String, Object>> trend = statisticsService.getYearlyTrend(userId, bookId, year);
        return CommonResponse.success(trend);
    }

    /**
     * 近N个月收支趋势（支持跨年）。
     *
     * @param bookId 账本 ID，可选，不传时汇总所有账本
     * @param months 近几个月，默认6
     * @return 每月 month/income/expense，按月份正序
     */
    @Operation(summary = "近N个月收支趋势")
    @GetMapping("/recent-trend")
    public CommonResponse<List<Map<String, Object>>> getRecentTrend(@RequestParam(required = false) Long bookId,
                                                                     @RequestParam(defaultValue = "6") int months) {
        Long userId = UserContext.getUserId();
        List<Map<String, Object>> trend = statisticsService.getRecentTrend(userId, bookId, months);
        return CommonResponse.success(trend);
    }

    /**
     * 账本余额汇总。
     *
     * <p>返回用户所有账本的总收入、总支出、余额（收入-支出）。
     * 统计全部历史数据，排除已删除记录。
     *
     * @return 各账本余额列表
     */
    @Operation(summary = "账本余额汇总")
    @GetMapping("/balances")
    public CommonResponse<List<TransactionMapper.BookBalanceDTO>> getBookBalances() {
        Long userId = UserContext.getUserId();
        List<TransactionMapper.BookBalanceDTO> balances = statisticsService.getBookBalances(userId);
        return CommonResponse.success(balances);
    }
}
