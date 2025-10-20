<template>
  <div class="items-page">
    <div class="header">
      <h1>物品管理</h1>
      <div class="actions">
        <router-link to="/items/new" class="btn-primary">
          添加物品
        </router-link>
      </div>
    </div>

    <div class="filters">
      <div class="search-box">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="搜索物品名称或品牌..."
          @input="handleSearch"
        />
      </div>
      <div class="filter-group">
        <select v-model="selectedCategory" @change="handleFilter">
          <option value="">所有品类</option>
          <option value="clothing">服装</option>
          <option value="shoes">鞋子</option>
          <option value="accessories">配饰</option>
        </select>
        <select v-model="selectedStatus" @change="handleFilter">
          <option value="">所有状态</option>
          <option value="ACTIVE">在库</option>
          <option value="REMOVED">已出库</option>
        </select>
      </div>
    </div>

    <div class="items-grid">
      <div v-for="item in items" :key="item.id" class="item-card">
        <div class="item-image">
          <img v-if="item.images && item.images.length > 0" :src="item.images[0]" :alt="item.name" />
          <div v-else class="no-image">暂无图片</div>
        </div>
        <div class="item-info">
          <h3>{{ item.name }}</h3>
          <p class="brand">{{ item.brand }}</p>
          <p class="price">¥{{ item.purchasePrice.toFixed(2) }}</p>
          <p class="date">{{ formatDate(item.purchaseDate) }}</p>
          <div class="item-actions">
            <router-link :to="`/items/${item.id}`" class="btn-small">查看</router-link>
            <router-link :to="`/items/${item.id}/edit`" class="btn-small">编辑</router-link>
            <button @click="handleDelete(item.id)" class="btn-small danger">删除</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="items.length === 0" class="empty-state">
      <p>暂无物品，<router-link to="/items/new">立即添加</router-link></p>
    </div>

    <div class="pagination">
      <button @click="prevPage" :disabled="currentPage === 0">上一页</button>
      <span>{{ currentPage + 1 }} / {{ totalPages }}</span>
      <button @click="nextPage" :disabled="currentPage >= totalPages - 1">下一页</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface Item {
  id: string
  name: string
  brand: string
  purchasePrice: number
  purchaseDate: string
  images: string[]
  status: string
}

const items = ref<Item[]>([])
const searchQuery = ref('')
const selectedCategory = ref('')
const selectedStatus = ref('')
const currentPage = ref(0)
const totalPages = ref(1)

const handleSearch = () => {
  // TODO: 实现搜索逻辑
  console.log('搜索:', searchQuery.value)
}

const handleFilter = () => {
  // TODO: 实现筛选逻辑
  console.log('筛选:', selectedCategory.value, selectedStatus.value)
}

const handleDelete = (id: string) => {
  if (confirm('确定要删除这个物品吗？')) {
    // TODO: 调用删除API
    console.log('删除物品:', id)
  }
}

const prevPage = () => {
  if (currentPage.value > 0) {
    currentPage.value--
    loadItems()
  }
}

const nextPage = () => {
  if (currentPage.value < totalPages.value - 1) {
    currentPage.value++
    loadItems()
  }
}

const loadItems = () => {
  // TODO: 调用API获取物品列表
  // 模拟数据
  items.value = [
    {
      id: '1',
      name: 'Nike Air Max 270',
      brand: 'Nike',
      purchasePrice: 899.00,
      purchaseDate: '2025-10-15',
      images: [],
      status: 'ACTIVE'
    },
    {
      id: '2',
      name: '优衣库基础T恤',
      brand: '优衣库',
      purchasePrice: 59.00,
      purchaseDate: '2025-10-10',
      images: [],
      status: 'ACTIVE'
    },
    {
      id: '3',
      name: 'Zara风衣',
      brand: 'Zara',
      purchasePrice: 399.00,
      purchaseDate: '2025-10-05',
      images: [],
      status: 'REMOVED'
    }
  ]
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleDateString('zh-CN')
}

onMounted(() => {
  loadItems()
})
</script>

<style scoped>
.items-page {
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
  border-radius: 4px;
  text-decoration: none;
  border: none;
  cursor: pointer;
}

.filters {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
  flex-wrap: wrap;
}

.search-box {
  flex: 1;
  min-width: 200px;
}

.search-box input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.filter-group {
  display: flex;
  gap: 10px;
}

.filter-group select {
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.item-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
  transition: transform 0.2s;
}

.item-card:hover {
  transform: translateY(-2px);
}

.item-image {
  height: 200px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.no-image {
  color: #999;
  font-size: 14px;
}

.item-info {
  padding: 15px;
}

.item-info h3 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 16px;
}

.item-info .brand {
  margin: 0 0 8px 0;
  color: #666;
  font-size: 14px;
}

.item-info .price {
  margin: 0 0 8px 0;
  color: #e74c3c;
  font-weight: bold;
  font-size: 16px;
}

.item-info .date {
  margin: 0 0 15px 0;
  color: #999;
  font-size: 12px;
}

.item-actions {
  display: flex;
  gap: 8px;
}

.btn-small {
  padding: 6px 12px;
  border-radius: 4px;
  text-decoration: none;
  font-size: 12px;
  border: 1px solid #ddd;
  background: white;
  color: #333;
  cursor: pointer;
}

.btn-small:hover {
  background: #f5f5f5;
}

.btn-small.danger {
  color: #e74c3c;
  border-color: #e74c3c;
}

.btn-small.danger:hover {
  background: #fdf2f2;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #666;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
}

.pagination button {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
}

.pagination button:disabled {
  background: #f5f5f5;
  color: #999;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .items-grid {
    grid-template-columns: 1fr;
  }
  
  .filters {
    flex-direction: column;
  }
  
  .filter-group {
    flex-direction: column;
  }
}
</style>
