# 技术方案文档 V1.0 - MVP核心功能

## 📋 文档信息
- **版本**: V1.0
- **发布时间**: 2025年11月（3周Sprint）
- **目标**: 完成"入库→出库→报表"闭环，解决核心痛点
- **技术难点**: 多租户数据隔离架构设计

## 🎯 技术目标

### 核心原则
- **渐进式架构**: 从单体架构开始，为后续微服务化预留接口
- **多租户安全**: 确保用户数据完全隔离
- **性能优先**: 响应时间≤300ms，支持50并发用户
- **可扩展性**: 预留云存储、缓存、监控等扩展接口

## 🏗️ 整体架构设计

### 架构图
```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端 (Vue 3)   │    │   后端 (Spring) │    │   数据库 (PG)   │
│                 │    │                 │    │                 │
│  ┌─────────────┐│    │  ┌─────────────┐│    │  ┌─────────────┐│
│  │ 用户认证    ││    │  │ JWT认证     ││    │  │ 用户数据    ││
│  │ 物品管理    ││◄──►│  │ 物品服务    ││◄──►│  │ 物品数据    ││
│  │ 交易管理    ││    │  │ 交易服务    ││    │  │ 交易数据    ││
│  │ 报表展示    ││    │  │ 报表服务    ││    │  │ 账户数据    ││
│  └─────────────┘│    │  └─────────────┘│    │  └─────────────┘│
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

### 技术栈选型
- **前端**: Vue 3 + TypeScript + Vite + Pinia
- **后端**: Spring Boot 3.3.4 + Java 17 + Spring Security
- **数据库**: PostgreSQL 14 + Flyway迁移
- **认证**: JWT + Spring Security
- **构建**: Gradle + Docker

## 🗄️ 数据库设计

### 核心表结构

#### 1. 用户表 (users) ✅ 已实现
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 2. 物品表 (items) ✅ 已实现
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
    images JSONB,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);
```

#### 3. 品类表 (categories) ✅ 已实现
```sql
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL,
    parent_id UUID REFERENCES categories(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 4. 库存交易表 (inventory_transactions) ✅ 已实现
```sql
CREATE TABLE inventory_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    item_id UUID NOT NULL REFERENCES items(id),
    type VARCHAR(20) NOT NULL,
    quantity INTEGER DEFAULT 1,
    unit_price DECIMAL(18,2) NOT NULL,
    total_amount DECIMAL(18,2) NOT NULL,
    transaction_date DATE NOT NULL,
    reason VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 5. 账户表 (accounts) ✅ 已实现
```sql
CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 6. 记账分录表 (ledger_entries) ✅ 已实现
```sql
CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    transaction_date DATE NOT NULL,
    amount DECIMAL(18,2) NOT NULL,
    direction VARCHAR(10) NOT NULL,
    account_id UUID NOT NULL REFERENCES accounts(id),
    item_id UUID REFERENCES items(id),
    category_code VARCHAR(50),
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 索引设计 ✅ 已实现
```sql
-- 性能优化索引
CREATE INDEX idx_items_user_id ON items(user_id);
CREATE INDEX idx_items_category_id ON items(category_id);
CREATE INDEX idx_items_status ON items(status);
CREATE INDEX idx_transactions_user_id ON inventory_transactions(user_id);
CREATE INDEX idx_transactions_item_id ON inventory_transactions(item_id);
CREATE INDEX idx_ledger_user_id ON ledger_entries(user_id);
```

## 🔧 后端技术方案

### 项目结构 ✅ 已实现
```
backend/src/main/java/app/inv/
├── InventoryApplication.java
├── config/           # 配置类
├── controller/       # REST控制器 ✅
├── service/          # 业务逻辑层 ✅
├── repository/       # 数据访问层 ✅
├── entity/           # JPA实体 ✅
├── dto/              # 数据传输对象 ✅
├── exception/        # 异常处理 ✅
├── security/         # 安全配置 ✅
└── util/             # 工具类 ✅
```

### 核心依赖 ✅ 已实现
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

### 多租户数据隔离方案

#### 1. 用户上下文管理
```java
@Component
public class UserContext {
    private static final ThreadLocal<UUID> currentUser = new ThreadLocal<>();
    
    public static void setCurrentUser(UUID userId) {
        currentUser.set(userId);
    }
    
    public static UUID getCurrentUser() {
        return currentUser.get();
    }
    
    public static void clear() {
        currentUser.remove();
    }
}
```

