import { test, expect } from '@playwright/test'

test.describe('认证流程', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/')
  })

  test('应该能够访问登录页面', async ({ page }) => {
    await page.goto('/login')
    await expect(page).toHaveTitle(/物品记账/)
    await expect(page.locator('h1')).toContainText('登录')
  })

  test('应该能够访问注册页面', async ({ page }) => {
    await page.goto('/register')
    await expect(page).toHaveTitle(/物品记账/)
    await expect(page.locator('h1')).toContainText('注册')
  })

  test('登录表单应该正常工作', async ({ page }) => {
    await page.goto('/login')
    
    // 填写登录表单
    await page.fill('input[id="username"]', 'testuser')
    await page.fill('input[id="password"]', 'password123')
    
    // 验证表单值
    await expect(page.locator('input[id="username"]')).toHaveValue('testuser')
    await expect(page.locator('input[id="password"]')).toHaveValue('password123')
    
    // 点击登录按钮
    await page.click('button[type="submit"]')
    
    // 验证跳转到首页
    await expect(page).toHaveURL('/')
  })

  test('注册表单应该正常工作', async ({ page }) => {
    await page.goto('/register')
    
    // 填写注册表单
    await page.fill('input[id="username"]', 'newuser')
    await page.fill('input[id="email"]', 'newuser@example.com')
    await page.fill('input[id="password"]', 'password123')
    await page.fill('input[id="confirmPassword"]', 'password123')
    
    // 验证表单值
    await expect(page.locator('input[id="username"]')).toHaveValue('newuser')
    await expect(page.locator('input[id="email"]')).toHaveValue('newuser@example.com')
    await expect(page.locator('input[id="password"]')).toHaveValue('password123')
    await expect(page.locator('input[id="confirmPassword"]')).toHaveValue('password123')
    
    // 点击注册按钮
    await page.click('button[type="submit"]')
  })

  test('应该能够从登录页面导航到注册页面', async ({ page }) => {
    await page.goto('/login')
    
    // 点击注册链接
    await page.click('text=立即注册')
    
    // 验证跳转到注册页面
    await expect(page).toHaveURL('/register')
  })

  test('应该能够从注册页面导航到登录页面', async ({ page }) => {
    await page.goto('/register')
    
    // 点击登录链接
    await page.click('text=已有账户')
    
    // 验证跳转到登录页面
    await expect(page).toHaveURL('/login')
  })
})

test.describe('导航和布局', () => {
  test('首页应该显示导航栏', async ({ page }) => {
    await page.goto('/')
    
    // 验证导航栏存在
    await expect(page.locator('.navbar')).toBeVisible()
    await expect(page.locator('.nav-brand')).toBeVisible()
    await expect(page.locator('.nav-menu')).toBeVisible()
  })

  test('导航链接应该正常工作', async ({ page }) => {
    await page.goto('/')
    
    // 测试各个导航链接
    const navLinks = [
      { text: '仪表盘', href: '/' },
      { text: '物品管理', href: '/items' },
      { text: '交易记录', href: '/transactions' },
      { text: '报表', href: '/reports' },
      { text: '账户', href: '/accounts' },
      { text: '设置', href: '/settings' }
    ]
    
    for (const link of navLinks) {
      await page.click(`text=${link.text}`)
      await expect(page).toHaveURL(link.href)
    }
  })

  test('响应式设计应该正常工作', async ({ page }) => {
    // 测试桌面视图
    await page.setViewportSize({ width: 1200, height: 800 })
    await page.goto('/')
    await expect(page.locator('.navbar')).toBeVisible()
    
    // 测试移动视图
    await page.setViewportSize({ width: 375, height: 667 })
    await page.goto('/')
    await expect(page.locator('.navbar')).toBeVisible()
  })
})
