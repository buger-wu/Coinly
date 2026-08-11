package com.coinly.business.transaction.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * V9.1: 周期记账配置实体。
 */
@Data
@TableName("biz_recurring_transaction")
public class RecurringTransactionEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long bookId;

    private Long categoryId;

    private Integer type;

    private BigDecimal amount;

    private String remark;

    private String cycleType;

    private Integer cycleDay;

    private LocalDate nextExecuteDate;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
