<template>
  <div class="statistics-view">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-card-body">
            <div class="stat-icon stat-icon-income">
              <el-icon :size="28"><ArrowDown /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">本月收入</div>
              <div class="stat-value">¥ 12,000.00</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-card-body">
            <div class="stat-icon stat-icon-expense">
              <el-icon :size="28"><ArrowUp /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">本月支出</div>
              <div class="stat-value">¥ 8,500.00</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-card-body">
            <div class="stat-icon stat-icon-balance">
              <el-icon :size="28"><Wallet /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">本月结余</div>
              <div class="stat-value">¥ 3,500.00</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ArrowDown, ArrowUp, Wallet } from '@element-plus/icons-vue'
</script>

<style scoped>
.statistics-view {
  max-width: 1400px;
  margin: 0 auto;
}

.stat-card {
  border-radius: 10px;
}

.stat-card-body {
  display: flex;
  align-items: center;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: #fff;
  flex-shrink: 0;
}

.stat-icon-income {
  background: linear-gradient(135deg, #67c23a, #85ce61);
}

.stat-icon-expense {
  background: linear-gradient(135deg, #f56c6c, #f78989);
}

.stat-icon-balance {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}
</style>
