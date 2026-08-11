package com.coinly.business.budget.service;

import com.coinly.business.budget.vo.BudgetAlertVO;

import java.util.List;

/**
 * V9.1: 预算预警记录服务接口。
 */
public interface BudgetAlertService {

    /**
     * 查询用户指定月份的预警记录，按创建时间倒序。
     */
    List<BudgetAlertVO> listAlerts(Long userId, String budgetMonth);

    /**
     * 标记预警为已读。
     */
    void markRead(Long userId, Long alertId);

    /**
     * 获取用户未读预警数量。
     */
    long countUnread(Long userId);
}
