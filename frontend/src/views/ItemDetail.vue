<template>
  <div class="item-detail">
    <div v-if="loading" class="loading">
      <p>加载中...</p>
    </div>
    
    <div v-else-if="item" class="item-content">
      <div class="item-header">
        <h1>{{ item.name }}</h1>
        <div class="item-actions">
          <router-link :to="`/items/${item.id}/edit`" class="btn-primary">编辑</router-link>
          <button @click="handleDelete" class="btn-danger">删除</button>
        </div>
      </div>

      <div class="item-info">
        <div class="info-section">
          <h2>基本信息</h2>
          <div class="info-grid">
            <div class="info-item">
              <label>品牌</label>
              <span>{{ item.brand || '未设置' }}</span>
            </div>
            <div class="info-item">
              <label>尺码</label>
              <span>{{ item.size || '未设置' }}</span>
            </div>
            <div class="info-item">
              <label>颜色</label>
              <span>{{ item.color || '未设置' }}</span>
            </div>
            <div class="info-item">
              <label>购买价格</label>
              <span class="price">¥{{ item.purchasePrice.toFixed(2) }}</span>
            </div>
            <div class="info-item">
              <label>购买日期</label>
              <span>{{ formatDate(item.purchaseDate) }}</span>
            </div>
            <div class="info-item">
              <label>存放位置</label>
              <span>{{ item.location || '未设置' }}</span>
            </div>
            <div class="info-item">
              <label>状态</label>
              <span :class="item.status.toLowerCase()">{{ getStatusText(item.status) }}</span>
            </div>
          </div>
        </div>

        <div class="info-section">
          <h2>图片</h2>
          <div v-if="item.images && item.images.length > 0" class="image-gallery">
            <div v-for="(image, index) in item.images" :key="index" class="image-item">
              <img :src="image" :alt="`图片${index + 1}`" @click="showImageModal(image)" />
            </div>
          </div>
          <div v-else class="no-images">
            <p>暂无图片</p>
          </div>
        </div>

        <div class="info-section">
          <h2>交易记录</h2>
          <div v-if="transactions.length > 0" class="transactions-list">
            <div v-for="transaction in transactions" :key="transaction.id" class="transaction-item">
              <div class="transaction-info">
                <span class="type" :class="transaction.type.toLowerCase()">
                  {{ getTransactionTypeText(transaction.type) }}
                </span>
                <span class="date">{{ formatDate(transaction.transactionDate) }}</span>
                <span v-if="transaction.notes" class="notes">{{ transaction.notes }}</span>
              </div>
              <div class="transaction-amount">
                <span :class="{ positive: transaction.type === 'IN', negative: transaction.type === 'OUT' }">
                  {{ transaction.type === 'IN' ? '+' : '-' }}¥{{ transaction.totalAmount.toFixed(2) }}
                </span>
              </div>
            </div>
          </div>
          <div v-else class="no-transactions">
            <p>暂无交易记录</p>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="error">
      <p>物品不存在</p>
      <router-link to="/items" class="btn-primary">返回列表</router-link>
    </div>

    <!-- 图片预览模态框 -->
    <div v-if="showModal" class="modal-overlay" @click="showModal = false">
      <div class="modal-content">
        <img :src="modalImage" :alt="'图片预览'" />
        <button @click="showModal = false" class="close-btn">×</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const item = ref(null)
const transactions = ref([])
const loading = ref(true)
const showModal = ref(false)
const modalImage = ref('')

const getStatusText = (status: string) => {
  return status === 'ACTIVE' ? '在库' : '已出库'
}

const getTransactionTypeText = (type: string) => {
  const typeMap = {
    'IN': '入库',
    'OUT': '出库',
    'ADJUST': '调整'
  }
  return typeMap[type] || type
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('zh-CN')
}

const showImageModal = (image: string) => {
  modalImage.value = image
  showModal.value = true
}

const handleDelete = () => {
  if (confirm('确定要删除这个物品吗？')) {
    // TODO: 调用删除API
    console.log('删除物品:', item.value.id)
    router.push('/items')
  }
}

const loadItem = () => {
  // TODO: 根据ID加载物品数据
  const itemId = route.params.id
  console.log('加载物品:', itemId)
  
  // 模拟数据
  setTimeout(() => {
    item.value = {
      id: itemId,
      name: 'Nike Air Max 270',
      brand: 'Nike',
      size: '42',
      color: '白色',
      purchasePrice: 899.00,
      purchaseDate: '2025-10-15',
      location: '鞋柜',
      status: 'ACTIVE',
      images: []
    }
    
    transactions.value = [
      {
        id: '1',
        type: 'IN',
        totalAmount: 899.00,
        transactionDate: '2025-10-15',
        notes: '购买入库'
      }
    ]
    
    loading.value = false
  }, 1000)
}

onMounted(() => {
  loadItem()
})
</script>

<style scoped>
.item-detail {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.loading, .error {
  text-align: center;
  padding: 40px;
}

.item-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.item-header h1 {
  margin: 0;
  color: #333;
}

.item-actions {
  display: flex;
  gap: 10px;
}

.btn-primary {
  background: #667eea;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  text-decoration: none;
  cursor: pointer;
}

.btn-danger {
  background: #e74c3c;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.item-info {
  padding: 20px;
}

.info-section {
  margin-bottom: 30px;
}

.info-section h2 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 18px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.info-item label {
  color: #666;
  font-size: 14px;
  font-weight: 500;
}

.info-item span {
  color: #333;
  font-size: 16px;
}

.info-item .price {
  color: #e74c3c;
  font-weight: bold;
}

.info-item .active {
  color: #10b981;
  font-weight: bold;
}

.info-item .removed {
  color: #ef4444;
  font-weight: bold;
}

.image-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 15px;
}

.image-item {
  aspect-ratio: 1;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.image-item:hover img {
  transform: scale(1.05);
}

.no-images, .no-transactions {
  text-align: center;
  padding: 20px;
  color: #666;
}

.transactions-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.transaction-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 4px;
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

.date {
  color: #666;
  font-size: 14px;
}

.notes {
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

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  position: relative;
  max-width: 90vw;
  max-height: 90vh;
}

.modal-content img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
}

.close-btn {
  position: absolute;
  top: -40px;
  right: 0;
  background: white;
  color: #333;
  border: none;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  font-size: 20px;
  cursor: pointer;
}

@media (max-width: 768px) {
  .item-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .transaction-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
