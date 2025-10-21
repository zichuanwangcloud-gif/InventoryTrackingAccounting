import { describe, it, expect, beforeEach, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/views/Login.vue'

// Mock router
const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: { template: '<div>Home</div>' } },
    { path: '/register', component: { template: '<div>Register</div>' } }
  ]
})

describe('Login.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('应该正确渲染登录表单', () => {
    const wrapper = mount(Login, {
      global: {
        plugins: [router]
      }
    })

    expect(wrapper.find('h1').text()).toBe('登录')
    expect(wrapper.find('input[id="username"]').exists()).toBe(true)
    expect(wrapper.find('input[id="password"]').exists()).toBe(true)
    expect(wrapper.find('button[type="submit"]').exists()).toBe(true)
    expect(wrapper.find('router-link[to="/register"]').exists()).toBe(true)
  })

  it('应该显示正确的表单标签和占位符', () => {
    const wrapper = mount(Login, {
      global: {
        plugins: [router]
      }
    })

    const usernameInput = wrapper.find('input[id="username"]')
    const passwordInput = wrapper.find('input[id="password"]')

    expect(wrapper.find('label[for="username"]').text()).toBe('用户名/邮箱')
    expect(wrapper.find('label[for="password"]').text()).toBe('密码')
    expect(usernameInput.attributes('placeholder')).toBe('请输入用户名或邮箱')
    expect(passwordInput.attributes('placeholder')).toBe('请输入密码')
    expect(usernameInput.attributes('type')).toBe('text')
    expect(passwordInput.attributes('type')).toBe('password')
  })

  it('应该处理表单输入', async () => {
    const wrapper = mount(Login, {
      global: {
        plugins: [router]
      }
    })

    const usernameInput = wrapper.find('input[id="username"]')
    const passwordInput = wrapper.find('input[id="password"]')

    await usernameInput.setValue('testuser')
    await passwordInput.setValue('password123')

    expect(wrapper.vm.form.username).toBe('testuser')
    expect(wrapper.vm.form.password).toBe('password123')
  })

  it('应该在提交时显示加载状态', async () => {
    const wrapper = mount(Login, {
      global: {
        plugins: [router]
      }
    })

    const usernameInput = wrapper.find('input[id="username"]')
    const passwordInput = wrapper.find('input[id="password"]')
    const submitButton = wrapper.find('button[type="submit"]')

    await usernameInput.setValue('testuser')
    await passwordInput.setValue('password123')

    // 模拟异步操作
    const loginPromise = wrapper.vm.handleLogin()
    
    // 检查加载状态
    expect(submitButton.text()).toBe('登录中...')
    expect(submitButton.attributes('disabled')).toBeDefined()

    await loginPromise
  })

  it('应该在登录成功后跳转到首页', async () => {
    const pushSpy = vi.spyOn(router, 'push')
    
    const wrapper = mount(Login, {
      global: {
        plugins: [router]
      }
    })

    const usernameInput = wrapper.find('input[id="username"]')
    const passwordInput = wrapper.find('input[id="password"]')

    await usernameInput.setValue('testuser')
    await passwordInput.setValue('password123')

    await wrapper.vm.handleLogin()

    expect(pushSpy).toHaveBeenCalledWith('/')
    expect(localStorage.getItem('token')).toBe('mock-jwt-token')
    expect(localStorage.getItem('user')).toBeTruthy()
  })

  it('应该显示注册链接', () => {
    const wrapper = mount(Login, {
      global: {
        plugins: [router]
      }
    })

    const registerLink = wrapper.find('router-link[to="/register"]')
    expect(registerLink.exists()).toBe(true)
    expect(registerLink.text()).toBe('立即注册')
  })

  it('应该验证必填字段', () => {
    const wrapper = mount(Login, {
      global: {
        plugins: [router]
      }
    })

    const usernameInput = wrapper.find('input[id="username"]')
    const passwordInput = wrapper.find('input[id="password"]')

    expect(usernameInput.attributes('required')).toBeDefined()
    expect(passwordInput.attributes('required')).toBeDefined()
  })
})
