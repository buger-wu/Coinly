package com.coinly.business.transaction.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coinly.business.transaction.entity.TransactionEntity;
import com.coinly.business.transaction.mapper.TransactionMapper;
import com.coinly.business.transaction.dto.TransactionQueryRequest;
import com.coinly.business.transaction.service.TransactionService;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl extends ServiceImpl<TransactionMapper, TransactionEntity> implements TransactionService {

    @Override
    public Page<TransactionEntity> getTransactionPage(Long userId, Long bookId, TransactionQueryRequest query, int page, int size) {
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

        return page(new Page<>(page, size), wrapper);
    }
}