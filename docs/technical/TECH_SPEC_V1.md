# 技术方案文档 V1.0（MVP）

## 1. 技术架构概览

### 1.1 整体架构
- **前端**：Vue 3 + Vite + TypeScript + Pinia + Vue Router
- **后端**：Spring Boot 3 + Java 17 + Spring Data JPA + PostgreSQL
- **构建工具**：Gradle (Kotlin DSL) + Vite
- **部署**：Docker + Docker Compose（可选）

### 1.2 技术栈选型理由
- **Vue 3**：组合式API，TypeScript支持良好，生态成熟
- **Spring Boot 3**：企业级框架，JPA简化数据层，安全框架完善
- **PostgreSQL**：ACID特性，JSON支持，扩展性好
- **JWT**：无状态认证，适合前后端分离

## 2. 数据库设计

### 2.1 核心实体关系
```
User (1) -> (N) Item
User (1) -> (N) Account
User (1) -> (N) InventoryTransaction
User (1) -> (N) LedgerEntry
Item (1) -> (N) InventoryTransaction
Account (1) -> (N) LedgerEntry
Category (1) -> (N) Item
```

### 2.2 表结构设计

#### 2.2.1 用户表 (users)
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 2.2.2 物品表 (items)
```sql
CREATE TABLE items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(200) NOT NULL,
    category_id UUID REFERENCES categories(id),
    brand VARCHAR(100),
    size VARCHAR(50),
    color VARCHAR(50),
    purchase_price DECIMAL(18,2) NOT NULL,
    purchase_date DATE NOT NULL,
    location VARCHAR(200),
    images JSONB, -- 存储图片URL数组
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, REMOVED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP -- 软删除
);
```

#### 2.2.3 品类表 (categories)
```sql
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    parent_id UUID REFERENCES categories(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 2.2.4 库存交易表 (inventory_transactions)
```sql
CREATE TABLE inventory_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    item_id UUID NOT NULL REFERENCES items(id),
    type VARCHAR(20) NOT NULL, -- IN, OUT, ADJUST
    quantity INTEGER DEFAULT 1,
    unit_price DECIMAL(18,2) NOT NULL,
    total_amount DECIMAL(18,2) NOT NULL,
    transaction_date DATE NOT NULL,
    reason VARCHAR(50), -- SELL, DISPOSE, GIFT, LOST
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 2.2.5 账户表 (accounts)
```sql
CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL, -- CASH, BANK, PLATFORM, OTHER
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 2.2.6 记账分录表 (ledger_entries)
```sql
CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    transaction_date DATE NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    direction VARCHAR(10) NOT NULL, -- DEBIT, CREDIT
    account_id UUID NOT NULL REFERENCES accounts(id),
    item_id UUID REFERENCES items(id),
    category_code VARCHAR(50),
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 2.3 索引设计
```sql
-- 性能优化索引
CREATE INDEX idx_items_user_id ON items(user_id);
CREATE INDEX idx_items_category_id ON items(category_id);
CREATE INDEX idx_items_status ON items(status);
CREATE INDEX idx_items_purchase_date ON items(purchase_date);
CREATE INDEX idx_transactions_user_id ON inventory_transactions(user_id);
CREATE INDEX idx_transactions_item_id ON inventory_transactions(item_id);
CREATE INDEX idx_transactions_date ON inventory_transactions(transaction_date);
CREATE INDEX idx_ledger_user_id ON ledger_entries(user_id);
CREATE INDEX idx_ledger_date ON ledger_entries(transaction_date);
```

## 3. 后端技术方案

### 3.1 项目结构
```
backend/src/main/java/app/inv/
├── InventoryApplication.java
├── config/           # 配置类
├── controller/       # REST控制器
├── service/          # 业务逻辑层
├── repository/       # 数据访问层
├── entity/           # JPA实体
├── dto/              # 数据传输对象
├── exception/        # 异常处理
├── security/         # 安全配置
└── util/             # 工具类
```

### 3.2 核心依赖
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
    runtimeOnly("org.postgresql:postgresql:42.7.4")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
