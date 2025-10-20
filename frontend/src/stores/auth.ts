import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

interface User {
  id: string
  username: string
  email: string
}

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string | null>(null)
  const loading = ref(false)

  const isAuthenticated = computed(() => !!token.value)

  const login = async (username: string, password: string) => {
    loading.value = true
    try {
      const response = await fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, password }),
      })

      if (!response.ok) {
        throw new Error('登录失败')
      }

      const data = await response.json()
      
      if (data.code === 200) {
        token.value = data.data.token
        user.value = {
          id: data.data.id,
          username: data.data.username,
          email: data.data.email
        }
        
        localStorage.setItem('token', token.value)
        localStorage.setItem('user', JSON.stringify(user.value))
        
        return { success: true }
      } else {
        throw new Error(data.message || '登录失败')
      }
    } catch (error) {
      console.error('登录错误:', error)
      return { success: false, error: error.message }
    } finally {
      loading.value = false
    }
  }

  const register = async (username: string, email: string, password: string) => {
    loading.value = true
    try {
      const response = await fetch('/api/v1/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ username, email, password }),
      })

      if (!response.ok) {
        throw new Error('注册失败')
      }

      const data = await response.json()
      
      if (data.code === 200) {
        return { success: true }
      } else {
        throw new Error(data.message || '注册失败')
      }
    } catch (error) {
      console.error('注册错误:', error)
      return { success: false, error: error.message }
    } finally {
      loading.value = false
    }
  }

  const logout = () => {
    user.value = null
    token.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  const initAuth = () => {
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('user')
    
    if (storedToken && storedUser) {
      token.value = storedToken
      user.value = JSON.parse(storedUser)
    }
  }

  const getAuthHeaders = () => {
    return {
      'Authorization': `Bearer ${token.value}`,
      'Content-Type': 'application/json',
    }
  }

  return {
    user,
    token,
    loading,
    isAuthenticated,
    login,
    register,
    logout,
    initAuth,
    getAuthHeaders
  }
})
