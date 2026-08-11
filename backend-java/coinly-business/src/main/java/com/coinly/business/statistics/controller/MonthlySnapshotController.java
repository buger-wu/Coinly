package com.coinly.business.statistics.controller;

import com.coinly.business.statistics.entity.MonthlySnapshotEntity;
import com.coinly.business.statistics.service.MonthlySnapshotService;
import com.coinly.common.context.UserContext;
import com.coinly.common.domain.CommonResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * V9.1: 月账单快照接口。
 */
@RestController
@RequestMapping("/api/v1/monthly-snapshots")
public class MonthlySnapshotController {

    private final MonthlySnapshotService monthlySnapshotService;

    public MonthlySnapshotController(MonthlySnapshotService monthlySnapshotService) {
        this.monthlySnapshotService = monthlySnapshotService;
    }

    @GetMapping
    public CommonResponse<List<MonthlySnapshotEntity>> list(@RequestParam(required = false) String month) {
        Long userId = UserContext.getUserId();
        return CommonResponse.success(monthlySnapshotService.list(userId, month));
    }

    @GetMapping("/{snapshotId}")
    public CommonResponse<MonthlySnapshotEntity> detail(@PathVariable Long snapshotId) {
        Long userId = UserContext.getUserId();
        return CommonResponse.success(monthlySnapshotService.getDetail(userId, snapshotId));
    }
}
