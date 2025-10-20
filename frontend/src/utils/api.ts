import { useAuthStore } from '../stores/auth'

const API_BASE_URL = import.meta.env.VITE_API_BASE || '/api'

export class ApiClient {
  private static instance: ApiClient
  private authStore = useAuthStore()

  static getInstance(): ApiClient {
    if (!ApiClient.instance) {
      ApiClient.instance = new ApiClient()
    }
    return ApiClient.instance
  }

  private async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<{ code: number; message: string; data: T }> {
    const url = `${API_BASE_URL}${endpoint}`
    
    const defaultHeaders: HeadersInit = {
      'Content-Type': 'application/json',
    }

    // 添加认证头
    if (this.authStore.token) {
      defaultHeaders['Authorization'] = `Bearer ${this.authStore.token}`
    }

    const config: RequestInit = {
      ...options,
      headers: {
        ...defaultHeaders,
        ...options.headers,
      },
    }

    try {
      const response = await fetch(url, config)
      
      if (!response.ok) {
        if (response.status === 401) {
          // Token过期，清除认证信息
          this.authStore.logout()
          throw new Error('认证已过期，请重新登录')
        }
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }

      const data = await response.json()
      return data
    } catch (error) {
      console.error('API请求错误:', error)
      throw error
    }
  }

  async get<T>(endpoint: string, params?: Record<string, any>): Promise<T> {
    let url = endpoint
    if (params) {
      const searchParams = new URLSearchParams()
      Object.entries(params).forEach(([key, value]) => {
        if (value !== undefined && value !== null) {
          searchParams.append(key, String(value))
        }
      })
      url += `?${searchParams.toString()}`
    }

    const response = await this.request<T>(url, { method: 'GET' })
    return response.data
  }

  async post<T>(endpoint: string, data?: any): Promise<T> {
    const response = await this.request<T>(endpoint, {
      method: 'POST',
      body: data ? JSON.stringify(data) : undefined,
    })
    return response.data
  }

  async put<T>(endpoint: string, data?: any): Promise<T> {
    const response = await this.request<T>(endpoint, {
      method: 'PUT',
      body: data ? JSON.stringify(data) : undefined,
    })
    return response.data
  }

  async delete<T>(endpoint: string): Promise<T> {
    const response = await this.request<T>(endpoint, { method: 'DELETE' })
    return response.data
  }

  async upload<T>(endpoint: string, file: File): Promise<T> {
    const formData = new FormData()
    formData.append('file', file)

    const response = await this.request<T>(endpoint, {
      method: 'POST',
      body: formData,
      headers: {
        // 不设置Content-Type，让浏览器自动设置multipart/form-data
      },
    })
    return response.data
  }
}

export const api = ApiClient.getInstance()

// 导出常用的API方法
export const authApi = {
  login: (username: string, password: string) =>
    api.post('/v1/auth/login', { username, password }),
  
  register: (username: string, email: string, password: string) =>
    api.post('/v1/auth/register', { username, email, password }),
  
  getCurrentUser: () => api.get('/v1/auth/me'),
}

export const itemsApi = {
  getItems: (params?: any) => api.get('/v1/items', params),
  getItem: (id: string) => api.get(`/v1/items/${id}`),
  createItem: (data: any) => api.post('/v1/items', data),
  updateItem: (id: string, data: any) => api.put(`/v1/items/${id}`, data),
  deleteItem: (id: string) => api.delete(`/v1/items/${id}`),
  getStats: () => api.get('/v1/items/stats'),
}

export const transactionsApi = {
  getTransactions: (params?: any) => api.get('/v1/transactions', params),
  getTransaction: (id: string) => api.get(`/v1/transactions/${id}`),
  getStats: (params?: any) => api.get('/v1/transactions/stats', params),
}

export const reportsApi = {
  getInventoryValue: (params?: any) => api.get('/v1/reports/inventory-value', params),
  getDisposalProfit: (params?: any) => api.get('/v1/reports/disposal-profit', params),
  getTrends: (params?: any) => api.get('/v1/reports/trends', params),
}

export const accountsApi = {
  getAccounts: () => api.get('/v1/accounts'),
  getAccount: (id: string) => api.get(`/v1/accounts/${id}`),
  createAccount: (data: any) => api.post('/v1/accounts', data),
  updateAccount: (id: string, data: any) => api.put(`/v1/accounts/${id}`, data),
  deleteAccount: (id: string) => api.delete(`/v1/accounts/${id}`),
}
