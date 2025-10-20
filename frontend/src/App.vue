<template>
  <div id="app">
    <nav v-if="showNav" class="navbar">
      <div class="nav-brand">
        <router-link to="/">物品记账</router-link>
      </div>
      <div class="nav-menu">
        <router-link to="/" class="nav-link">仪表盘</router-link>
        <router-link to="/items" class="nav-link">物品管理</router-link>
        <router-link to="/transactions" class="nav-link">交易记录</router-link>
        <router-link to="/reports" class="nav-link">报表</router-link>
        <router-link to="/accounts" class="nav-link">账户</router-link>
        <router-link to="/settings" class="nav-link">设置</router-link>
        <button @click="handleLogout" class="nav-link logout-btn">退出</button>
      </div>
    </nav>
    
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const showNav = computed(() => {
  return !['/login', '/register'].includes(route.path)
})

const handleLogout = () => {
  authStore.logout()
  router.push('/login')
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  height: 100%;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.navbar {
  background: #2c3e50;
  color: white;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 60px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.nav-brand a {
  color: white;
  text-decoration: none;
  font-size: 20px;
  font-weight: bold;
}

.nav-menu {
  display: flex;
  gap: 20px;
  align-items: center;
}

.nav-link {
  color: white;
  text-decoration: none;
  padding: 8px 12px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.nav-link:hover {
  background: rgba(255,255,255,0.1);
}

.nav-link.router-link-active {
  background: rgba(255,255,255,0.2);
}

.logout-btn {
  background: #e74c3c;
  border: none;
  cursor: pointer;
}

.logout-btn:hover {
  background: #c0392b;
}

.main-content {
  min-height: calc(100vh - 60px);
  background: #f5f5f5;
}

@media (max-width: 768px) {
  .navbar {
    flex-direction: column;
    height: auto;
    padding: 10px;
  }
  
  .nav-menu {
    flex-wrap: wrap;
    gap: 10px;
    margin-top: 10px;
  }
  
  .main-content {
    min-height: calc(100vh - 120px);
  }
}
</style>