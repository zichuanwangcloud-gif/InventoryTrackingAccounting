import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useAuthStore } from '@/stores/auth'

// Mock fetch
global.fetch = vi.fn()

describe('AuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()
  })

  describe('初始状态', () => {
    it('应该正确初始化默认状态', () => {
      const authStore = useAuthStore()
      
      expect(authStore.user).toBeNull()
      expect(authStore.token).toBeNull()
      expect(authStore.loading).toBe(false)
      expect(authStore.isAuthenticated).toBe(false)
    })
  })

  describe('登录功能', () => {
    it('应该成功登录并设置用户信息', async () => {
      const mockResponse = {
        code: 200,
        data: {
          token: 'mock-token',
          id: 'user-1',
          username: 'testuser',
          email: 'test@example.com'
        }
      }

      vi.mocked(fetch).mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockResponse)
      } as Response)

      const authStore = useAuthStore()
      const result = await authStore.login('testuser', 'password')

      expect(result.success).toBe(true)
      expect(authStore.token).toBe('mock-token')
      expect(authStore.user).toEqual({
        id: 'user-1',
        username: 'testuser',
        email: 'test@example.com'
      })
      expect(authStore.isAuthenticated).toBe(true)
      expect(localStorage.getItem('token')).toBe('mock-token')
    })

    it('应该处理登录失败', async () => {
      vi.mocked(fetch).mockResolvedValueOnce({
        ok: false,
        json: () => Promise.resolve({ code: 401, message: '认证失败' })
      } as Response)

      const authStore = useAuthStore()
      const result = await authStore.login('testuser', 'wrongpassword')

      expect(result.success).toBe(false)
      expect(result.error).toBe('登录失败')
      expect(authStore.isAuthenticated).toBe(false)
    })
  })

  describe('注册功能', () => {
    it('应该成功注册', async () => {
      const mockResponse = {
        code: 200,
        message: '注册成功'
      }

      vi.mocked(fetch).mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve(mockResponse)
      } as Response)

      const authStore = useAuthStore()
      const result = await authStore.register('newuser', 'new@example.com', 'password')

      expect(result.success).toBe(true)
    })

    it('应该处理注册失败', async () => {
      vi.mocked(fetch).mockResolvedValueOnce({
        ok: false,
        json: () => Promise.resolve({ code: 400, message: '用户名已存在' })
      } as Response)

      const authStore = useAuthStore()
      const result = await authStore.register('existinguser', 'existing@example.com', 'password')

      expect(result.success).toBe(false)
      expect(result.error).toBe('注册失败')
    })
  })

  describe('登出功能', () => {
    it('应该清除用户信息和本地存储', () => {
      const authStore = useAuthStore()
      
      // 先设置一些数据
      authStore.user = { id: '1', username: 'test', email: 'test@test.com' }
      authStore.token = 'some-token'
      localStorage.setItem('token', 'some-token')
      localStorage.setItem('user', JSON.stringify(authStore.user))

      authStore.logout()

      expect(authStore.user).toBeNull()
      expect(authStore.token).toBeNull()
      expect(authStore.isAuthenticated).toBe(false)
      expect(localStorage.getItem('token')).toBeNull()
      expect(localStorage.getItem('user')).toBeNull()
    })
  })

  describe('初始化认证', () => {
    it('应该从本地存储恢复用户状态', () => {
      const mockUser = { id: '1', username: 'test', email: 'test@test.com' }
      const mockToken = 'stored-token'
      
      localStorage.setItem('token', mockToken)
      localStorage.setItem('user', JSON.stringify(mockUser))

      const authStore = useAuthStore()
      authStore.initAuth()

      expect(authStore.token).toBe(mockToken)
      expect(authStore.user).toEqual(mockUser)
      expect(authStore.isAuthenticated).toBe(true)
    })
  })

  describe('认证头', () => {
    it('应该返回正确的认证头', () => {
      const authStore = useAuthStore()
      authStore.token = 'test-token'
      
      const headers = authStore.getAuthHeaders()
      
      expect(headers).toEqual({
        'Authorization': 'Bearer test-token',
        'Content-Type': 'application/json'
      })
    })
  })
})
