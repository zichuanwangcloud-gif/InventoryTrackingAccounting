<template>
  <div class="dashboard">
    <div class="header">
      <h1>仪表盘</h1>
      <div class="stats-cards">
        <div class="stat-card">
          <h3>总库存价值</h3>
          <p class="value">¥{{ totalValue.toFixed(2) }}</p>
        </div>
        <div class="stat-card">
          <h3>本月入库</h3>
          <p class="value">¥{{ monthlyInbound.toFixed(2) }}</p>
        </div>
        <div class="stat-card">
          <h3>本月出库</h3>
          <p class="value">¥{{ monthlyOutbound.toFixed(2) }}</p>
        </div>
        <div class="stat-card">
          <h3>净额</h3>
          <p class="value" :class="{ positive: netAmount >= 0, negative: netAmount < 0 }">
            ¥{{ netAmount.toFixed(2) }}
          </p>
        </div>
      </div>
    </div>

    <div class="content">
      <div class="chart-section">
        <h2>趋势分析</h2>
        <div class="chart-placeholder">
          <p>图表组件待实现</p>
        </div>
      </div>

      <div class="recent-section">
        <h2>最近交易</h2>
        <div class="transaction-list">
          <div v-for="transaction in recentTransactions" :key="transaction.id" class="transaction-item">
            <div class="transaction-info">
              <span class="type">{{ getTransactionTypeText(transaction.type) }}</span>
              <span class="item">{{ transaction.itemName }}</span>
            </div>
            <div class="transaction-amount">
              <span :class="{ positive: transaction.type === 'IN', negative: transaction.type === 'OUT' }">
                {{ transaction.type === 'IN' ? '+' : '-' }}¥{{ transaction.amount.toFixed(2) }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

const totalValue = ref(0)
const monthlyInbound = ref(0)
const monthlyOutbound = ref(0)
const netAmount = ref(0)
const recentTransactions = ref([])

const getTransactionTypeText = (type: string) => {
  const typeMap = {
    'IN': '入库',
    'OUT': '出库',
    'ADJUST': '调整'
  }
  return typeMap[type] || type
}

onMounted(() => {
  // TODO: 调用API获取数据
  totalValue.value = 5000.00
  monthlyInbound.value = 1200.00
  monthlyOutbound.value = 800.00
  netAmount.value = monthlyInbound.value - monthlyOutbound.value
  
  recentTransactions.value = [
    { id: 1, type: 'IN', itemName: 'Nike运动鞋', amount: 299.00 },
    { id: 2, type: 'OUT', itemName: '优衣库T恤', amount: 89.00 },
    { id: 3, type: 'IN', itemName: 'Zara外套', amount: 399.00 }
  ]
})
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.header h1 {
  margin-bottom: 20px;
  color: #333;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  text-align: center;
}

.stat-card h3 {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 14px;
}

.stat-card .value {
  font-size: 24px;
  font-weight: bold;
  margin: 0;
}

.positive {
  color: #10b981;
}

.negative {
  color: #ef4444;
}

.content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 30px;
}

.chart-section, .recent-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.chart-section h2, .recent-section h2 {
  margin: 0 0 20px 0;
  color: #333;
}

.chart-placeholder {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 4px;
  color: #666;
}

.transaction-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.transaction-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 4px;
}

.transaction-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.transaction-info .type {
  font-size: 12px;
  color: #666;
}

.transaction-info .item {
  font-weight: 500;
}

.transaction-amount {
  font-weight: bold;
}

@media (max-width: 768px) {
  .content {
    grid-template-columns: 1fr;
  }
  
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
