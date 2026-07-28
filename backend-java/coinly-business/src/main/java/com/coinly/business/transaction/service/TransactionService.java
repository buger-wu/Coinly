package com.coinly.business.transaction.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coinly.business.transaction.entity.TransactionEntity;
import com.coinly.business.transaction.dto.TransactionQueryRequest;

public interface TransactionService extends IService<TransactionEntity> {

    Page<TransactionEntity> getTransactionPage(Long userId, Long bookId, TransactionQueryRequest query, int page, int size);
}