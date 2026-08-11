package com.coinly.business.transaction.controller;

import com.coinly.business.transaction.dto.RecurringTransactionRequest;
import com.coinly.business.transaction.service.RecurringTransactionService;
import com.coinly.business.transaction.vo.RecurringTransactionVO;
import com.coinly.common.context.UserContext;
import com.coinly.common.domain.CommonResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * V9.1: 周期记账配置接口。
 */
@RestController
@RequestMapping("/api/v1/recurring-transactions")
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    public RecurringTransactionController(RecurringTransactionService recurringTransactionService) {
        this.recurringTransactionService = recurringTransactionService;
    }

    @PostMapping
    public CommonResponse<RecurringTransactionVO> create(@Valid @RequestBody RecurringTransactionRequest request) {
        Long userId = UserContext.getUserId();
        return CommonResponse.success(recurringTransactionService.create(userId, request));
    }

    @GetMapping
    public CommonResponse<List<RecurringTransactionVO>> list() {
        Long userId = UserContext.getUserId();
        return CommonResponse.success(recurringTransactionService.list(userId));
    }

    @PutMapping("/{id}")
    public CommonResponse<RecurringTransactionVO> update(@PathVariable Long id,
                                                          @Valid @RequestBody RecurringTransactionRequest request) {
        Long userId = UserContext.getUserId();
        return CommonResponse.success(recurringTransactionService.update(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public CommonResponse<Void> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        recurringTransactionService.delete(userId, id);
        return CommonResponse.success("删除成功", null);
    }

    @PutMapping("/{id}/toggle")
    public CommonResponse<Void> toggle(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        recurringTransactionService.toggleStatus(userId, id);
        return CommonResponse.success("操作成功", null);
    }
}
