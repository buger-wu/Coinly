package com.coinly.business.category.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coinly.business.category.entity.CategoryEntity;

public interface CategoryService extends IService<CategoryEntity> {

    void initDefaultCategories(Long userId);
}