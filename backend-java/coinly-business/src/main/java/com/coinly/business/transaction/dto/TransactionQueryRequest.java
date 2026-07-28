package com.coinly.business.transaction.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TransactionQueryRequest {

    private Integer type;

    private Long categoryId;

    private LocalDate startDate;

    private LocalDate endDate;
}