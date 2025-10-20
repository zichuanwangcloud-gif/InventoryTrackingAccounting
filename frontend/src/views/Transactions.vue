<template>
  <div class="transactions-page">
    <div class="header">
      <h1>交易记录</h1>
      <div class="actions">
        <button class="btn-primary">入库</button>
        <button class="btn-primary">出库</button>
      </div>
    </div>

    <div class="filters">
      <div class="filter-group">
        <select v-model="selectedType" @change="handleFilter">
          <option value="">所有类型</option>
          <option value="IN">入库</option>
          <option value="OUT">出库</option>
          <option value="ADJUST">调整</option>
        </select>
        <input
          v-model="startDate"
          type="date"
          placeholder="开始日期"
          @change="handleFilter"
        />
        <input
          v-model="endDate"
          type="date"
          placeholder="结束日期"
          @change="handleFilter"
        />
      </div>
    </div>

    <div class="transactions-list">
      <div v-for="transaction in transactions" :key="transaction.id" class="transaction-item">
        <div class="transaction-info">
          <span class="type" :class="transaction.type.toLowerCase()">
            {{ getTransactionTypeText(transaction.type) }}
          </span>
          <span class="item">{{ transaction.itemName }}</span>
          <span class="date">{{ formatDate(transaction.transactionDate) }}</span>
        </div>
        <div class="transaction-amount">
          <span :class="{ positive: transaction.type === 'IN', negative: transaction.type === 'OUT' }">
            {{ transaction.type === 'IN' ? '+' : '-' }}¥{{ transaction.totalAmount.toFixed(2) }}
          </span>
        </div>
      </div>
    </div>

    <div v-if="transactions.length === 0" class="empty-state">
      <p>暂无交易记录</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface Transaction {
  id: string
  type: 'IN' | 'OUT' | 'ADJUST'
  itemName: string
  totalAmount: number
  transactionDate: string
}

const transactions = ref<Transaction[]>([])
const selectedType = ref('')
const startDate = ref('')
const endDate = ref('')

const getTransactionTypeText = (type: string) => {
  const typeMap = {
    'IN': '入库',
    'OUT': '出库',
    'ADJUST': '调整'
  }
  return typeMap[type] || type
}

const handleFilter = () => {
  // TODO: 实现筛选逻辑
  console.log('筛选:', selectedType.value, startDate.value, endDate.value)
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('zh-CN')
}

const loadTransactions = () => {
  // TODO: 调用API获取交易记录
  transactions.value = [
    {
      id: '1',
      type: 'IN',
      itemName: 'Nike运动鞋',
      totalAmount: 899.00,
      transactionDate: '2025-10-15'
    },
    {
      id: '2',
      type: 'OUT',
      itemName: '优衣库T恤',
      totalAmount: 59.00,
      transactionDate: '2025-10-10'
    }
  ]
}

onMounted(() => {
  loadTransactions()
})
</script>

<style scoped>
.transactions-page {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.header h1 {
  margin: 0;
  color: #333;
}

.actions {
  display: flex;
  gap: 10px;
}

.btn-primary {
  background: #667eea;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.filters {
  margin-bottom: 30px;
}

.filter-group {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.filter-group select,
.filter-group input {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.transactions-list {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
}

.transaction-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.transaction-item:last-child {
  border-bottom: none;
}

.transaction-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.type {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
  text-transform: uppercase;
}

.type.in {
  background: #d4edda;
  color: #155724;
}

.type.out {
  background: #f8d7da;
  color: #721c24;
}

.type.adjust {
  background: #d1ecf1;
  color: #0c5460;
}

.item {
  font-weight: 500;
  color: #333;
}

.date {
  color: #666;
  font-size: 14px;
}

.transaction-amount {
  font-weight: bold;
  font-size: 16px;
}

.positive {
  color: #10b981;
}

.negative {
  color: #ef4444;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #666;
}

@media (max-width: 768px) {
  .transaction-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  
  .transaction-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  
  .filter-group {
    flex-direction: column;
  }
}
</style>
