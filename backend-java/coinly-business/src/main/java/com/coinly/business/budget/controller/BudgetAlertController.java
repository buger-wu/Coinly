package com.coinly.business.budget.controller;

import com.coinly.business.budget.service.BudgetAlertService;
import com.coinly.business.budget.vo.BudgetAlertVO;
import com.coinly.common.context.UserContext;
import com.coinly.common.domain.CommonResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * V9.1: 预算预警记录接口。
 */
@RestController
@RequestMapping("/v1/budget-alerts")
public class BudgetAlertController {

    private final BudgetAlertService budgetAlertService;

    public BudgetAlertController(BudgetAlertService budgetAlertService) {
        this.budgetAlertService = budgetAlertService;
    }

    /**
     * 查询当前用户预警记录。
     */
    @GetMapping
    public CommonResponse<List<BudgetAlertVO>> listAlerts(@RequestParam(required = false) String month) {
        Long userId = UserContext.getUserId();
        return CommonResponse.success(budgetAlertService.listAlerts(userId, month));
    }

    /**
     * 标记预警为已读。
     */
    @PutMapping("/{alertId}/read")
    public CommonResponse<Void> markRead(@PathVariable Long alertId) {
        Long userId = UserContext.getUserId();
        budgetAlertService.markRead(userId, alertId);
        return CommonResponse.success("标记成功", null);
    }

    /**
     * 未读预警数量。
     */
    @GetMapping("/unread-count")
    public CommonResponse<Long> countUnread() {
        Long userId = UserContext.getUserId();
        return CommonResponse.success(budgetAlertService.countUnread(userId));
    }
}
