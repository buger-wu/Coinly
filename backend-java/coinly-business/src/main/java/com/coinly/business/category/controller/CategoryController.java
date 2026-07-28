package com.coinly.business.category.controller;

import com.coinly.business.category.dto.CreateCategoryRequest;
import com.coinly.business.category.dto.UpdateCategoryRequest;
import com.coinly.business.category.entity.CategoryEntity;
import com.coinly.business.category.service.CategoryService;
import com.coinly.common.context.UserContext;
import com.coinly.common.domain.CommonResponse;
import com.coinly.common.exception.BusinessException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public CommonResponse<List<CategoryEntity>> getCategoryList() {
        Long userId = UserContext.getUserId();
        List<CategoryEntity> categories = categoryService.lambdaQuery()
                .eq(CategoryEntity::getUserId, userId)
                .isNull(CategoryEntity::getParentId)
                .orderByAsc(CategoryEntity::getSortOrder)
                .list();
        return CommonResponse.success(categories);
    }

    @GetMapping("/all")
    public CommonResponse<List<CategoryEntity>> getAllCategories() {
        Long userId = UserContext.getUserId();
        List<CategoryEntity> categories = categoryService.lambdaQuery()
                .eq(CategoryEntity::getUserId, userId)
                .orderByAsc(CategoryEntity::getSortOrder)
                .list();
        return CommonResponse.success(categories);
    }

    @PostMapping
    public CommonResponse<CategoryEntity> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        Long userId = UserContext.getUserId();

        CategoryEntity category = new CategoryEntity();
        category.setUserId(userId);
        category.setName(request.getName());
        category.setType(request.getType());
        category.setParentId(request.getParentId());
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());

        categoryService.save(category);
        return CommonResponse.success(category);
    }

    @PutMapping("/{id}")
    public CommonResponse<Void> updateCategory(@PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest request) {
        Long userId = UserContext.getUserId();
        CategoryEntity category = categoryService.lambdaQuery()
                .eq(CategoryEntity::getId, id)
                .eq(CategoryEntity::getUserId, userId)
                .one();

        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        if (request.getName() != null) {
            category.setName(request.getName());
        }
        if (request.getType() != null) {
            category.setType(request.getType());
        }
        if (request.getParentId() != null) {
            category.setParentId(request.getParentId());
        }
        category.setUpdateTime(LocalDateTime.now());

        categoryService.updateById(category);
        return CommonResponse.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public CommonResponse<Void> deleteCategory(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        CategoryEntity category = categoryService.lambdaQuery()
                .eq(CategoryEntity::getId, id)
                .eq(CategoryEntity::getUserId, userId)
                .one();

        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        categoryService.removeById(id);
        return CommonResponse.success("删除成功", null);
    }
}