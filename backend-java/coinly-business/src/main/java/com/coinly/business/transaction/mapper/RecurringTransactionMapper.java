package com.coinly.business.transaction.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coinly.business.transaction.entity.RecurringTransactionEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * V9.1: 周期记账配置 Mapper。
 */
@Mapper
public interface RecurringTransactionMapper extends BaseMapper<RecurringTransactionEntity> {
}
