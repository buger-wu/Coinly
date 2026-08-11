<template>
  <div class="statistics-view" v-loading="loading">
    <!-- 月份选择栏 -->
    <div class="month-bar">
      <el-date-picker
        v-model="currentMonth"
        type="month"
        format="YYYY年MM月"
        value-format="YYYY-MM"
        :clearable="false"
        @change="handleMonthChange"
        style="width: 160px"
      />
      <span class="month-label">{{ currentMonth }} 收支统计</span>
    </div>

    <!-- 顶部：月度总览卡片 -->
    <el-row :gutter="20" class="section">
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-card-body">
            <div class="stat-icon stat-icon-income">
              <el-icon :size="28"><ArrowDown /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">月度收入</div>
              <div class="stat-value income">¥ {{ formatMoney(monthlyData.totalIncome) }}</div>
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
              <div class="stat-label">月度支出</div>
              <div class="stat-value expense">¥ {{ formatMoney(monthlyData.totalExpense) }}</div>
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
              <div class="stat-label">月度结余</div>
              <div class="stat-value" :class="monthlyData.netIncome < 0 ? 'expense' : 'balance'">
                ¥ {{ formatMoney(monthlyData.netIncome) }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 中部：分类占比 + 年度趋势 -->
    <el-row :gutter="20" class="section">
      <el-col :span="12">
        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="card-title">分类支出占比</span>
          </template>
          <div ref="categoryChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="card-title">{{ currentYear }}年收支趋势</span>
          </template>
          <div ref="yearlyChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import { ArrowDown, ArrowUp, Wallet } from '@element-plus/icons-vue'
import request from '@/utils/request'

interface MonthlyData {
  totalIncome: number
  totalExpense: number
  netIncome: number
}

interface CategoryItem {
  categoryName: string
  amount: number
  percentage: number
}

interface TrendItem {
  month: string
  income: number
  expense: number
}

const currentYear = new Date().getFullYear()
const currentMonth = ref((() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}`
})())

const monthlyData = ref<MonthlyData>({ totalIncome: 0, totalExpense: 0, netIncome: 0 })
const loading = ref(false)
const categoryChartRef = ref<HTMLElement>()
const yearlyChartRef = ref<HTMLElement>()

let categoryChart: echarts.ECharts | null = null
let yearlyChart: echarts.ECharts | null = null

const formatMoney = (value: number) => {
  return Number(value || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const fetchMonthlyData = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/v1/statistics/monthly', {
      params: { month: currentMonth.value }
    })
    monthlyData.value = res.data || {}
  } catch {
    /* 使用默认值 */
  } finally {
    loading.value = false
  }
}

const fetchCategory = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/v1/statistics/category', {
      params: { month: currentMonth.value }
    })
    await nextTick()
    initCategoryChart(res.data || [])
  } catch {
    await nextTick()
    initCategoryChart([])
  } finally {
    loading.value = false
  }
}

const fetchYearlyTrend = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/v1/statistics/yearly', {
      params: { year: String(currentYear) }
    })
    await nextTick()
    initYearlyChart(res.data || [])
  } catch {
    await nextTick()
    initYearlyChart([])
  } finally {
    loading.value = false
  }
}

const initCategoryChart = (data: CategoryItem[]) => {
  if (!categoryChartRef.value) return
  if (categoryChart) {
    categoryChart.dispose()
  }
  categoryChart = echarts.init(categoryChartRef.value)
  categoryChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: ¥{c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: '5%',
      top: 'middle'
    },
    series: [
      {
        name: '支出分类',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: false,
        label: {
          show: true,
          formatter: '{b}: {d}%'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '16',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: true
        },
        data: data.map((item) => ({
          name: item.categoryName,
          value: item.amount
        }))
      }
    ]
  })
}

const initYearlyChart = (data: TrendItem[]) => {
  if (!yearlyChartRef.value) return
  if (yearlyChart) {
    yearlyChart.dispose()
  }
  yearlyChart = echarts.init(yearlyChartRef.value)
  yearlyChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params: any[]) => {
        let html = params[0].axisValue + '<br/>'
        params.forEach((p) => {
          html += `${p.marker} ${p.seriesName}: ¥${formatMoney(p.value)}<br/>`
        })
        return html
      }
    },
    legend: {
      data: ['收入', '支出'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '12%',
      top: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.map((item) => item.month),
      boundaryGap: false
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: (val: number) => {
          if (val >= 10000) return val / 10000 + '万'
          return val.toString()
        }
      }
    },
    series: [
      {
        name: '收入',
        type: 'line',
        data: data.map((item) => item.income),
        smooth: true,
        itemStyle: { color: '#67c23a' },
        lineStyle: { width: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
            { offset: 1, color: 'rgba(103, 194, 58, 0)' }
          ])
        }
      },
      {
        name: '支出',
        type: 'line',
        data: data.map((item) => item.expense),
        smooth: true,
        itemStyle: { color: '#f56c6c' },
        lineStyle: { width: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(245, 108, 108, 0.3)' },
            { offset: 1, color: 'rgba(245, 108, 108, 0)' }
          ])
        }
      }
    ]
  })
}

const handleResize = () => {
  categoryChart?.resize()
  yearlyChart?.resize()
}

const handleMonthChange = async () => {
  await Promise.all([
    fetchMonthlyData(),
    fetchCategory()
  ])
}

onMounted(async () => {
  await Promise.all([
    fetchMonthlyData(),
    fetchCategory(),
    fetchYearlyTrend()
  ])
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  categoryChart?.dispose()
  yearlyChart?.dispose()
  categoryChart = null
  yearlyChart = null
})
</script>

<style scoped>
.statistics-view {
  max-width: 1400px;
  margin: 0 auto;
}

.month-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding: 12px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.month-label {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.section {
  margin-bottom: 20px;
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

.income {
  color: #67c23a;
}

.expense {
  color: #f56c6c;
}

.balance {
  color: #409eff;
}

.section-card {
  border-radius: 10px;
}

.card-title {
  font-weight: 600;
  font-size: 16px;
}

.chart-container {
  width: 100%;
  height: 320px;
}
</style>
