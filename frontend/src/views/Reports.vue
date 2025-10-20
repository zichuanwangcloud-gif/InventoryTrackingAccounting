<template>
  <div class="reports-page">
    <h1>报表分析</h1>
    
    <div class="reports-grid">
      <div class="report-card">
        <h2>库存价值</h2>
        <div class="chart-placeholder">
          <p>库存价值图表</p>
          <p class="value">¥{{ totalValue.toFixed(2) }}</p>
        </div>
      </div>
      
      <div class="report-card">
        <h2>处置盈亏</h2>
        <div class="chart-placeholder">
          <p>处置盈亏图表</p>
          <p class="value">¥{{ disposalProfit.toFixed(2) }}</p>
        </div>
      </div>
      
      <div class="report-card">
        <h2>趋势分析</h2>
        <div class="chart-placeholder">
          <p>趋势分析图表</p>
          <div class="trend-stats">
            <div class="stat">
              <span class="label">入库:</span>
              <span class="value positive">¥{{ inboundAmount.toFixed(2) }}</span>
            </div>
            <div class="stat">
              <span class="label">出库:</span>
              <span class="value negative">¥{{ outboundAmount.toFixed(2) }}</span>
            </div>
            <div class="stat">
              <span class="label">净额:</span>
              <span class="value" :class="{ positive: netAmount >= 0, negative: netAmount < 0 }">
                ¥{{ netAmount.toFixed(2) }}
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
const disposalProfit = ref(0)
const inboundAmount = ref(0)
const outboundAmount = ref(0)
const netAmount = ref(0)

const loadReports = () => {
  // TODO: 调用API获取报表数据
  totalValue.value = 5000.00
  disposalProfit.value = 200.00
  inboundAmount.value = 1200.00
  outboundAmount.value = 800.00
  netAmount.value = inboundAmount.value - outboundAmount.value
}

onMounted(() => {
  loadReports()
})
</script>

<style scoped>
.reports-page {
  padding: 20px;
}

.reports-page h1 {
  margin-bottom: 30px;
  color: #333;
}

.reports-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.report-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.report-card h2 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 18px;
}

.chart-placeholder {
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 4px;
  color: #666;
}

.chart-placeholder .value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-top: 10px;
}

.trend-stats {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 15px;
}

.stat {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat .label {
  color: #666;
}

.stat .value {
  font-weight: bold;
}

.positive {
  color: #10b981;
}

.negative {
  color: #ef4444;
}

@media (max-width: 768px) {
  .reports-grid {
    grid-template-columns: 1fr;
  }
}
</style>
