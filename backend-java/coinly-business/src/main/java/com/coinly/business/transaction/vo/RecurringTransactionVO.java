package com.coinly.business.transaction.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * V9.1: 周期记账配置 VO。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecurringTransactionVO {

    private Long id;
    private Long bookId;
    private String bookName;
    private Long categoryId;
    private String categoryName;
    private Integer type;
    private BigDecimal amount;
    private String remark;
    private String cycleType;
    private Integer cycleDay;
    private LocalDate nextExecuteDate;
    private Integer status;
    private LocalDateTime createTime;
}
