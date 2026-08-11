package com.coinly.business.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertMessage {
    private Long userId;
    private Long budgetId;
    private Long categoryId;
    private String categoryName;
    private BigDecimal budgetAmount;
    private BigDecimal usedAmount;
    private BigDecimal percentage;
    private String level;
    private String month;
    private long timestamp;
}
