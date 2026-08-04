<template>
  <div class="dashboard-view">
    <!-- 顶部：本月总览卡片 -->
    <el-row :gutter="20" class="section">
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-card-body">
            <div class="stat-icon stat-icon-income">
              <el-icon :size="28"><ArrowDown /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">本月收入</div>
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
              <div class="stat-label">本月支出</div>
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
              <div class="stat-label">本月结余</div>
              <div class="stat-value" :class="monthlyData.netIncome < 0 ? 'expense' : 'balance'">
                ¥ {{ formatMoney(monthlyData.netIncome) }}
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 中部：账本余额 + 收支趋势 -->
    <el-row :gutter="20" class="section">
      <el-col :span="12">
        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="card-title">账本余额</span>
          </template>
          <el-row v-if="balances.length > 0" :gutter="12">
            <el-col
              v-for="book in balances"
              :key="book.bookId"
              :span="12"
              class="book-col"
            >
              <div class="book-card">
                <div class="book-name">{{ book.bookName }}</div>
                <div class="book-details">
                  <div class="book-detail">
                    <span class="detail-label">收入</span>
                    <span class="detail-value income">¥ {{ formatMoney(book.totalIncome) }}</span>
                  </div>
                  <div class="book-detail">
                    <span class="detail-label">支出</span>
                    <span class="detail-value expense">¥ {{ formatMoney(book.totalExpense) }}</span>
                  </div>
                  <div class="book-detail">
                    <span class="detail-label">余额</span>
                    <span
                      class="detail-value"
                      :class="book.totalIncome - book.totalExpense >= 0 ? 'income' : 'expense'"
                    >
                      ¥ {{ formatMoney(book.totalIncome - book.totalExpense) }}
                    </span>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
          <el-empty v-else description="暂无账本数据" :image-size="60" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="card-title">近6个月收支趋势</span>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部：分类支出占比 -->
    <el-row :gutter="20" class="section">
      <el-col :span="24">
        <el-card shadow="never" class="section-card">
          <template #header>
            <span class="card-title">分类支出占比</span>
          </template>
          <div ref="categoryChartRef" class="chart-container"></div>
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

interface BookBalance {
  bookId: number
  bookName: string
  totalIncome: number
  totalExpense: number
}

interface TrendItem {
  month: string
  income: number
  expense: number
}

interface CategoryItem {
  categoryName: string
  amount: number
  percentage: number
}

const currentMonth = (() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  return `${year}-${month}`
})()

const monthlyData = ref<MonthlyData>({ totalIncome: 0, totalExpense: 0, netIncome: 0 })
const balances = ref<BookBalance[]>([])
const trendChartRef = ref<HTMLElement>()
const categoryChartRef = ref<HTMLElement>()

let trendChart: echarts.ECharts | null = null
let categoryChart: echarts.ECharts | null = null

const formatMoney = (value: number) => {
  return Number(value || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const fetchMonthlyData = async () => {
  try {
    const data = await request.get<any, MonthlyData>('/v1/statistics/monthly', {
      params: { month: currentMonth }
    })
    monthlyData.value = data
  } catch {
    /* 使用默认值 */
  }
}

const fetchBalances = async () => {
  try {
    const data = await request.get<any, BookBalance[]>('/v1/statistics/balances')
    balances.value = data || []
  } catch {
    /* 使用默认空数组 */
  }
}

const fetchTrend = async () => {
  try {
    const data = await request.get<any, TrendItem[]>('/v1/statistics/recent-trend', {
      params: { months: 6 }
    })
    await nextTick()
    initTrendChart(data || [])
  } catch {
    await nextTick()
    initTrendChart([])
  }
}

const fetchCategory = async () => {
  try {
    const data = await request.get<any, CategoryItem[]>('/v1/statistics/category', {
      params: { month: currentMonth }
    })
    await nextTick()
    initCategoryChart(data || [])
  } catch {
    await nextTick()
    initCategoryChart([])
  }
}

const initTrendChart = (data: TrendItem[]) => {
  if (!trendChartRef.value) return
  if (trendChart) {
    trendChart.dispose()
  }
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
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

const handleResize = () => {
  trendChart?.resize()
  categoryChart?.resize()
}

onMounted(async () => {
  await Promise.all([
    fetchMonthlyData(),
    fetchBalances(),
    fetchTrend(),
    fetchCategory()
  ])
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  categoryChart?.dispose()
  trendChart = null
  categoryChart = null
})
</script>

<style scoped>
.dashboard-view {
  max-width: 1400px;
  margin: 0 auto;
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

.book-col {
  margin-bottom: 12px;
}

.book-card {
  background: #f7f8fa;
  border-radius: 8px;
  padding: 14px;
}

.book-name {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.book-details {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.book-detail {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}

.detail-label {
  color: #909399;
}

.detail-value {
  font-weight: 500;
}
</style>
