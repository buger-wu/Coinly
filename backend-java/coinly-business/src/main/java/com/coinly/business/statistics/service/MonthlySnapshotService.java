package com.coinly.business.statistics.service;

import com.coinly.business.statistics.entity.MonthlySnapshotEntity;

import java.util.List;

/**
 * V9.1: 月账单快照服务接口。
 */
public interface MonthlySnapshotService {

    List<MonthlySnapshotEntity> list(Long userId, String month);

    MonthlySnapshotEntity getDetail(Long userId, Long snapshotId);
}
