<template>
  <div class="login-container">
    <div class="login-form">
      <h1>登录</h1>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="username">用户名/邮箱</label>
          <input
            id="username"
            v-model="form.username"
            type="text"
            required
            placeholder="请输入用户名或邮箱"
          />
        </div>
        
        <div class="form-group">
          <label for="password">密码</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            required
            placeholder="请输入密码"
          />
        </div>
        
        <button type="submit" :disabled="loading" class="login-btn">
          {{ loading ? '登录中...' : '登录' }}
        </button>
        
        <div class="form-footer">
          <p>还没有账户？<router-link to="/register">立即注册</router-link></p>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)

const form = ref({
  username: '',
  password: ''
})

const handleLogin = async () => {
  loading.value = true
  
  try {
    // TODO: 调用登录API
    console.log('登录信息:', form.value)
    
    // 模拟登录成功
    localStorage.setItem('token', 'mock-jwt-token')
    localStorage.setItem('user', JSON.stringify({
      id: '1',
      username: form.value.username,
      email: 'user@example.com'
    }))
    
    router.push('/')
  } catch (error) {
    console.error('登录失败:', error)
    alert('登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-form {
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.login-form h1 {
  text-align: center;
  margin-bottom: 30px;
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

.form-group input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 16px;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.2);
}

.login-btn {
  width: 100%;
  padding: 12px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.login-btn:hover:not(:disabled) {
  background: #5a6fd8;
}

.login-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.form-footer {
  text-align: center;
  margin-top: 20px;
}

.form-footer a {
  color: #667eea;
  text-decoration: none;
}

.form-footer a:hover {
  text-decoration: underline;
}
</style>
