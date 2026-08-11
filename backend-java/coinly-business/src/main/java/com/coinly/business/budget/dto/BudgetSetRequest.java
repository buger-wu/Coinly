package com.coinly.business.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetSetRequest {

    /** 分类 ID，null 表示总预算 */
    private Long categoryId;

    @NotNull(message = "预算金额不能为空")
    @DecimalMin(value = "0.01", message = "预算金额必须大于0")
    private BigDecimal amount;

    @NotBlank(message = "预算月份不能为空")
    @Size(min = 7, max = 7, message = "月份格式应为 yyyy-MM")
    private String budgetMonth;
}