#### 2. JWT认证增强
```java
@Service
public class JwtService {
    private static final String SECRET_KEY = "your-secret-key";
    private static final long EXPIRATION_TIME = 86400000; // 24小时
    
    public String generateToken(User user) {
        return Jwts.builder()
            .setSubject(user.getId().toString())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }
    
    public UUID extractUserId(String token) {
        return UUID.fromString(extractClaim(token, Claims::getSubject));
    }
}
```

#### 3. 数据访问层增强
```java
@Repository
public interface ItemRepository extends JpaRepository<Item, UUID> {
    @Query("SELECT i FROM Item i WHERE i.userId = :userId")
    Page<Item> findByUserId(@Param("userId") UUID userId, Pageable pageable);
    
    @Query("SELECT i FROM Item i WHERE i.userId = :userId AND i.status = :status")
    List<Item> findByUserIdAndStatus(@Param("userId") UUID userId, @Param("status") ItemStatus status);
}
```

## 🎨 前端技术方案

### 项目结构 ✅ 已实现
```
frontend/src/
├── main.ts
├── App.vue
├── router/           # 路由配置 ✅
├── stores/           # Pinia状态管理 ✅
├── views/            # 页面组件 ✅
├── components/       # 通用组件 ✅
├── utils/            # 工具函数 ✅
└── assets/           # 静态资源 ✅
```

### 状态管理设计 ✅ 已实现
```typescript
// stores/auth.ts - 认证状态
export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: null,
    isAuthenticated: false
  }),
  actions: {
    async login(username: string, password: string) {
      // 实现登录逻辑
    },
    logout() {
      // 实现登出逻辑
    }
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
    async fetchItems() {
      // 实现获取物品列表
    },
    async createItem(item: Item) {
      // 实现创建物品
    }
  }
})
```

## 🔌 API接口设计

### 认证接口 ✅ 已实现
```http
POST /api/v1/auth/register    # 用户注册 ✅
POST /api/v1/auth/login       # 用户登录 ✅
POST /api/v1/auth/refresh     # 刷新Token ⚠️ 待实现
GET  /api/v1/auth/me          # 获取当前用户 ✅
```

### 物品管理接口 ✅ 已实现
```http
GET    /api/v1/items              # 获取物品列表 ✅
POST   /api/v1/items              # 创建物品 ⚠️ 待实现
GET    /api/v1/items/{id}         # 获取物品详情 ⚠️ 待实现
PUT    /api/v1/items/{id}         # 更新物品 ⚠️ 待实现
DELETE /api/v1/items/{id}         # 删除物品 ⚠️ 待实现
POST   /api/v1/items/{id}/images  # 上传图片 ⚠️ 待实现
```

### 交易管理接口 ⚠️ 部分实现
```http
GET    /api/v1/transactions       # 获取交易列表 ⚠️ 待实现
POST   /api/v1/transactions       # 创建交易 ⚠️ 待实现
GET    /api/v1/transactions/{id}  # 获取交易详情 ⚠️ 待实现
```

### 账户管理接口 ✅ 已实现
```http
GET    /api/v1/accounts           # 获取账户列表 ✅
POST   /api/v1/accounts           # 创建账户 ⚠️ 待实现
PUT    /api/v1/accounts/{id}      # 更新账户 ⚠️ 待实现
DELETE /api/v1/accounts/{id}      # 删除账户 ⚠️ 待实现
```

### 报表接口 ✅ 已实现
```http
GET    /api/v1/reports/inventory-value    # 库存价值报表 ✅
GET    /api/v1/reports/disposal-profit    # 处置盈亏报表 ✅
GET    /api/v1/reports/trends             # 趋势报表 ⚠️ 待实现
```

## 🚀 技术难点实现

### 1. 多租户数据隔离
**实现方案**: 在Service层统一添加用户ID过滤
```java
@Service
public class ItemService {
    public Page<Item> getItemsByUserWithFilters(User user, String search, UUID categoryId, ItemStatus status, Pageable pageable) {
        return itemRepository.findByUserIdAndFilters(user.getId(), search, categoryId, status, pageable);
    }
}
```

### 2. 图片存储优化
**实现方案**: 本地文件系统 + 预留云存储接口
```java
@Service
public class FileStorageService {
    @Value("${app.storage.type:local}")
    private String storageType;
    
    public String uploadImage(MultipartFile file) {
        if ("local".equals(storageType)) {
            return uploadToLocal(file);
        } else if ("s3".equals(storageType)) {
            return uploadToS3(file);
        }
        throw new UnsupportedOperationException("Unsupported storage type: " + storageType);
    }
}
```

