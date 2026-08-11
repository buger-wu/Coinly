<template>
  <div class="budget-page">
    <div class="page-header">
      <div class="header-title">
        <el-icon :size="24" color="#409eff"><PieChart /></el-icon>
        <span>预算管理</span>
      </div>
      <div class="header-actions">
        <el-date-picker
          v-model="currentMonth"
          type="month"
          format="YYYY-MM"
          value-format="YYYY-MM"
          :clearable="false"
          @change="fetchBudgets"
          style="width: 140px"
        />
        <el-button type="primary" :icon="Plus" @click="handleSet">设置预算</el-button>
      </div>
    </div>

    <el-card shadow="never" class="budget-card" v-loading="loading">
      <el-empty v-if="budgets.length === 0" description="暂无预算，点击下方按钮开始管理" :image-size="80">
        <el-button type="primary" @click="handleSet">设置预算</el-button>
      </el-empty>

      <div v-else class="budget-list">
        <div v-for="item in budgets" :key="item.id" class="budget-item">
          <div class="budget-item-header">
            <div class="budget-name">
              <el-tag
                :type="item.categoryId === null ? 'primary' : 'info'"
                effect="plain"
                size="small"
              >
                {{ item.categoryId === null ? '总预算' : '分类' }}
              </el-tag>
              <span class="name-text">{{ item.categoryName }}</span>
            </div>
            <div class="budget-actions">
              <span class="budget-amount">¥ {{ formatMoney(item.amount) }}</span>
              <el-button link type="primary" :icon="Edit" @click="handleEdit(item)" />
              <el-button link type="danger" :icon="Delete" @click="handleDelete(item)" />
            </div>
          </div>

          <div class="budget-progress">
            <el-progress
              :percentage="Math.min(item.percentage, 100)"
              :color="getProgressColor(item.status)"
              :stroke-width="18"
              :text-inside="true"
              :format="() => `${item.percentage}%`"
            />
          </div>

          <div class="budget-details">
            <span class="detail-item">
              已使用: <span class="detail-value used">¥ {{ formatMoney(item.used) }}</span>
            </span>
            <span class="detail-item">
              剩余:
              <span class="detail-value" :class="item.remaining < 0 ? 'over' : 'remain'">
                ¥ {{ formatMoney(item.remaining) }}
              </span>
            </span>
            <span v-if="item.status === 'danger'" class="detail-item">
              <el-tag type="danger" effect="dark" size="small" round>已超支</el-tag>
            </span>
            <span v-else-if="item.status === 'warning'" class="detail-item">
              <el-tag type="warning" effect="dark" size="small" round>接近超支</el-tag>
            </span>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 设置预算弹窗 -->
    <el-dialog title="设置预算" v-model="dialogVisible" width="460px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="预算月份">
          <el-date-picker
            v-model="form.budgetMonth"
            type="month"
            format="YYYY-MM"
            value-format="YYYY-MM"
            :clearable="false"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="预算类型">
          <el-radio-group v-model="budgetType">
            <el-radio-button :value="'total'">总预算</el-radio-button>
            <el-radio-button :value="'category'">分类预算</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="budgetType === 'category'" label="选择分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择支出分类" filterable style="width: 100%">
            <el-option-group
              v-for="group in expenseCategoryGroups"
              :key="group.label"
              :label="group.label"
            >
              <el-option
                v-for="cat in group.options"
                :key="cat.id"
                :label="cat.name"
                :value="cat.id"
              />
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item label="预算金额" prop="amount">
          <el-input-number
            v-model="form.amount"
            :min="0.01"
            :precision="2"
            :step="100"
            placeholder="请输入预算金额"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus, Edit, Delete, PieChart } from '@element-plus/icons-vue'
import request from '@/utils/request'

interface Budget {
  id: number
  categoryId: number | null
  categoryName: string
  amount: number
  used: number
  remaining: number
  percentage: number
  status: string
}

interface Category {
  id: number
  name: string
  type: number
  parentId: number | null
}

const currentMonth = ref(new Date().toISOString().slice(0, 7))
const budgets = ref<Budget[]>([])
const categories = ref<Category[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const budgetType = ref<'total' | 'category'>('total')

const form = ref({
  categoryId: null as number | null,
  amount: 1000,
  budgetMonth: currentMonth.value
})

const rules = {
  amount: [{ required: true, message: '请输入预算金额', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const expenseCategoryGroups = computed(() => {
  const firstLevel = categories.value.filter(c => c.type === 0 && c.parentId === null)
  return firstLevel.map(parent => ({
    label: parent.name,
    options: [
      parent,
      ...categories.value.filter(c => c.type === 0 && c.parentId === parent.id)
    ]
  }))
})

function formatMoney(value: number) {
  return Number(value || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

function getProgressColor(status: string) {
  if (status === 'danger') return '#f56c6c'
  if (status === 'warning') return '#e6a23c'
  return '#67c23a'
}

async function fetchBudgets() {
  loading.value = true
  try {
    const res: any = await request.get('/v1/budgets', { params: { month: currentMonth.value } })
    budgets.value = res.data || []
  } catch {
    budgets.value = []
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  try {
    const res: any = await request.get('/v1/categories/all')
    categories.value = res.data || []
  } catch {
    categories.value = []
  }
}

function handleSet() {
  budgetType.value = 'total'
  form.value = {
    categoryId: null,
    amount: 1000,
    budgetMonth: currentMonth.value
  }
  dialogVisible.value = true
}

function handleEdit(item: Budget) {
  budgetType.value = item.categoryId === null ? 'total' : 'category'
  form.value = {
    categoryId: item.categoryId,
    amount: item.amount,
    budgetMonth: currentMonth.value
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (budgetType.value === 'category' && !form.value.categoryId) {
    ElMessage.warning('请选择分类')
    return
  }

  submitting.value = true
  try {
    const payload = {
      categoryId: budgetType.value === 'total' ? null : form.value.categoryId,
      amount: form.value.amount,
      budgetMonth: form.value.budgetMonth
    }
    await request.post('/v1/budgets', payload)
    ElMessage.success('预算设置成功')
    dialogVisible.value = false
    fetchBudgets()
  } catch (error) {
    console.error('设置预算失败:', error)
  } finally {
    submitting.value = false
  }
}

async function handleDelete(item: Budget) {
  try {
    await ElMessageBox.confirm(`确定要删除「${item.categoryName}」的预算吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await request.delete(`/v1/budgets/${item.id}`)
  ElMessage.success('删除成功')
  fetchBudgets()
}

onMounted(() => {
  fetchBudgets()
  fetchCategories()
})
</script>

<style scoped>
.budget-page {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
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

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.budget-card {
  border-radius: 8px;
  min-height: 300px;
}

.budget-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.budget-item {
  padding: 20px;
  background: #f7f8fa;
  border-radius: 10px;
  transition: background 0.2s;
}

.budget-item:hover {
  background: #f0f2f5;
}

.budget-item-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 14px;
}

.budget-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.name-text {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.budget-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.budget-amount {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-right: 8px;
}

.budget-progress {
  margin-bottom: 10px;
}

.budget-details {
  display: flex;
  gap: 20px;
  font-size: 13px;
  color: #909399;
}

.detail-value {
  font-weight: 600;
}

.detail-value.used {
  color: #f56c6c;
}

.detail-value.remain {
  color: #67c23a;
}

.detail-value.over {
  color: #f56c6c;
}
</style>
