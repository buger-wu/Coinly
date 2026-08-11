package com.coinly.business.transaction.service;

import com.coinly.business.transaction.dto.RecurringTransactionRequest;
import com.coinly.business.transaction.vo.RecurringTransactionVO;

import java.util.List;

/**
 * V9.1: 周期记账配置服务接口。
 */
public interface RecurringTransactionService {

    RecurringTransactionVO create(Long userId, RecurringTransactionRequest request);

    List<RecurringTransactionVO> list(Long userId);

    RecurringTransactionVO update(Long userId, Long id, RecurringTransactionRequest request);

    void delete(Long userId, Long id);

    void toggleStatus(Long userId, Long id);
}
