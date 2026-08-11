package com.coinly.business.budget.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BudgetVO {

    private Long id;

    /** 分类 ID，null 表示总预算 */
    private Long categoryId;

    /** 分类名称，总预算时为"总预算" */
    private String categoryName;

    /** 预算金额 */
    private BigDecimal amount;

    /** 已使用金额 */
    private BigDecimal used;

    /** 剩余金额 */
    private BigDecimal remaining;

    /** 使用率（百分比） */
    private BigDecimal percentage;

    /** 状态：success(绿)/warning(黄)/danger(红) */
    private String status;
}
