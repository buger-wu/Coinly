<template>
  <div class="statistics">
    <div class="page-header">
      <div class="header-title">
        <el-icon :size="24" color="#409eff"><TrendCharts /></el-icon>
        <span>统计分析</span>
      </div>
    </div>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="账本">
          <el-select v-model="queryForm.bookId" placeholder="全部账本" clearable style="width: 160px">
            <el-option v-for="book in books" :key="book.id" :label="book.name" :value="book.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="月份">
          <el-date-picker v-model="queryForm.month" type="month" placeholder="选择月份" value-format="YYYY-MM" style="width: 160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="fetchStatistics">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon income-icon"><el-icon :size="28"><Top /></el-icon></div>
          <div class="stat-info">
            <div class="stat-label">本月收入</div>
            <div class="stat-value income">¥{{ monthlySummary.totalIncome ?? '0.00' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon expense-icon"><el-icon :size="28"><Bottom /></el-icon></div>
          <div class="stat-info">
            <div class="stat-label">本月支出</div>
            <div class="stat-value expense">¥{{ monthlySummary.totalExpense ?? '0.00' }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon" :class="(monthlySummary.balance ?? 0) >= 0 ? 'balance-pos-icon' : 'balance-neg-icon'">
            <el-icon :size="28"><Wallet /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-label">本月结余</div>
            <div class="stat-value" :class="(monthlySummary.balance ?? 0) >= 0 ? 'income' : 'expense'">
              {{ (monthlySummary.balance ?? 0) >= 0 ? '+' : '' }}¥{{ monthlySummary.balance ?? '0.00' }}
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">支出分类占比</span>
              <span class="chart-subtitle">{{ queryForm.month }}</span>
            </div>
          </template>
          <el-empty v-if="categoryStats.length === 0" description="暂无支出数据" :image-size="100" />
          <div v-else ref="pieChartRef" style="width: 100%; height: 360px" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="chart-card">
          <template #header>
            <div class="chart-header">
              <span class="chart-title">年度收支趋势</span>
              <span class="chart-subtitle">{{ currentYear }}</span>
            </div>
          </template>
          <div ref="lineChartRef" style="width: 100%; height: 360px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Top, Bottom } from '@element-plus/icons-vue'
import request from '@/utils/request'
import * as echarts from 'echarts'

interface Book { id: number; name: string }
interface MonthlySummary { totalIncome: number; totalExpense: number; balance: number }
interface CategoryStat { categoryName: string; amount: number; percentage: number }
interface TrendItem { month: string; income: number; expense: number }

const books = ref<Book[]>([])
const pieChartRef = ref<HTMLElement>()
const lineChartRef = ref<HTMLElement>()
let pieChart: echarts.ECharts | null = null
let lineChart: echarts.ECharts | null = null

const queryForm = ref({
  bookId: null as number | null,
  month: new Date().toISOString().slice(0, 7)
})

const currentYear = computed(() => queryForm.value.month.slice(0, 4))
const monthlySummary = ref<Partial<MonthlySummary>>({})
const categoryStats = ref<CategoryStat[]>([])
const yearlyTrend = ref<TrendItem[]>([])

async function fetchBooks() {
  try {
    const res: any = await request.get('/v1/books')
    books.value = res.data
  } catch {}
}

async function fetchStatistics() {
  try {
    const params: any = { month: queryForm.value.month }
    if (queryForm.value.bookId) params.bookId = queryForm.value.bookId

    const yearParams: any = { year: currentYear.value }
    if (queryForm.value.bookId) yearParams.bookId = queryForm.value.bookId

    const [summaryRes, categoryRes, trendRes]: any = await Promise.all([
      request.get('/v1/statistics/monthly', { params }),
      request.get('/v1/statistics/category', { params }),
      request.get('/v1/statistics/yearly', { params: yearParams })
    ])

    monthlySummary.value = summaryRes.data
    categoryStats.value = categoryRes.data
    yearlyTrend.value = trendRes.data

    await nextTick()
    renderPieChart()
    renderLineChart()
  } catch {
    ElMessage.error('获取统计数据失败')
  }
}

const PIE_COLORS = [
  '#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de',
  '#3ba272', '#fc8452', '#9a60b4', '#ea7ccc', '#48b8d0'
]

function renderPieChart() {
  if (!pieChartRef.value) return
  if (!pieChart) pieChart = echarts.init(pieChartRef.value)

  pieChart.setOption({
    color: PIE_COLORS,
    tooltip: {
      trigger: 'item',
      formatter: (p: any) => `${p.name}<br/>¥${p.value} (${p.percent}%)`
    },
    legend: { orient: 'vertical', right: '5%', top: 'center', itemWidth: 12, itemHeight: 12 },
    series: [{
      type: 'pie',
      radius: ['40%', '68%'],
      center: ['38%', '50%'],
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%', fontSize: 12 },
      data: categoryStats.value.map(item => ({ name: item.categoryName, value: item.amount }))
    }]
  })
}

function renderLineChart() {
  if (!lineChartRef.value) return
  if (!lineChart) lineChart = echarts.init(lineChartRef.value)

  const months = yearlyTrend.value.map(i => i.month.slice(5) + '月')
  const incomeData = yearlyTrend.value.map(i => i.income)
  const expenseData = yearlyTrend.value.map(i => i.expense)

  lineChart.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        let html = `${params[0].axisValue}<br/>`
        params.forEach((p: any) => {
          html += `${p.marker} ${p.seriesName}: ¥${p.value}<br/>`
        })
        return html
      }
    },
    legend: { top: 0, data: ['收入', '支出'] },
    grid: { top: 40, left: 60, right: 20, bottom: 30 },
    xAxis: { type: 'category', data: months, axisLabel: { fontSize: 12 } },
    yAxis: { type: 'value', axisLabel: { formatter: '¥{value}' } },
    series: [
      {
        name: '收入',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 3 },
        itemStyle: { color: '#67c23a' },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(103, 194, 58, 0.25)' },
          { offset: 1, color: 'rgba(103, 194, 58, 0.02)' }
        ])},
        data: incomeData
      },
      {
        name: '支出',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 3 },
        itemStyle: { color: '#f56c6c' },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(245, 108, 108, 0.25)' },
          { offset: 1, color: 'rgba(245, 108, 108, 0.02)' }
        ])},
        data: expenseData
      }
    ]
  })
}

