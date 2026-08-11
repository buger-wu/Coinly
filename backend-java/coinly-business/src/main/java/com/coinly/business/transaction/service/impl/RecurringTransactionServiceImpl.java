package com.coinly.business.transaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coinly.business.book.entity.BookEntity;
import com.coinly.business.book.mapper.BookMapper;
import com.coinly.business.category.entity.CategoryEntity;
import com.coinly.business.category.mapper.CategoryMapper;
import com.coinly.business.transaction.dto.RecurringTransactionRequest;
import com.coinly.business.transaction.entity.RecurringTransactionEntity;
import com.coinly.business.transaction.mapper.RecurringTransactionMapper;
import com.coinly.business.transaction.service.RecurringTransactionService;
import com.coinly.business.transaction.vo.RecurringTransactionVO;
import com.coinly.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * V9.1: 周期记账配置服务实现。
 */
@Service
public class RecurringTransactionServiceImpl implements RecurringTransactionService {

    private final RecurringTransactionMapper recurringTransactionMapper;
    private final BookMapper bookMapper;
    private final CategoryMapper categoryMapper;

    public RecurringTransactionServiceImpl(RecurringTransactionMapper recurringTransactionMapper,
                                           BookMapper bookMapper,
                                           CategoryMapper categoryMapper) {
        this.recurringTransactionMapper = recurringTransactionMapper;
        this.bookMapper = bookMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public RecurringTransactionVO create(Long userId, RecurringTransactionRequest request) {
        validateOwnership(userId, request.getBookId(), request.getCategoryId());

        RecurringTransactionEntity entity = new RecurringTransactionEntity();
        entity.setUserId(userId);
        entity.setBookId(request.getBookId());
        entity.setCategoryId(request.getCategoryId());
        entity.setType(request.getType());
        entity.setAmount(request.getAmount());
        entity.setRemark(request.getRemark());
        entity.setCycleType(request.getCycleType());
        entity.setCycleDay(request.getCycleDay());
        entity.setNextExecuteDate(request.getNextExecuteDate());
        entity.setStatus(1);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        recurringTransactionMapper.insert(entity);
        return convert(entity);
    }

    @Override
    public List<RecurringTransactionVO> list(Long userId) {
        List<RecurringTransactionEntity> list = recurringTransactionMapper.selectList(
                new LambdaQueryWrapper<RecurringTransactionEntity>()
                        .eq(RecurringTransactionEntity::getUserId, userId)
                        .orderByDesc(RecurringTransactionEntity::getCreateTime)
        );
        return list.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public RecurringTransactionVO update(Long userId, Long id, RecurringTransactionRequest request) {
        RecurringTransactionEntity entity = getAndCheck(userId, id);
        validateOwnership(userId, request.getBookId(), request.getCategoryId());

        entity.setBookId(request.getBookId());
        entity.setCategoryId(request.getCategoryId());
        entity.setType(request.getType());
        entity.setAmount(request.getAmount());
        entity.setRemark(request.getRemark());
        entity.setCycleType(request.getCycleType());
        entity.setCycleDay(request.getCycleDay());
        entity.setNextExecuteDate(request.getNextExecuteDate());
        entity.setUpdateTime(LocalDateTime.now());

        recurringTransactionMapper.updateById(entity);
        return convert(entity);
    }

    @Override
    public void delete(Long userId, Long id) {
        RecurringTransactionEntity entity = getAndCheck(userId, id);
        recurringTransactionMapper.deleteById(id);
    }

    @Override
    public void toggleStatus(Long userId, Long id) {
        RecurringTransactionEntity entity = getAndCheck(userId, id);
        entity.setStatus(entity.getStatus() != null && entity.getStatus() == 1 ? 0 : 1);
        entity.setUpdateTime(LocalDateTime.now());
        recurringTransactionMapper.updateById(entity);
    }

    private RecurringTransactionEntity getAndCheck(Long userId, Long id) {
        RecurringTransactionEntity entity = recurringTransactionMapper.selectById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new BusinessException("周期记账配置不存在");
        }
        return entity;
    }

    private void validateOwnership(Long userId, Long bookId, Long categoryId) {
        BookEntity book = bookMapper.selectById(bookId);
        if (book == null || !book.getUserId().equals(userId)) {
            throw new BusinessException("账本不存在");
        }
        CategoryEntity category = categoryMapper.selectById(categoryId);
        if (category == null || !category.getUserId().equals(userId)) {
            throw new BusinessException("分类不存在");
        }
    }

    private RecurringTransactionVO convert(RecurringTransactionEntity entity) {
        BookEntity book = bookMapper.selectById(entity.getBookId());
        CategoryEntity category = categoryMapper.selectById(entity.getCategoryId());

        return new RecurringTransactionVO(
                entity.getId(),
                entity.getBookId(),
                book != null ? book.getName() : "未知账本",
                entity.getCategoryId(),
                category != null ? category.getName() : "未知分类",
                entity.getType(),
                entity.getAmount(),
                entity.getRemark(),
                entity.getCycleType(),
                entity.getCycleDay(),
                entity.getNextExecuteDate(),
                entity.getStatus(),
                entity.getCreateTime()
        );
    }
}
