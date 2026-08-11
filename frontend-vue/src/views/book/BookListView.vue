<template>
  <div class="book-list">
    <div class="page-header">
      <div class="header-title">
        <el-icon :size="24" color="#409eff"><Wallet /></el-icon>
        <span>我的账本</span>
      </div>
      <el-button type="primary" :icon="Plus" @click="handleCreate">新建账本</el-button>
    </div>

    <div v-loading="loading" class="content-area">
      <el-empty v-if="!loading && books.length === 0" description="还没有账本，创建一个开始记账吧">
        <el-button type="primary" @click="handleCreate">创建账本</el-button>
      </el-empty>

      <el-row :gutter="20" v-else>
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="book in books" :key="book.id">
          <el-card class="book-card" shadow="hover" @click="goToTransactions(book.id)">
            <div class="book-icon">
              <el-icon :size="32" color="#409eff"><Notebook /></el-icon>
            </div>
            <div class="book-name">{{ book.name }}</div>
            <div class="book-desc">{{ book.description || '暂无描述' }}</div>
            <div class="book-footer">
              <span class="book-tip">点击查看明细</span>
              <div class="book-actions" @click.stop>
                <el-button :icon="Edit" circle size="small" @click="handleEdit(book)" />
                <el-button :icon="Delete" circle size="small" type="danger" @click="handleDelete(book.id)" />
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px" :close-on-click-modal="false">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" label-position="top">
        <el-form-item label="账本名称" prop="name">
          <el-input v-model="form.name" placeholder="例如：日常开销、旅游基金" clearable />
        </el-form-item>
        <el-form-item label="账本描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="添加一些描述，方便记忆（选填）"
            clearable
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { Plus, Edit, Delete, Notebook } from '@element-plus/icons-vue'
import request from '@/utils/request'

interface Book {
  id: number
  name: string
  description: string
}

const router = useRouter()
const books = ref<Book[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = ref({
  id: 0,
  name: '',
  description: ''
})

const rules = {
  name: [{ required: true, message: '请输入账本名称', trigger: 'blur' }]
}

async function fetchBooks() {
  loading.value = true
  try {
    const res: any = await request.get('/v1/books')
    books.value = res.data || []
  } catch (error) {
    console.error('获取账本失败:', error)
  } finally {
    loading.value = false
  }
}

function handleCreate() {
  dialogTitle.value = '新建账本'
  form.value = { id: 0, name: '', description: '' }
  dialogVisible.value = true
}

function handleEdit(book: Book) {
  dialogTitle.value = '编辑账本'
  form.value = { id: book.id, name: book.name, description: book.description }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    if (form.value.id) {
      await request.put(`/v1/books/${form.value.id}`, {
        name: form.value.name,
        description: form.value.description
      })
      ElMessage.success('修改成功')
    } else {
      await request.post('/v1/books', {
        name: form.value.name,
        description: form.value.description
      })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchBooks()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定要删除此账本吗？', '提示', {
      type: 'warning'
    })
  } catch {
    return
  }
  await request.delete(`/v1/books/${id}`)
  ElMessage.success('删除成功')
  fetchBooks()
}

function goToTransactions(bookId: number) {
  router.push(`/books/${bookId}/transactions`)
}

onMounted(() => {
  fetchBooks()
})
</script>

<style scoped>
.book-list {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
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

.content-area {
  min-height: 400px;
}

.book-card {
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 20px;
  border-radius: 12px;
  overflow: hidden;
}

.book-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.15);
}

.book-card:hover .book-icon {
  transform: scale(1.1);
}

.book-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 64px;
  height: 64px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  border-radius: 16px;
  transition: transform 0.3s ease;
}

.book-name {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-desc {
  color: #909399;
  font-size: 13px;
  text-align: center;
  margin-bottom: 16px;
  min-height: 20px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.book-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.book-tip {
  font-size: 12px;
  color: #c0c4cc;
}

.book-actions {
  display: flex;
  gap: 8px;
}

:deep(.el-card__body) {
  padding: 24px;
}
</style>
