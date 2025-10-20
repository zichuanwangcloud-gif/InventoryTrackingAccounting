<template>
  <div class="home">
    <h1>欢迎使用物品记账系统</h1>
    <p>请先登录或注册账户</p>
    <div class="actions">
      <router-link to="/login" class="btn">登录</router-link>
      <router-link to="/register" class="btn">注册</router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  text-align: center;
}

.home h1 {
  margin-bottom: 20px;
  color: #333;
}

.home p {
  margin-bottom: 30px;
  color: #666;
  font-size: 18px;
}

.actions {
  display: flex;
  gap: 20px;
}

.btn {
  padding: 12px 24px;
  background: #667eea;
  color: white;
  text-decoration: none;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.btn:hover {
  background: #5a6fd8;
}
</style>