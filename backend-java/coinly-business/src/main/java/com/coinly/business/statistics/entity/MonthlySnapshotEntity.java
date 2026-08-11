package com.coinly.business.statistics.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * V9.1: 月账单快照实体。
 */
@Data
@TableName("biz_monthly_snapshot")
public class MonthlySnapshotEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long bookId;

    private String snapshotMonth;

    private BigDecimal totalIncome;

    private BigDecimal totalExpense;

    private BigDecimal netAmount;

    private Integer transactionCount;

    private String categorySummary;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
