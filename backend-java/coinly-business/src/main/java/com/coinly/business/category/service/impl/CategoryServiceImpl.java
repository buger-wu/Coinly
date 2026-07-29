package com.coinly.business.category.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coinly.business.category.entity.CategoryEntity;
import com.coinly.business.category.entity.DefaultCategoryEntity;
import com.coinly.business.category.mapper.CategoryMapper;
import com.coinly.business.category.mapper.DefaultCategoryMapper;
import com.coinly.business.category.service.CategoryService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, CategoryEntity> implements CategoryService {

    private final DefaultCategoryMapper defaultCategoryMapper;

    public CategoryServiceImpl(DefaultCategoryMapper defaultCategoryMapper) {
        this.defaultCategoryMapper = defaultCategoryMapper;
    }

    /**
     * 为新注册用户初始化默认分类。
     * @param userId 新注册用户的 ID
     */
    @Override
    public void initDefaultCategories(Long userId) {
        List<DefaultCategoryEntity> defaults = defaultCategoryMapper.selectList(null);

        // 按是否有 parent_id 拆分：一级分类（parent_id=null）和二级分类
        List<DefaultCategoryEntity> firstLevel = defaults.stream()
                .filter(d -> d.getParentId() == null)
                .collect(Collectors.toList());

        List<DefaultCategoryEntity> secondLevel = defaults.stream()
                .filter(d -> d.getParentId() != null)
                .collect(Collectors.toList());

        // 第一步：批量插入一级分类
        List<CategoryEntity> firstLevelCategories = firstLevel.stream()
                .map(d -> {
                    CategoryEntity c = new CategoryEntity();
                    c.setUserId(userId);
                    c.setName(d.getName());
                    c.setType(d.getType());
                    c.setParentId(null);
                    c.setSortOrder(d.getSortOrder());
                    c.setCreateTime(LocalDateTime.now());
                    c.setUpdateTime(LocalDateTime.now());
                    return c;
                })
                .collect(Collectors.toList());

        saveBatch(firstLevelCategories);

        // 第二步：建立 "模板一级分类 ID -> 用户新建一级分类 ID" 的映射
        Map<Long, Long> idMapping = new HashMap<>();
        for (int i = 0; i < firstLevel.size(); i++) {
            idMapping.put(firstLevel.get(i).getId(), firstLevelCategories.get(i).getId());
        }

        // 第三步：批量插入二级分类，parent_id 指向用户新建的一级分类
        List<CategoryEntity> secondLevelCategories = secondLevel.stream()
                .map(d -> {
                    CategoryEntity c = new CategoryEntity();
                    c.setUserId(userId);
                    c.setName(d.getName());
                    c.setType(d.getType());
                    c.setParentId(idMapping.get(d.getParentId()));
                    c.setSortOrder(d.getSortOrder());
                    c.setCreateTime(LocalDateTime.now());
                    c.setUpdateTime(LocalDateTime.now());
                    return c;
                })
                .collect(Collectors.toList());

        saveBatch(secondLevelCategories);
    }
}