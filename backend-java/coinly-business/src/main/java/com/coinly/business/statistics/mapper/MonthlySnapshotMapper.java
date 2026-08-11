package com.coinly.business.statistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.coinly.business.statistics.entity.MonthlySnapshotEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * V9.1: 月账单快照 Mapper。
 */
@Mapper
public interface MonthlySnapshotMapper extends BaseMapper<MonthlySnapshotEntity> {
}
