package com.coinly.business.statistics.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CategoryStatDTO {

    private String categoryName;

    private BigDecimal amount;

    private BigDecimal percentage;

    public CategoryStatDTO(String categoryName, BigDecimal amount) {
        this.categoryName = categoryName;
        this.amount = amount != null ? amount : BigDecimal.ZERO;
        this.percentage = BigDecimal.ZERO;
    }
}