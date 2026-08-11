package com.coinly.business.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * V9.1: 周期记账配置请求 DTO。
 */
@Data
public class RecurringTransactionRequest {

    @NotNull(message = "账本ID不能为空")
    private Long bookId;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    @NotNull(message = "交易类型不能为空")
    private Integer type;

    @NotNull(message = "金额不能为空")
    @Positive(message = "金额必须大于0")
    private BigDecimal amount;

    private String remark;

    @NotBlank(message = "周期类型不能为空")
    private String cycleType;

    private Integer cycleDay;

    @NotNull(message = "首次执行日期不能为空")
    private LocalDate nextExecuteDate;
}
