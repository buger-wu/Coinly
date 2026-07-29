<template>
  <div class="profile">
    <el-card>
      <template #header>
        <div style="font-weight: bold">个人信息</div>
      </template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" style="max-width: 500px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, type FormInstance } from 'element-plus'
import { useUserStore } from '@/stores/user'
import request from '@/utils/request'

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const submitting = ref(false)

const form = ref({
  username: '',
  nickname: '',
  email: ''
})

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }]
}

async function fetchProfile() {
  const res: any = await request.get('/v1/user/profile')
  form.value = res.data
}

async function handleSubmit() {
  await formRef.value?.validate()
  submitting.value = true
  try {
    await request.put('/v1/user/profile', {
      nickname: form.value.nickname,
      email: form.value.email
    })
    ElMessage.success('保存成功')
    userStore.setUserInfo({
      id: userStore.userInfo?.id || 0,
      username: form.value.username,
      nickname: form.value.nickname,
      email: form.value.email
    })
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.profile {
  padding: 20px;
}
</style>
