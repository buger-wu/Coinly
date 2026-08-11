package com.coinly.business.mq.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportMessage {
    private Long userId;
    private Long bookId;
    private Long transactionId;
    private String month;
    private long timestamp;
}
