package com.coinly.business.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateTransactionRequest {

    private Integer type;

    private Long categoryId;

    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;

    private String remark;

    private LocalDate transactionDate;
}