### 3. 事务一致性保证
**实现方案**: 使用Spring事务管理
```java
@Service
@Transactional
public class TransactionService {
    public InventoryTransaction createTransaction(TransactionRequest request) {
        // 1. 创建交易记录
        InventoryTransaction transaction = new InventoryTransaction();
        // ... 设置属性
        
        // 2. 更新物品状态
        Item item = itemRepository.findById(request.getItemId()).orElseThrow();
        if ("OUT".equals(request.getType())) {
            item.setStatus(ItemStatus.REMOVED);
        }
        
        // 3. 生成记账分录
        generateLedgerEntries(transaction);
        
        return transactionRepository.save(transaction);
    }
}
```

## 📊 性能优化方案

### 1. 数据库优化
- **索引策略**: 为常用查询字段建立索引
- **分页查询**: 使用Spring Data JPA分页
- **查询优化**: 避免N+1查询问题

### 2. 缓存策略
```java
@Service
public class CategoryService {
    @Cacheable("categories")
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
```

### 3. 前端优化
- **组件懒加载**: 使用Vue 3的defineAsyncComponent
- **图片压缩**: 前端图片压缩后上传
- **虚拟滚动**: 大列表使用虚拟滚动

## 🧪 测试策略

### 1. 单元测试 ✅ 已实现
```java
@ExtendWith(MockitoExtension.class)
class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;
    
    @InjectMocks
    private ItemService itemService;
    
    @Test
    void shouldCreateItemSuccessfully() {
        // 测试用例
    }
}
```

### 2. 集成测试 ✅ 已实现
```java
@SpringBootTest
@Testcontainers
class DatabaseIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14");
    
    @Test
    void shouldRetrieveCategories() {
        // 集成测试用例
    }
}
```

### 3. 端到端测试
```typescript
// frontend/tests/e2e/login.spec.ts
import { test, expect } from '@playwright/test';

test('用户登录流程', async ({ page }) => {
  await page.goto('/login');
  await page.fill('[data-testid=username]', 'testuser');
  await page.fill('[data-testid=password]', 'testpass');
  await page.click('[data-testid=login-button]');
  await expect(page).toHaveURL('/dashboard');
});
```

## 🚦 部署方案

### 1. 开发环境 ✅ 已实现
```yaml
# docker-compose.dev.yml
version: '3.8'
services:
  postgres:
    image: postgres:14
    environment:
      POSTGRES_DB: inventory
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
```

### 2. 生产环境
```yaml
# docker-compose.prod.yml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    depends_on:
      - postgres
  
  postgres:
    image: postgres:14
    environment:
      POSTGRES_DB: inventory
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

## 📈 监控和日志

### 1. 应用监控
```java
@Component
public class PerformanceMonitor {
    private final MeterRegistry meterRegistry;
    
    @EventListener
    public void handleItemCreated(ItemCreatedEvent event) {
        meterRegistry.counter("items.created").increment();
    }
}
```

### 2. 日志配置
```yaml
logging:
  level:
    app.inv: DEBUG
    org.hibernate.SQL: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## 🔮 扩展预留

### 1. 云存储接口
```java
public interface StorageService {
    String uploadFile(MultipartFile file);
    void deleteFile(String filePath);
    String getFileUrl(String filePath);
}
```

### 2. 缓存接口
```java
public interface CacheService {
    <T> T get(String key, Class<T> type);
    void put(String key, Object value, Duration ttl);
    void evict(String key);
}
```

### 3. 消息队列接口
```java
public interface MessageService {
    void publish(String topic, Object message);
    void subscribe(String topic, MessageHandler handler);
}
```

## 📝 开发计划

### Sprint 1 (第1周)
- [x] 基础架构搭建
- [x] 数据库设计和迁移
- [x] 用户认证系统
- [ ] JWT Token完整实现

### Sprint 2 (第2周)
- [ ] 物品管理功能完善
- [ ] 图片上传功能
- [ ] 交易管理功能
- [ ] 前端页面开发

### Sprint 3 (第3周)
- [ ] 报表功能开发
- [ ] 搜索筛选功能
- [ ] 测试和优化
- [ ] 部署和文档

## 🎯 验收标准

### 功能验收
- [ ] 用户注册登录功能正常
- [ ] 物品CRUD操作完整
- [ ] 交易记录功能正常
- [ ] 基础报表功能可用
- [ ] 搜索筛选功能正常

### 性能验收
- [ ] 页面加载时间≤2秒
- [ ] API响应时间≤300ms
- [ ] 支持50个并发用户
- [ ] 数据库查询优化

### 安全验收
- [ ] 用户数据完全隔离
- [ ] JWT认证安全可靠
- [ ] 文件上传安全验证
- [ ] 敏感信息加密存储

---

**文档版本**: V1.0  
**最后更新**: 2025-10-21  
**负责人**: 技术负责人  
**审核人**: 架构师
