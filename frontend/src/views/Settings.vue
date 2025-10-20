<template>
  <div class="settings-page">
    <h1>设置</h1>
    
    <div class="settings-sections">
      <div class="settings-section">
        <h2>个人信息</h2>
        <div class="form-group">
          <label for="username">用户名</label>
          <input id="username" v-model="userInfo.username" type="text" disabled />
        </div>
        <div class="form-group">
          <label for="email">邮箱</label>
          <input id="email" v-model="userInfo.email" type="email" />
        </div>
        <button class="btn-primary">保存</button>
      </div>

      <div class="settings-section">
        <h2>密码修改</h2>
        <div class="form-group">
          <label for="currentPassword">当前密码</label>
          <input id="currentPassword" v-model="passwordForm.currentPassword" type="password" />
        </div>
        <div class="form-group">
          <label for="newPassword">新密码</label>
          <input id="newPassword" v-model="passwordForm.newPassword" type="password" />
        </div>
        <div class="form-group">
          <label for="confirmPassword">确认新密码</label>
          <input id="confirmPassword" v-model="passwordForm.confirmPassword" type="password" />
        </div>
        <button class="btn-primary">修改密码</button>
      </div>

      <div class="settings-section">
        <h2>数据管理</h2>
        <div class="data-actions">
          <button class="btn-secondary">导出数据</button>
          <button class="btn-secondary">导入数据</button>
          <button class="btn-danger">清空数据</button>
        </div>
      </div>

      <div class="settings-section">
        <h2>关于</h2>
        <div class="about-info">
          <p>版本: 1.0.0</p>
          <p>开发者: Inventory Team</p>
          <p>联系方式: support@inventory-app.com</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const userInfo = ref({
  username: 'test_user',
  email: 'test@example.com'
})

const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})
</script>

<style scoped>
.settings-page {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.settings-page h1 {
  margin-bottom: 30px;
  color: #333;
}

.settings-sections {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.settings-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.settings-section h2 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 18px;
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

.form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

.form-group input:disabled {
  background: #f5f5f5;
  color: #666;
}

.btn-primary {
  background: #667eea;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-secondary {
  background: white;
  color: #333;
  padding: 10px 20px;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 10px;
}

.btn-danger {
  background: #e74c3c;
  color: white;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.data-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.about-info p {
  margin: 10px 0;
  color: #666;
}

@media (max-width: 768px) {
  .data-actions {
    flex-direction: column;
  }
  
  .btn-secondary,
  .btn-danger {
    margin-right: 0;
    margin-bottom: 10px;
  }
}
</style>
