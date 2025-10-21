# 网页访问指南

## 🚀 系统已启动！

### 访问地址

#### 前端应用（用户界面）
- **URL**: http://localhost:5173
- **状态**: ✅ 已启动
- **框架**: Vue 3 + Vite
- **功能**: 库存跟踪和会计管理界面

#### 后端API服务
- **URL**: http://localhost:8080
- **状态**: 🔄 启动中
- **框架**: Spring Boot 3.3.4
- **功能**: RESTful API服务

### 系统功能

#### 📦 库存管理
- 物品录入和管理
- 分类管理
- 库存查询
- 图片上传

#### 💰 会计功能
- 交易记录
- 账户管理
- 财务报表
- 成本分析

#### 👤 用户管理
- 用户注册/登录
- 权限控制
- 个人设置

### 数据库信息
- **类型**: PostgreSQL 14
- **数据库**: inventory
- **状态**: ✅ 已连接
- **数据**: 包含默认品类数据

## 🔧 技术栈

### 后端
- Java 17
- Spring Boot 3.3.4
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT认证

### 前端
- Vue 3
- TypeScript
- Pinia (状态管理)
- Vue Router
- Vite (构建工具)

### 测试
- JUnit 5 (后端单元测试)
- Vitest (前端单元测试)
- Playwright (E2E测试)

## 📱 使用说明

1. **打开浏览器** 访问 http://localhost:5173
2. **注册账户** 或使用测试账户登录
3. **开始使用** 库存跟踪和会计功能

## 🛠️ 开发模式

### 前端开发
```bash
cd frontend
npm run dev
```

### 后端开发
```bash
cd backend
./gradlew bootRun
```

### 数据库管理
```bash
# 连接数据库
psql -h localhost -U postgres -d inventory
```

## 📊 系统状态

- ✅ **数据库**: PostgreSQL 运行正常
- ✅ **前端**: Vue开发服务器运行正常
- 🔄 **后端**: Spring Boot服务启动中
- ✅ **测试**: 集成测试通过

## 🎯 下一步

1. 等待后端服务完全启动
2. 访问前端界面开始使用
3. 测试各项功能
4. 根据需要调整配置

---

**注意**: 如果后端服务启动失败，请检查：
- Java 17是否正确安装
- PostgreSQL服务是否运行
- 端口8080是否被占用
