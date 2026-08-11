package com.coinly.business.budget.service;

import com.coinly.business.budget.dto.BudgetSetRequest;
import com.coinly.business.budget.dto.BudgetVO;

import java.util.List;

public interface BudgetService {

    /**
     * 设置预算（同用户+同分类+同月份已存在则更新）
     */
    void setBudget(Long userId, BudgetSetRequest request);

    /**
     * 查询当月预算列表（含使用率、超支标记）
     */
    List<BudgetVO> getBudgetList(Long userId, String budgetMonth);

    /**
     * 删除预算
     */
    void deleteBudget(Long userId, Long budgetId);
}
