package com.coinly.business.category.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coinly.business.category.entity.CategoryEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<CategoryEntity> {
}