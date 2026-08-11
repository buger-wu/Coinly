import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'

interface UserInfo {
  id: number
  username: string
  nickname: string
  email: string
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(
    (() => {
      const stored = localStorage.getItem('userInfo')
      return stored ? JSON.parse(stored) : null
    })()
  )

  function setToken(val: string) {
    token.value = val
    localStorage.setItem('token', val)
  }

  function setUserInfo(info: UserInfo) {
    userInfo.value = info
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  async function logout() {
    // V7: 调用后端登出接口，将 Token 加入黑名单
    try {
      await request.post('/v1/auth/logout')
    } catch {
      // 即使后端调用失败，也要清除本地状态
    }
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  const isLoggedIn = () => !!token.value

  return { token, userInfo, setToken, setUserInfo, logout, isLoggedIn }
})
