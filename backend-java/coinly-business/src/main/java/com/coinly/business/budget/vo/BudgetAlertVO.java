package com.coinly.business.budget.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * V9.1: 预算预警记录 VO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetAlertVO {

    private Long id;
    private Long budgetId;
    private Long categoryId;
    private String categoryName;
    private String budgetMonth;
    private BigDecimal budgetAmount;
    private BigDecimal usedAmount;
    private BigDecimal percentage;
    private String alertLevel;
    private Boolean read;
    private LocalDateTime createTime;
}
