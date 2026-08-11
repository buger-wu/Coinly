package com.coinly.business.budget.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * V9.1: 预算预警记录实体。
 */
@Data
@TableName("biz_budget_alert")
public class BudgetAlertEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long budgetId;

    private Long categoryId;

    private String categoryName;

    private String budgetMonth;

    private BigDecimal budgetAmount;

    private BigDecimal usedAmount;

    private BigDecimal percentage;

    private String alertLevel;

    private Integer isRead;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
