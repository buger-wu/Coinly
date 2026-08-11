package com.coinly.business.budget.service.impl;

import com.coinly.business.budget.dto.BudgetSetRequest;
import com.coinly.business.budget.dto.BudgetVO;
import com.coinly.business.budget.entity.BudgetAlertEntity;
import com.coinly.business.budget.entity.BudgetEntity;
import com.coinly.business.budget.mapper.BudgetAlertMapper;
import com.coinly.business.budget.mapper.BudgetMapper;
import com.coinly.business.budget.service.BudgetService;
import com.coinly.business.category.entity.CategoryEntity;
import com.coinly.business.category.mapper.CategoryMapper;
import com.coinly.business.mq.dto.AlertMessage;
import com.coinly.business.mq.producer.MessageProducer;
import com.coinly.business.transaction.mapper.TransactionMapper;
import com.coinly.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class BudgetServiceImpl implements BudgetService {

    private final BudgetMapper budgetMapper;
    private final TransactionMapper transactionMapper;
    private final CategoryMapper categoryMapper;
    private final MessageProducer messageProducer;
    private final BudgetAlertMapper budgetAlertMapper;

    public BudgetServiceImpl(BudgetMapper budgetMapper,
                             TransactionMapper transactionMapper,
                             CategoryMapper categoryMapper,
                             MessageProducer messageProducer,
                             BudgetAlertMapper budgetAlertMapper) {
        this.budgetMapper = budgetMapper;
        this.transactionMapper = transactionMapper;
        this.categoryMapper = categoryMapper;
        this.messageProducer = messageProducer;
        this.budgetAlertMapper = budgetAlertMapper;
    }

    @Override
    public void setBudget(Long userId, BudgetSetRequest request) {
        // 校验分类归属（如果指定了 categoryId）
        if (request.getCategoryId() != null) {
            CategoryEntity category = categoryMapper.selectById(request.getCategoryId());
            if (category == null || !category.getUserId().equals(userId)) {
                throw new BusinessException("分类不存在");
            }
        }

        // 查询是否已有同用户+同分类+同月份的预算
        BudgetEntity existing = budgetMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BudgetEntity>()
                        .eq(BudgetEntity::getUserId, userId)
                        .eq(BudgetEntity::getBudgetMonth, request.getBudgetMonth())
                        .eq(request.getCategoryId() != null,
                                BudgetEntity::getCategoryId, request.getCategoryId())
                        .isNull(request.getCategoryId() == null,
                                BudgetEntity::getCategoryId)
                        .last("LIMIT 1")
        );

        if (existing != null) {
            // 更新已有预算
            existing.setAmount(request.getAmount());
            existing.setUpdateTime(LocalDateTime.now());
            budgetMapper.updateById(existing);
        } else {
            // 新建预算
            BudgetEntity budget = new BudgetEntity();
            budget.setUserId(userId);
            budget.setCategoryId(request.getCategoryId());
            budget.setAmount(request.getAmount());
            budget.setBudgetMonth(request.getBudgetMonth());
            budget.setCreateTime(LocalDateTime.now());
            budget.setUpdateTime(LocalDateTime.now());
            budgetMapper.insert(budget);
        }
    }

    @Override
    public List<BudgetVO> getBudgetList(Long userId, String budgetMonth) {
        YearMonth ym = YearMonth.parse(budgetMonth);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        // 查询当月所有预算
        List<BudgetEntity> budgets = budgetMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BudgetEntity>()
                        .eq(BudgetEntity::getUserId, userId)
                        .eq(BudgetEntity::getBudgetMonth, budgetMonth)
                        .orderByDesc(BudgetEntity::getCategoryId) // 总预算(categoryId=null)排在最后
        );

        // 查询当月总支出
        BigDecimal totalExpense = transactionMapper.sumByType(userId, null, 0, startDate, endDate);
        if (totalExpense == null) {
            totalExpense = BigDecimal.ZERO;
        }

        List<BudgetVO> result = new ArrayList<>();
        for (BudgetEntity budget : budgets) {
            BigDecimal used;
            String categoryName;

            if (budget.getCategoryId() == null) {
                // 总预算：使用当月总支出
                used = totalExpense;
                categoryName = "总预算";
            } else {
                // 分类预算：查询该分类（含子分类）当月支出
                List<Long> categoryIds = collectCategoryIds(userId, budget.getCategoryId());
                if (categoryIds.isEmpty()) {
                    used = BigDecimal.ZERO;
                } else {
                    used = transactionMapper.sumExpenseByCategories(userId, categoryIds, startDate, endDate);
                    if (used == null) {
                        used = BigDecimal.ZERO;
                    }
                }
                CategoryEntity category = categoryMapper.selectById(budget.getCategoryId());
                categoryName = category != null ? category.getName() : "未知分类";
            }

            BigDecimal remaining = budget.getAmount().subtract(used);
            BigDecimal percentage = budget.getAmount().compareTo(BigDecimal.ZERO) > 0
                    ? used.multiply(BigDecimal.valueOf(100)).divide(budget.getAmount(), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            String status;
            String alertLevel = null;
            if (percentage.compareTo(BigDecimal.valueOf(100)) > 0) {
                status = "danger";
                alertLevel = "danger";
            } else if (percentage.compareTo(BigDecimal.valueOf(80)) >= 0) {
                status = "warning";
                alertLevel = "warning";
            } else {
                status = "success";
            }

            // V9.1: 超阈值时异步发送预警消息（避免重复发送）
            if (alertLevel != null) {
                sendAlertIfNeeded(userId, budget, categoryName, used, percentage, alertLevel);
            }

            result.add(new BudgetVO(
                    budget.getId(),
                    budget.getCategoryId(),
                    categoryName,
                    budget.getAmount(),
                    used,
                    remaining,
                    percentage,
                    status
            ));
        }

        return result;
    }

    @Override
    public void deleteBudget(Long userId, Long budgetId) {
        BudgetEntity budget = budgetMapper.selectById(budgetId);
        if (budget == null || !budget.getUserId().equals(userId)) {
            throw new BusinessException("预算不存在");
        }
        budgetMapper.deleteById(budgetId);
    }

    /**
     * V9.1: 如果同一预算在同一月份、同一级别没有已发送的预警，则发送 MQ 消息。
     */
    private void sendAlertIfNeeded(Long userId, BudgetEntity budget, String categoryName,
                                     BigDecimal used, BigDecimal percentage, String alertLevel) {
        Long existingCount = budgetAlertMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<BudgetAlertEntity>()
                        .eq(BudgetAlertEntity::getBudgetId, budget.getId())
                        .eq(BudgetAlertEntity::getBudgetMonth, budget.getBudgetMonth())
                        .eq(BudgetAlertEntity::getAlertLevel, alertLevel)
        );
        if (existingCount != null && existingCount > 0) {
            return;
        }

        AlertMessage message = new AlertMessage();
        message.setUserId(userId);
        message.setBudgetId(budget.getId());
        message.setCategoryId(budget.getCategoryId());
        message.setCategoryName(categoryName);
        message.setBudgetAmount(budget.getAmount());
        message.setUsedAmount(used);
        message.setPercentage(percentage);
        message.setLevel(alertLevel);
        message.setMonth(budget.getBudgetMonth());
        message.setTimestamp(System.currentTimeMillis());

        messageProducer.sendAlertMessage(message);
    }

    /**
     * 收集指定分类及其所有子分类的 ID（支持两级分类树）。
     * 若分类本身就是叶子节点（parentId 非空），则只返回自身 ID。
     */
    private List<Long> collectCategoryIds(Long userId, Long categoryId) {
        List<Long> ids = new ArrayList<>();
        ids.add(categoryId);

        List<CategoryEntity> children = categoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CategoryEntity>()
                        .eq(CategoryEntity::getUserId, userId)
                        .eq(CategoryEntity::getParentId, categoryId)
                        .eq(CategoryEntity::getType, 0)
        );
        for (CategoryEntity child : children) {
            ids.add(child.getId());
        }

        return ids;
    }
}
