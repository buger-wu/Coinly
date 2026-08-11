package com.coinly.business.budget.controller;

import com.coinly.business.budget.dto.BudgetSetRequest;
import com.coinly.business.budget.dto.BudgetVO;
import com.coinly.business.budget.service.BudgetService;
import com.coinly.common.context.UserContext;
import com.coinly.common.domain.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "预算模块")
@RestController
@RequestMapping("/v1/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @Operation(summary = "设置/修改预算")
    @PostMapping
    public CommonResponse<Void> setBudget(@Valid @RequestBody BudgetSetRequest request) {
        Long userId = UserContext.getUserId();
        budgetService.setBudget(userId, request);
        return CommonResponse.success("设置成功", null);
    }

    @Operation(summary = "查询当月预算列表（含使用率、超支标记）")
    @GetMapping
    public CommonResponse<List<BudgetVO>> getBudgetList(@RequestParam String month) {
        Long userId = UserContext.getUserId();
        List<BudgetVO> list = budgetService.getBudgetList(userId, month);
        return CommonResponse.success(list);
    }

    @Operation(summary = "删除预算")
    @DeleteMapping("/{id}")
    public CommonResponse<Void> deleteBudget(@PathVariable Long id) {
        Long userId = UserContext.getUserId();
        budgetService.deleteBudget(userId, id);
        return CommonResponse.success("删除成功", null);
    }
}
