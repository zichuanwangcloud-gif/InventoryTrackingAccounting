import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useAuthStore } from './auth'

interface InventoryValueReport {
  totalValue: number
  categoryValues: Record<string, number>
}

interface DisposalProfitReport {
  disposalAmounts: Record<string, number>
}

interface TrendsReport {
  inboundAmount: number
  outboundAmount: number
  netAmount: number
}

export const useReportsStore = defineStore('reports', () => {
  const authStore = useAuthStore()
  const loading = ref(false)

  const getInventoryValueReport = async (groupBy?: string) => {
    loading.value = true
    try {
      const queryParams = new URLSearchParams()
      if (groupBy) queryParams.append('groupBy', groupBy)

      const response = await fetch(`/api/v1/reports/inventory-value?${queryParams}`, {
        headers: authStore.getAuthHeaders(),
      })

      if (!response.ok) {
        throw new Error('获取库存价值报表失败')
      }

      const data = await response.json()
      
      if (data.code === 200) {
        return data.data as InventoryValueReport
      } else {
        throw new Error(data.message || '获取库存价值报表失败')
      }
    } catch (error) {
      console.error('获取库存价值报表错误:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const getDisposalProfitReport = async (startDate?: string, endDate?: string) => {
    loading.value = true
    try {
      const queryParams = new URLSearchParams()
      if (startDate) queryParams.append('startDate', startDate)
      if (endDate) queryParams.append('endDate', endDate)

      const response = await fetch(`/api/v1/reports/disposal-profit?${queryParams}`, {
        headers: authStore.getAuthHeaders(),
      })

      if (!response.ok) {
        throw new Error('获取处置盈亏报表失败')
      }

      const data = await response.json()
      
      if (data.code === 200) {
        return data.data as DisposalProfitReport
      } else {
        throw new Error(data.message || '获取处置盈亏报表失败')
      }
    } catch (error) {
      console.error('获取处置盈亏报表错误:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const getTrendsReport = async (period?: string, startDate?: string, endDate?: string) => {
    loading.value = true
    try {
      const queryParams = new URLSearchParams()
      if (period) queryParams.append('period', period)
      if (startDate) queryParams.append('startDate', startDate)
      if (endDate) queryParams.append('endDate', endDate)

      const response = await fetch(`/api/v1/reports/trends?${queryParams}`, {
        headers: authStore.getAuthHeaders(),
      })

      if (!response.ok) {
        throw new Error('获取趋势报表失败')
      }

      const data = await response.json()
      
      if (data.code === 200) {
        return data.data as TrendsReport
      } else {
        throw new Error(data.message || '获取趋势报表失败')
      }
    } catch (error) {
      console.error('获取趋势报表错误:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const getTransactionStats = async (startDate?: string, endDate?: string) => {
    loading.value = true
    try {
      const queryParams = new URLSearchParams()
      if (startDate) queryParams.append('startDate', startDate)
      if (endDate) queryParams.append('endDate', endDate)

      const response = await fetch(`/api/v1/transactions/stats?${queryParams}`, {
        headers: authStore.getAuthHeaders(),
      })

      if (!response.ok) {
        throw new Error('获取交易统计失败')
      }

      const data = await response.json()
      
      if (data.code === 200) {
        return data.data
      } else {
        throw new Error(data.message || '获取交易统计失败')
      }
    } catch (error) {
      console.error('获取交易统计错误:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    getInventoryValueReport,
    getDisposalProfitReport,
    getTrendsReport,
    getTransactionStats
  }
})
