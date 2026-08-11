package com.coinly.business.transaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coinly.business.category.entity.CategoryEntity;
import com.coinly.business.category.service.CategoryService;
import com.coinly.business.transaction.dto.TransactionExcelDTO;
import com.coinly.business.transaction.dto.TransactionQueryRequest;
import com.coinly.business.transaction.entity.TransactionEntity;
import com.coinly.business.transaction.mapper.TransactionMapper;
import com.coinly.business.transaction.service.TransactionService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl extends ServiceImpl<TransactionMapper, TransactionEntity> implements TransactionService {

    private final CategoryService categoryService;

    public TransactionServiceImpl(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public Page<TransactionEntity> getTransactionPage(Long userId, Long bookId, TransactionQueryRequest query, int page, int size) {
        LambdaQueryWrapper<TransactionEntity> wrapper = buildQueryWrapper(userId, bookId, query);
        return page(new Page<>(page, size), wrapper);
    }

    /**
     * V9.1: 查询全量交易（不分页）+ 批量填充 categoryName + 转 Excel DTO。
     */
    @Override
    public List<TransactionExcelDTO> listForExport(Long userId, Long bookId, TransactionQueryRequest query) {
        List<TransactionEntity> entities = list(buildQueryWrapper(userId, bookId, query));
        if (entities.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, String> categoryMap = loadCategoryNameMap(userId, entities);
        return entities.stream()
                .map(t -> new TransactionExcelDTO(
                        t.getTransactionDate() == null ? "" : t.getTransactionDate().toString(),
                        t.getType() != null && t.getType() == 1 ? "收入" : "支出",
                        t.getCategoryId() == null ? "" : categoryMap.getOrDefault(t.getCategoryId(), ""),
                        t.getAmount(),
                        t.getRemark() == null ? "" : t.getRemark()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 复用：构造交易查询 wrapper（含筛选 + 排序）。
     */
    private LambdaQueryWrapper<TransactionEntity> buildQueryWrapper(Long userId, Long bookId, TransactionQueryRequest query) {
        LambdaQueryWrapper<TransactionEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TransactionEntity::getUserId, userId)
                .eq(TransactionEntity::getBookId, bookId)
                .orderByDesc(TransactionEntity::getTransactionDate)
                .orderByDesc(TransactionEntity::getId);

        if (query != null) {
            if (query.getType() != null) {
                wrapper.eq(TransactionEntity::getType, query.getType());
            }
            if (query.getCategoryId() != null) {
                wrapper.eq(TransactionEntity::getCategoryId, query.getCategoryId());
            }
            if (query.getStartDate() != null) {
                wrapper.ge(TransactionEntity::getTransactionDate, query.getStartDate());
            }
            if (query.getEndDate() != null) {
                wrapper.le(TransactionEntity::getTransactionDate, query.getEndDate());
            }
        }
        return wrapper;
    }

    /**
     * 复用：批量查分类，返回 categoryId -> name 映射。
     */
    private Map<Long, String> loadCategoryNameMap(Long userId, List<TransactionEntity> transactions) {
        Set<Long> categoryIds = transactions.stream()
                .map(TransactionEntity::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (categoryIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return categoryService.lambdaQuery()
                .eq(CategoryEntity::getUserId, userId)
                .in(CategoryEntity::getId, categoryIds)
                .list()
                .stream()
                .collect(Collectors.toMap(CategoryEntity::getId, CategoryEntity::getName));
    }
}