package com.coinly.business.statistics.service.impl;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计 Service 实现。
 * 统计数据实时计算，不冗余存储余额，保证数据一致性。
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final TransactionMapper transactionMapper;

    public StatisticsServiceImpl(TransactionMapper transactionMapper) {
        this.transactionMapper = transactionMapper;
    }

    /**
     * 月度收支总览。
     *
     * <p>统计指定月份（支持按账本筛选）的收入总额、支出总额、净收支。
     *
     * @param userId 当前用户 ID
     * @param bookId 账本 ID，null 表示所有账本汇总
     * @param month  月份，格式 yyyy-MM
     * @return 收入、支出、净收支
     */
    @Override
    public MonthlySummaryDTO getMonthlySummary(Long userId, Long bookId, String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // type=1 收入，type=0 支出
        BigDecimal totalIncome = transactionMapper.sumByType(userId, bookId, 1, startDate, endDate);
        BigDecimal totalExpense = transactionMapper.sumByType(userId, bookId, 0, startDate, endDate);

        return new MonthlySummaryDTO(totalIncome, totalExpense);
    }

    /**
     * 分类支出占比统计。
     *
     * @param userId 当前用户 ID
     * @param bookId 账本 ID，null 表示所有账本汇总
     * @param month  月份，格式 yyyy-MM
     * @return 分类名称、金额、百分比列表（按金额倒序）
     */
    @Override
    public List<CategoryStatDTO> getCategoryStats(Long userId, Long bookId, String month) {
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 按分类分组求和（SQL 已按金额倒序）
        List<TransactionMapper.CategoryAmountDTO> rawData = transactionMapper.sumByCategory(userId, bookId, startDate, endDate);

        // 计算总支出，用于算占比
        BigDecimal total = rawData.stream()
                .map(TransactionMapper.CategoryAmountDTO::totalAmount)
                .filter(a -> a != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算每个分类的占比
        List<CategoryStatDTO> result = new ArrayList<>();
        for (TransactionMapper.CategoryAmountDTO dto : rawData) {
            CategoryStatDTO stat = new CategoryStatDTO(dto.name(), dto.totalAmount());
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                stat.setPercentage(dto.totalAmount().multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP));
            }
            result.add(stat);
        }

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
}