```

### 3.3 API设计规范

#### 3.3.1 RESTful API规范
- 基础URL：`/api/v1`
- 认证：Bearer Token (JWT)
- 响应格式：统一JSON格式
- 分页：`page`, `size`, `sort`参数
- 错误码：HTTP状态码 + 业务错误码

#### 3.3.2 核心API接口

**认证接口**
```
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh
POST /api/v1/auth/logout
```

**物品管理接口**
```
GET    /api/v1/items              # 获取物品列表
POST   /api/v1/items              # 创建物品
GET    /api/v1/items/{id}         # 获取物品详情
PUT    /api/v1/items/{id}         # 更新物品
DELETE /api/v1/items/{id}         # 删除物品
POST   /api/v1/items/{id}/images  # 上传图片
```

**库存交易接口**
```
GET    /api/v1/transactions       # 获取交易列表
POST   /api/v1/transactions       # 创建交易
GET    /api/v1/transactions/{id}  # 获取交易详情
```

**账户管理接口**
```
GET    /api/v1/accounts           # 获取账户列表
POST   /api/v1/accounts           # 创建账户
PUT    /api/v1/accounts/{id}      # 更新账户
DELETE /api/v1/accounts/{id}      # 删除账户
```

**报表接口**
```
GET    /api/v1/reports/inventory-value    # 库存价值报表
GET    /api/v1/reports/disposal-profit    # 处置盈亏报表
GET    /api/v1/reports/trends             # 趋势报表
```

### 3.4 安全方案

#### 3.4.1 JWT认证流程
1. 用户登录 → 验证凭据 → 生成JWT Token
2. 前端存储Token → 请求头携带Authorization
3. 后端验证Token → 提取用户信息
4. Token过期 → 使用Refresh Token续期

#### 3.4.2 安全配置
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // JWT配置
    // 密码加密配置
    // CORS配置
    // 权限控制
}
```

## 4. 前端技术方案

### 4.1 项目结构
```
frontend/src/
├── main.ts
├── App.vue
├── router/           # 路由配置
├── stores/           # Pinia状态管理
├── views/            # 页面组件
├── components/       # 通用组件
├── composables/      # 组合式函数
├── types/            # TypeScript类型
├── utils/            # 工具函数
└── assets/           # 静态资源
```

### 4.2 状态管理设计

#### 4.2.1 Pinia Store结构
```typescript
// stores/auth.ts - 认证状态
export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: null,
    isAuthenticated: false
  }),
  actions: {
    login(credentials),
    logout(),
    refreshToken()
  }
})

// stores/inventory.ts - 库存状态
export const useInventoryStore = defineStore('inventory', {
  state: () => ({
    items: [],
    categories: [],
    transactions: []
  }),
  actions: {
    fetchItems(),
    createItem(),
    updateItem(),
    deleteItem()
  }
})
```

### 4.3 路由设计
```typescript
const routes = [
  { path: '/', component: Dashboard },
  { path: '/items', component: ItemList },
  { path: '/items/new', component: ItemForm },
  { path: '/items/:id', component: ItemDetail },
  { path: '/transactions', component: TransactionList },
  { path: '/reports', component: Reports },
  { path: '/settings', component: Settings }
]
```

### 4.4 组件设计

#### 4.4.1 核心组件
- `ItemForm` - 物品表单（创建/编辑）
- `ItemList` - 物品列表（表格/卡片视图）
- `TransactionForm` - 交易表单
- `ImageUploader` - 图片上传组件
- `DataTable` - 通用数据表格
- `Chart` - 图表组件

#### 4.4.2 页面组件
- `Dashboard` - 仪表盘
- `ItemManagement` - 物品管理
- `TransactionManagement` - 交易管理
- `Reports` - 报表页面
- `Settings` - 设置页面

## 5. 开发规范

### 5.1 代码规范
- **后端**：遵循Spring Boot最佳实践，使用Lombok简化代码
- **前端**：遵循Vue 3 Composition API规范，使用TypeScript严格模式
- **数据库**：遵循第三范式，合理使用索引
- **API**：RESTful设计，统一错误处理

### 5.2 测试策略
- **单元测试**：Service层业务逻辑测试
- **集成测试**：Controller层API测试
- **前端测试**：组件单元测试（Vue Test Utils）
- **E2E测试**：关键业务流程测试

### 5.3 性能优化
- **后端**：数据库查询优化，缓存策略，分页查询
- **前端**：组件懒加载，图片压缩，虚拟滚动
- **网络**：API响应压缩，静态资源CDN

## 6. 部署方案

### 6.1 开发环境
```yaml
# docker-compose.dev.yml
version: '3.8'
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: inventory
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
```

### 6.2 生产环境
- **容器化**：Docker多阶段构建
- **数据库**：PostgreSQL主从配置
- **反向代理**：Nginx负载均衡
- **监控**：应用性能监控（APM）

## 7. 风险评估与应对

### 7.1 技术风险
- **数据库性能**：大量数据查询优化，索引策略
- **文件存储**：图片存储容量规划，CDN方案
- **并发安全**：JWT Token并发刷新问题

### 7.2 业务风险
- **数据一致性**：事务处理，数据校验
- **用户体验**：移动端适配，加载性能
- **数据安全**：用户隐私保护，数据备份

## 8. 开发计划

### 8.1 里程碑规划
- **Week 1-2**：基础架构搭建，数据库设计
- **Week 3-4**：核心功能开发（物品管理、交易）
- **Week 5-6**：记账功能、报表功能
- **Week 7-8**：前端界面、测试优化

### 8.2 交付物
- 可运行的后端服务
- 完整的前端应用
- API文档（Swagger）
- 部署文档
- 用户手册
