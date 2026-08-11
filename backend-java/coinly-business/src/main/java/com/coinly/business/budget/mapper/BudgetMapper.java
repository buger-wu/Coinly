package com.coinly.business.budget.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coinly.business.budget.entity.BudgetEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BudgetMapper extends BaseMapper<BudgetEntity> {
}
