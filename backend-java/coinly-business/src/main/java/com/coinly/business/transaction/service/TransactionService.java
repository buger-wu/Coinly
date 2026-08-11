package com.coinly.business.transaction.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coinly.business.transaction.entity.TransactionEntity;
import com.coinly.business.transaction.dto.TransactionExcelDTO;
import com.coinly.business.transaction.dto.TransactionQueryRequest;

import java.util.List;

public interface TransactionService extends IService<TransactionEntity> {

    Page<TransactionEntity> getTransactionPage(Long userId, Long bookId, TransactionQueryRequest query, int page, int size);

    /**
     * V9.1: 查询全量交易记录（不分页），并转换为 Excel 导出 DTO。
     */
    List<TransactionExcelDTO> listForExport(Long userId, Long bookId, TransactionQueryRequest query);
}