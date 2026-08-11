package com.coinly.business.budget.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.coinly.business.budget.entity.BudgetAlertEntity;
import com.coinly.business.budget.mapper.BudgetAlertMapper;
import com.coinly.business.budget.service.BudgetAlertService;
import com.coinly.business.budget.vo.BudgetAlertVO;
import com.coinly.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * V9.1: 预算预警记录服务实现。
 */
@Service
public class BudgetAlertServiceImpl implements BudgetAlertService {

    private final BudgetAlertMapper budgetAlertMapper;

    public BudgetAlertServiceImpl(BudgetAlertMapper budgetAlertMapper) {
        this.budgetAlertMapper = budgetAlertMapper;
    }

    @Override
    public List<BudgetAlertVO> listAlerts(Long userId, String budgetMonth) {
        LambdaQueryWrapper<BudgetAlertEntity> wrapper = new LambdaQueryWrapper<BudgetAlertEntity>()
                .eq(BudgetAlertEntity::getUserId, userId)
                .orderByDesc(BudgetAlertEntity::getCreateTime);
        if (budgetMonth != null && !budgetMonth.isEmpty()) {
            wrapper.eq(BudgetAlertEntity::getBudgetMonth, budgetMonth);
        }
        List<BudgetAlertEntity> entities = budgetAlertMapper.selectList(wrapper);
        return entities.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public void markRead(Long userId, Long alertId) {
        BudgetAlertEntity entity = budgetAlertMapper.selectById(alertId);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new BusinessException("预警记录不存在");
        }
        budgetAlertMapper.update(null, new LambdaUpdateWrapper<BudgetAlertEntity>()
                .eq(BudgetAlertEntity::getId, alertId)
                .set(BudgetAlertEntity::getIsRead, 1));
    }

    @Override
    public long countUnread(Long userId) {
        Long count = budgetAlertMapper.selectCount(
                new LambdaQueryWrapper<BudgetAlertEntity>()
                        .eq(BudgetAlertEntity::getUserId, userId)
                        .eq(BudgetAlertEntity::getIsRead, 0)
        );
        return count == null ? 0 : count;
    }

    private BudgetAlertVO convert(BudgetAlertEntity entity) {
        return new BudgetAlertVO(
                entity.getId(),
                entity.getBudgetId(),
                entity.getCategoryId(),
                entity.getCategoryName(),
                entity.getBudgetMonth(),
                entity.getBudgetAmount(),
                entity.getUsedAmount(),
                entity.getPercentage(),
                entity.getAlertLevel(),
                entity.getIsRead() != null && entity.getIsRead() == 1,
                entity.getCreateTime()
        );
    }
}
