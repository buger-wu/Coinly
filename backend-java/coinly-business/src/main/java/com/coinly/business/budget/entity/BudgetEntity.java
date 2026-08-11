package com.coinly.business.budget.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_budget")
public class BudgetEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 分类 ID，null 表示总预算 */
    private Long categoryId;

    private BigDecimal amount;

    /** 预算月份，格式 yyyy-MM */
    private String budgetMonth;

    @TableLogic
    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
