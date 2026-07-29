package com.coinly.business.statistics.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthlySummaryDTO {

    private BigDecimal totalIncome;

    private BigDecimal totalExpense;

    private BigDecimal netIncome;

    public MonthlySummaryDTO(BigDecimal totalIncome, BigDecimal totalExpense) {
        this.totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        this.totalExpense = totalExpense != null ? totalExpense : BigDecimal.ZERO;
        this.netIncome = this.totalIncome.subtract(this.totalExpense);
    }
}