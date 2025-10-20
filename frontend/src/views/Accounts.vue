<template>
  <div class="accounts-page">
    <div class="header">
      <h1>账户管理</h1>
      <button class="btn-primary" @click="showCreateForm = true">添加账户</button>
    </div>

    <div class="accounts-list">
      <div v-for="account in accounts" :key="account.id" class="account-card">
        <div class="account-info">
          <h3>{{ account.name }}</h3>
          <p class="type">{{ getAccountTypeText(account.type) }}</p>
          <p class="balance">余额: ¥{{ account.balance?.toFixed(2) || '0.00' }}</p>
        </div>
        <div class="account-actions">
          <button class="btn-small">编辑</button>
          <button class="btn-small danger">删除</button>
        </div>
      </div>
    </div>

    <div v-if="accounts.length === 0" class="empty-state">
      <p>暂无账户，<button @click="showCreateForm = true" class="link-btn">立即添加</button></p>
    </div>

    <!-- 创建账户表单 -->
    <div v-if="showCreateForm" class="modal-overlay" @click="showCreateForm = false">
      <div class="modal" @click.stop>
        <h2>添加账户</h2>
        <form @submit.prevent="handleCreateAccount">
          <div class="form-group">
            <label for="name">账户名称</label>
            <input
              id="name"
              v-model="newAccount.name"
              type="text"
              required
              placeholder="如：现金、银行卡"
            />
          </div>
          <div class="form-group">
            <label for="type">账户类型</label>
            <select id="type" v-model="newAccount.type" required>
              <option value="">请选择类型</option>
              <option value="CASH">现金</option>
              <option value="BANK">银行卡</option>
              <option value="PLATFORM">平台账户</option>
              <option value="OTHER">其他</option>
            </select>
          </div>
          <div class="form-actions">
            <button type="button" @click="showCreateForm = false" class="btn-secondary">取消</button>
            <button type="submit" :disabled="loading" class="btn-primary">
              {{ loading ? '创建中...' : '创建' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'

interface Account {
  id: string
  name: string
  type: string
  balance?: number
}

const accounts = ref<Account[]>([])
const showCreateForm = ref(false)
const loading = ref(false)

const newAccount = ref({
  name: '',
  type: ''
})

const getAccountTypeText = (type: string) => {
  const typeMap = {
    'CASH': '现金',
    'BANK': '银行卡',
    'PLATFORM': '平台账户',
    'OTHER': '其他'
  }
  return typeMap[type] || type
}

const handleCreateAccount = async () => {
  loading.value = true
  
  try {
    // TODO: 调用API创建账户
    console.log('创建账户:', newAccount.value)
    
    // 模拟创建成功
    const account: Account = {
      id: Date.now().toString(),
      name: newAccount.value.name,
      type: newAccount.value.type,
      balance: 0
    }
    
    accounts.value.push(account)
    showCreateForm.value = false
    newAccount.value = { name: '', type: '' }
  } catch (error) {
    console.error('创建账户失败:', error)
    alert('创建账户失败，请重试')
  } finally {
    loading.value = false
  }
}

const loadAccounts = () => {
  // TODO: 调用API获取账户列表
  accounts.value = [
    {
      id: '1',
      name: '现金',
      type: 'CASH',
      balance: 500.00
    },
    {
      id: '2',
      name: '招商银行卡',
      type: 'BANK',
      balance: 2000.00
    }
  ]
}

onMounted(() => {
  loadAccounts()
})
</script>

<style scoped>
.accounts-page {
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

.btn-primary {
  background: #667eea;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.accounts-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.account-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.account-info h3 {
  margin: 0 0 8px 0;
  color: #333;
}

.account-info .type {
  margin: 0 0 8px 0;
  color: #666;
  font-size: 14px;
}

.account-info .balance {
  margin: 0;
  color: #10b981;
  font-weight: bold;
}

.account-actions {
  display: flex;
  gap: 8px;
}

.btn-small {
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
  border: 1px solid #ddd;
  background: white;
  color: #333;
  cursor: pointer;
}

.btn-small.danger {
  color: #e74c3c;
  border-color: #e74c3c;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #666;
}

.link-btn {
  background: none;
  border: none;
  color: #667eea;
  cursor: pointer;
  text-decoration: underline;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  padding: 30px;
  border-radius: 8px;
  width: 100%;
  max-width: 400px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.modal h2 {
  margin: 0 0 20px 0;
  color: #333;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #555;
  font-weight: 500;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 15px;
  margin-top: 20px;
}

.btn-secondary {
  background: white;
  color: #333;
  padding: 10px 20px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
}

@media (max-width: 768px) {
  .accounts-list {
    grid-template-columns: 1fr;
  }
  
  .account-card {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
}
</style>