onMounted(() => {
  fetchBooks()
  fetchStatistics()
  window.addEventListener('resize', () => {
    pieChart?.resize()
    lineChart?.resize()
  })
})
</script>

<style scoped>
.statistics {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.header-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.filter-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.stat-card {
  border-radius: 12px;
  overflow: hidden;
}

:deep(.stat-card .el-card__body) {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 24px;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 60px;
  height: 60px;
  border-radius: 16px;
  flex-shrink: 0;
}

.income-icon { background: linear-gradient(135deg, #d4f7e0, #a8edbc); color: #27ae60; }
.expense-icon { background: linear-gradient(135deg, #fde8e8, #f9c0c0); color: #e74c3c; }
.balance-pos-icon { background: linear-gradient(135deg, #dbeafe, #bfdbfe); color: #3b82f6; }
.balance-neg-icon { background: linear-gradient(135deg, #fef3c7, #fde68a); color: #d97706; }

.stat-info { flex: 1; }
.stat-label { font-size: 14px; color: #909399; margin-bottom: 8px; }
.stat-value { font-size: 28px; font-weight: 700; line-height: 1; }
.stat-value.income { color: #27ae60; }
.stat-value.expense { color: #e74c3c; }

.chart-card { border-radius: 8px; }

.chart-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.chart-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-subtitle {
  font-size: 13px;
  color: #909399;
  background: #f5f7fa;
  padding: 2px 10px;
  border-radius: 12px;
}
</style>
