<template>
  <div class="category-page">
    <div class="page-header">
      <div class="header-title">
        <el-icon :size="24" color="#409eff"><Menu /></el-icon>
        <span>分类管理</span>
      </div>
      <el-button type="primary" :icon="Plus" @click="handleCreate">新增分类</el-button>
    </div>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never" class="type-card">
          <template #header>
            <div class="card-header">
              <span class="card-title expense-title">支出分类</span>
              <el-tag type="danger" effect="light" round>{{ expenseCategories.length }} 项</el-tag>
            </div>
          </template>
          <div v-loading="loading">
            <el-empty v-if="expenseCategories.length === 0" description="暂无支出分类" :image-size="80" />
            <div v-else class="category-list">
              <div v-for="cat in expenseCategories" :key="cat.id" class="category-item">
                <div class="category-info">
                  <span class="category-dot expense-dot"></span>
                  <span class="category-name">{{ cat.name }}</span>
                </div>
                <div class="category-actions">
                  <el-button link type="primary" :icon="Edit" @click="handleEdit(cat)" />
                  <el-button link type="danger" :icon="Delete" @click="handleDelete(cat)" />
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="never" class="type-card">
          <template #header>
            <div class="card-header">
              <span class="card-title income-title">收入分类</span>
              <el-tag type="success" effect="light" round>{{ incomeCategories.length }} 项</el-tag>
            </div>
          </template>
          <div v-loading="loading">
            <el-empty v-if="incomeCategories.length === 0" description="暂无收入分类" :image-size="80" />
            <div v-else class="category-list">
              <div v-for="cat in incomeCategories" :key="cat.id" class="category-item">
                <div class="category-info">
                  <span class="category-dot income-dot"></span>
                  <span class="category-name">{{ cat.name }}</span>
                </div>
                <div class="category-actions">
                  <el-button link type="primary" :icon="Edit" @click="handleEdit(cat)" />
                  <el-button link type="danger" :icon="Delete" @click="handleDelete(cat)" />
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="440px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" label-position="top">
        <el-form-item label="分类类型" prop="type">
          <el-radio-group v-model="form.type" size="large">
            <el-radio-button :value="0">支出</el-radio-button>
            <el-radio-button :value="1">收入</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：餐饮、交通" clearable />
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
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import request from '@/utils/request'

interface Category {
  id: number
  name: string
  type: number
}

const categories = ref<Category[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = ref({ id: 0, name: '', type: 0 })

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const expenseCategories = computed(() => categories.value.filter(c => c.type === 0))
const incomeCategories = computed(() => categories.value.filter(c => c.type === 1))

async function fetchCategories() {
  loading.value = true
  try {
    const res: any = await request.get('/v1/categories')
    categories.value = res.data || []
  } catch (error) {
    console.error('获取分类失败:', error)
  } finally {
    loading.value = false
  }
}

function handleCreate() {
  dialogTitle.value = '新增分类'
  form.value = { id: 0, name: '', type: 0 }
  dialogVisible.value = true
}

function handleEdit(cat: Category) {
  dialogTitle.value = '编辑分类'
  form.value = { id: cat.id, name: cat.name, type: cat.type }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (form.value.id) {
      await request.put(`/v1/categories/${form.value.id}`, {
        name: form.value.name,
        type: form.value.type
      })
      ElMessage.success('修改成功')
    } else {
      await request.post('/v1/categories', {
        name: form.value.name,
        type: form.value.type
      })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchCategories()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(cat: Category) {
  try {
    await ElMessageBox.confirm(`确定要删除分类「${cat.name}」吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  await request.delete(`/v1/categories/${cat.id}`)
  ElMessage.success('删除成功')
  fetchCategories()
}

onMounted(() => {
  fetchCategories()
})
</script>

<style scoped>
.category-page {
  max-width: 1200px;
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

.type-card {
  border-radius: 8px;
  min-height: 400px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
}

.expense-title { color: #f56c6c; }
.income-title { color: #67c23a; }

.category-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.category-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-radius: 8px;
  transition: background 0.2s;
}

.category-item:hover {
  background: #f5f7fa;
}

.category-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.category-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.expense-dot { background: #f56c6c; }
.income-dot { background: #67c23a; }

.category-name {
  font-size: 15px;
  color: #303133;
}

.category-actions {
  display: flex;
  gap: 4px;
  opacity: 0;
  transition: opacity 0.2s;
}

.category-item:hover .category-actions {
  opacity: 1;
}
</style>
