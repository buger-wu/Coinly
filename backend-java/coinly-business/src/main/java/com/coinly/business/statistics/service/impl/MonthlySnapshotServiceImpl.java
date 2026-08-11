package com.coinly.business.statistics.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coinly.business.statistics.entity.MonthlySnapshotEntity;
import com.coinly.business.statistics.mapper.MonthlySnapshotMapper;
import com.coinly.business.statistics.service.MonthlySnapshotService;
import com.coinly.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * V9.1: 月账单快照服务实现。
 */
@Service
public class MonthlySnapshotServiceImpl implements MonthlySnapshotService {

    private final MonthlySnapshotMapper monthlySnapshotMapper;

    public MonthlySnapshotServiceImpl(MonthlySnapshotMapper monthlySnapshotMapper) {
        this.monthlySnapshotMapper = monthlySnapshotMapper;
    }

    @Override
    public List<MonthlySnapshotEntity> list(Long userId, String month) {
        LambdaQueryWrapper<MonthlySnapshotEntity> wrapper = new LambdaQueryWrapper<MonthlySnapshotEntity>()
                .eq(MonthlySnapshotEntity::getUserId, userId)
                .orderByDesc(MonthlySnapshotEntity::getSnapshotMonth);
        if (month != null && !month.isEmpty()) {
            wrapper.eq(MonthlySnapshotEntity::getSnapshotMonth, month);
        }
        return monthlySnapshotMapper.selectList(wrapper);
    }

    @Override
    public MonthlySnapshotEntity getDetail(Long userId, Long snapshotId) {
        MonthlySnapshotEntity entity = monthlySnapshotMapper.selectById(snapshotId);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new BusinessException("月账单不存在");
        }
        return entity;
    }
}
