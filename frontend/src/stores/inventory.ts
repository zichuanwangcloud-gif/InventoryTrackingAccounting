import { defineStore } from 'pinia'
import { ref } from 'vue'
import { useAuthStore } from './auth'

interface Item {
  id: string
  name: string
  brand?: string
  size?: string
  color?: string
  purchasePrice: number
  purchaseDate: string
  location?: string
  images: string[]
  status: 'ACTIVE' | 'REMOVED'
  categoryId?: string
}

interface Transaction {
  id: string
  type: 'IN' | 'OUT' | 'ADJUST'
  quantity: number
  unitPrice: number
  totalAmount: number
  transactionDate: string
  reason?: string
  notes?: string
  itemId: string
  itemName: string
}

export const useInventoryStore = defineStore('inventory', () => {
  const authStore = useAuthStore()
  const items = ref<Item[]>([])
  const transactions = ref<Transaction[]>([])
  const loading = ref(false)

  const fetchItems = async (params: {
    page?: number
    size?: number
    search?: string
    categoryId?: string
    status?: string
  } = {}) => {
    loading.value = true
    try {
      const queryParams = new URLSearchParams()
      if (params.page !== undefined) queryParams.append('page', params.page.toString())
      if (params.size !== undefined) queryParams.append('size', params.size.toString())
      if (params.search) queryParams.append('search', params.search)
      if (params.categoryId) queryParams.append('categoryId', params.categoryId)
      if (params.status) queryParams.append('status', params.status)

      const response = await fetch(`/api/v1/items?${queryParams}`, {
        headers: authStore.getAuthHeaders(),
      })

      if (!response.ok) {
        throw new Error('获取物品列表失败')
      }

      const data = await response.json()
      
      if (data.code === 200) {
        items.value = data.data.content || []
        return data.data
      } else {
        throw new Error(data.message || '获取物品列表失败')
      }
    } catch (error) {
      console.error('获取物品列表错误:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const createItem = async (itemData: Partial<Item>) => {
    loading.value = true
    try {
      const response = await fetch('/api/v1/items', {
        method: 'POST',
        headers: authStore.getAuthHeaders(),
        body: JSON.stringify(itemData),
      })

      if (!response.ok) {
        throw new Error('创建物品失败')
      }

      const data = await response.json()
      
      if (data.code === 200) {
        items.value.unshift(data.data)
        return { success: true, data: data.data }
      } else {
        throw new Error(data.message || '创建物品失败')
      }
    } catch (error) {
      console.error('创建物品错误:', error)
      return { success: false, error: error.message }
    } finally {
      loading.value = false
    }
  }

  const updateItem = async (id: string, itemData: Partial<Item>) => {
    loading.value = true
    try {
      const response = await fetch(`/api/v1/items/${id}`, {
        method: 'PUT',
        headers: authStore.getAuthHeaders(),
        body: JSON.stringify(itemData),
      })

      if (!response.ok) {
        throw new Error('更新物品失败')
      }

      const data = await response.json()
      
      if (data.code === 200) {
        const index = items.value.findIndex(item => item.id === id)
        if (index !== -1) {
          items.value[index] = data.data
        }
        return { success: true, data: data.data }
      } else {
        throw new Error(data.message || '更新物品失败')
      }
    } catch (error) {
      console.error('更新物品错误:', error)
      return { success: false, error: error.message }
    } finally {
      loading.value = false
    }
  }

  const deleteItem = async (id: string) => {
    loading.value = true
    try {
      const response = await fetch(`/api/v1/items/${id}`, {
        method: 'DELETE',
        headers: authStore.getAuthHeaders(),
      })

      if (!response.ok) {
        throw new Error('删除物品失败')
      }

      const data = await response.json()
      
      if (data.code === 200) {
        const index = items.value.findIndex(item => item.id === id)
        if (index !== -1) {
          items.value.splice(index, 1)
        }
        return { success: true }
      } else {
        throw new Error(data.message || '删除物品失败')
      }
    } catch (error) {
      console.error('删除物品错误:', error)
      return { success: false, error: error.message }
    } finally {
      loading.value = false
    }
  }

  const fetchTransactions = async (params: {
    page?: number
    size?: number
    type?: string
    itemId?: string
    startDate?: string
    endDate?: string
  } = {}) => {
    loading.value = true
    try {
      const queryParams = new URLSearchParams()
      if (params.page !== undefined) queryParams.append('page', params.page.toString())
      if (params.size !== undefined) queryParams.append('size', params.size.toString())
      if (params.type) queryParams.append('type', params.type)
      if (params.itemId) queryParams.append('itemId', params.itemId)
      if (params.startDate) queryParams.append('startDate', params.startDate)
      if (params.endDate) queryParams.append('endDate', params.endDate)

      const response = await fetch(`/api/v1/transactions?${queryParams}`, {
        headers: authStore.getAuthHeaders(),
      })

      if (!response.ok) {
        throw new Error('获取交易列表失败')
      }

      const data = await response.json()
      
      if (data.code === 200) {
        transactions.value = data.data.content || []
        return data.data
      } else {
        throw new Error(data.message || '获取交易列表失败')
      }
    } catch (error) {
      console.error('获取交易列表错误:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const getItemStats = async () => {
    try {
      const response = await fetch('/api/v1/items/stats', {
        headers: authStore.getAuthHeaders(),
      })

      if (!response.ok) {
        throw new Error('获取统计信息失败')
      }

      const data = await response.json()
      
      if (data.code === 200) {
        return data.data
      } else {
        throw new Error(data.message || '获取统计信息失败')
      }
    } catch (error) {
      console.error('获取统计信息错误:', error)
      throw error
    }
  }

  return {
    items,
    transactions,
    loading,
    fetchItems,
    createItem,
    updateItem,
    deleteItem,
    fetchTransactions,
    getItemStats
  }
})
