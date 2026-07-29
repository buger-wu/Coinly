<template>
  <div class="transaction-list">
    <div class="page-header">
      <div class="header-title">
        <el-icon :size="24" color="#409eff"><List /></el-icon>
        <span>交易记录</span>
      </div>
      <div class="header-actions">
        <el-button :icon="Download" @click="handleExport" :disabled="transactions.length === 0">导出 CSV</el-button>
        <el-button type="primary" :icon="Plus" @click="handleCreate">记一笔</el-button>
      </div>
    </div>

    <el-card class="filter-card" shadow="never">
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="类型">
          <el-select v-model="queryForm.type" placeholder="全部" clearable style="width: 120px">
            <el-option label="支出" :value="0">
              <span style="color: #f56c6c">⬇ 支出</span>
            </el-option>
            <el-option label="收入" :value="1">
              <span style="color: #67c23a">⬆ 收入</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="queryForm.categoryId" placeholder="全部" clearable style="width: 150px" filterable>
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 280px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="fetchTransactions">查询</el-button>
          <el-button :icon="RefreshLeft" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never" v-loading="loading">
      <el-empty v-if="!loading && transactions.length === 0" description="暂无交易记录">
        <el-button type="primary" @click="handleCreate">记一笔</el-button>
      </el-empty>
      <div v-else>
        <el-table :data="transactions" stripe style="width: 100%">
          <el-table-column prop="transactionDate" label="日期" width="120" align="center" />
          <el-table-column prop="type" label="类型" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.type === 0 ? 'danger' : 'success'" effect="light" round>
                {{ row.type === 0 ? '⬇ 支出' : '⬆ 收入' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="categoryName" label="分类" width="140" align="center" />
          <el-table-column prop="amount" label="金额" width="140" align="right">
            <template #default="{ row }">
              <span class="amount-text" :class="row.type === 0 ? 'expense' : 'income'">
                {{ row.type === 0 ? '-' : '+' }}¥{{ row.amount }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
          <el-table-column label="操作" width="160" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
              <el-button link type="danger" :icon="Delete" @click="handleDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchTransactions"
          @current-change="fetchTransactions"
          style="margin-top: 20px"
          background
        />
      </div>
    </el-card>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="540px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" label-position="top">
        <el-form-item label="交易类型" prop="type">
          <el-radio-group v-model="form.type" size="large">
            <el-radio-button :value="0">
              <el-icon><Bottom /></el-icon>
              支出
            </el-radio-button>
            <el-radio-button :value="1">
              <el-icon><Top /></el-icon>
              收入
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%" filterable>
            <template v-for="parent in parentCategories.filter(c => c.type === form.type)" :key="parent.id">
              <el-option-group v-if="getChildren(parent.id).length > 0" :label="parent.name">
                <el-option
                  v-for="child in getChildren(parent.id)"
                  :key="child.id"
                  :label="child.name"
                  :value="child.id"
                />
              </el-option-group>
              <el-option v-else :label="parent.name" :value="parent.id" />
            </template>
          </el-select>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input v-model.number="form.amount" placeholder="请输入金额" size="large">
            <template #prefix>¥</template>
          </el-input>
        </el-form-item>
        <el-form-item label="交易日期" prop="transactionDate">
          <el-date-picker
            v-model="form.transactionDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="添加备注信息（选填）" />
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
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus, Edit, Delete, Search, RefreshLeft, List, Top, Bottom, Download } from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const bookId = route.params.bookId

interface Transaction {
  id: number
  type: string
  categoryId: number
  categoryName: string
  amount: number
  transactionDate: string
  remark: string
}

interface Category {
  id: number
  name: string
  type: number
  parentId: number | null
}

const transactions = ref<Transaction[]>([])
const categories = ref<Category[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const submitting = ref(false)

const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

const queryForm = ref({
  type: null as number | null,
  categoryId: null as number | null,
  dateRange: [] as string[]
})

const form = ref({
  id: 0,
  type: 0,
  categoryId: null as number | null,
  amount: null as number | null,
  transactionDate: new Date().toISOString().split('T')[0],
  remark: ''
})

const rules = {
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  amount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额必须大于0', trigger: 'blur' }
  ],
  transactionDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

async function fetchTransactions() {
  loading.value = true
  try {
    const params: any = {
      page: pagination.value.page,
      size: pagination.value.size
    }
    if (queryForm.value.type) params.type = queryForm.value.type
    if (queryForm.value.categoryId) params.categoryId = queryForm.value.categoryId
    if (queryForm.value.dateRange?.length === 2) {
      params.startDate = queryForm.value.dateRange[0]
      params.endDate = queryForm.value.dateRange[1]
    }
    const res: any = await request.get(`/v1/books/${bookId}/transactions`, { params })
    transactions.value = res.data.records
    pagination.value.total = res.data.total
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  const res: any = await request.get('/v1/categories/all')
  categories.value = res.data
}

const parentCategories = computed(() => categories.value.filter(c => c.parentId === null || c.parentId === undefined))

function getChildren(parentId: number) {
  return categories.value.filter(c => c.parentId === parentId)
}

function handleCreate() {
  dialogTitle.value = '记一笔'
  form.value = {
    id: 0,
    type: 0,
    categoryId: null,
    amount: null,
    transactionDate: new Date().toISOString().split('T')[0],
    remark: ''
  }
  dialogVisible.value = true
}

function handleEdit(transaction: Transaction) {
  dialogTitle.value = '编辑交易'
  form.value = {
    id: transaction.id,
    type: transaction.type,
    categoryId: transaction.categoryId,
    amount: transaction.amount,
    transactionDate: transaction.transactionDate,
    remark: transaction.remark
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (form.value.id) {
      await request.put(`/v1/books/${bookId}/transactions/${form.value.id}`, form.value)
      ElMessage.success('修改成功')
    } else {
      await request.post(`/v1/books/${bookId}/transactions`, form.value)
      ElMessage.success('记账成功')
    }
    dialogVisible.value = false
    fetchTransactions()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定要删除此交易记录吗？', '提示', { type: 'warning' })
  await request.delete(`/v1/books/${bookId}/transactions/${id}`)
  ElMessage.success('删除成功')
  fetchTransactions()
}

function resetQuery() {
  queryForm.value = {
    type: null,
    categoryId: null,
    dateRange: []
  }
  pagination.value.page = 1
  fetchTransactions()
}

function handleExport() {
  const headers = ['日期', '类型', '分类', '金额', '备注']
  const rows = transactions.value.map(t => [
    t.transactionDate,
    t.type === 0 ? '支出' : '收入',
    t.categoryName || '',
    t.amount,
    (t.remark || '').replace(/,/g, '，')
  ])

  const BOM = '\uFEFF'
  const csv = BOM + [headers.join(','), ...rows.map(r => r.join(','))].join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `交易记录_${new Date().toISOString().slice(0, 10)}.csv`
  link.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

onMounted(() => {
  fetchCategories()
  fetchTransactions()
})
</script>

<style scoped>
.transaction-list {
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
  margin-bottom: 16px;
  border-radius: 8px;
}

.filter-form {
  margin-bottom: -18px;
}

.table-card {
  border-radius: 8px;
}

.amount-text {
  font-weight: 600;
  font-size: 15px;
}

.amount-text.expense {
  color: #f56c6c;
}

.amount-text.income {
  color: #67c23a;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background: #fafafa;
  font-weight: 600;
}

:deep(.el-pagination) {
  justify-content: flex-end;
}
</style>
