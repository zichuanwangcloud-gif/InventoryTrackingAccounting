# 库存记账系统测试指南

本文档详细说明了如何运行库存记账系统的各种测试，包括单元测试、集成测试、E2E测试和系统测试。

## 📋 测试概览

### 后端测试
- **单元测试**: 使用JUnit 5 + Mockito测试业务逻辑
- **集成测试**: 使用TestContainers测试数据库集成
- **WebMvc测试**: 测试REST API端点
- **覆盖率报告**: 使用JaCoCo生成代码覆盖率报告

### 前端测试
- **单元测试**: 使用Vitest + Vue Test Utils测试组件
- **E2E测试**: 使用Playwright测试用户交互
- **Store测试**: 测试Pinia状态管理

### 系统测试
- **Docker容器化**: 使用Docker Compose编排完整系统
- **健康检查**: 监控服务状态
- **自动化测试**: 端到端自动化测试流程

## 🚀 快速开始

### 1. 环境要求

- Docker & Docker Compose
- Node.js 18+ (前端开发)
- Java 17+ (后端开发)
- Gradle 8.5+ (后端构建)

### 2. 运行所有测试

```bash
# 使用测试脚本（推荐）
./test-scripts.sh

# 选择选项 9 运行所有测试
```

### 3. 手动运行测试

#### 后端测试

```bash
cd backend

# 运行单元测试
./gradlew test

# 生成覆盖率报告
./gradlew jacocoTestReport

# 查看报告
open build/reports/jacoco/test/html/index.html
```

#### 前端测试

```bash
cd frontend

# 安装依赖
npm install

# 运行单元测试
npm run test

# 运行E2E测试
npm run test:e2e

# 运行测试覆盖率
npm run test:coverage
```

## 🐳 Docker测试

### 构建镜像

```bash
# 构建后端镜像
docker build -t inventory-backend ./backend

# 构建前端镜像
docker build -t inventory-frontend ./frontend
```

### 运行系统测试

```bash
# 启动完整系统
docker-compose up --build -d

# 运行测试环境
docker-compose -f docker-compose.test.yml up --build

# 清理环境
docker-compose down -v
```

## 📊 测试报告

### 后端测试报告
- **位置**: `backend/build/reports/jacoco/test/html/index.html`
- **内容**: 代码覆盖率、测试结果详情

### 前端测试报告
- **单元测试**: 控制台输出 + HTML报告
- **E2E测试**: `frontend/test-results/` 目录

### 系统测试报告
- **Docker日志**: `docker-compose logs`
- **健康检查**: 各服务的健康状态

## 🔧 测试配置

### 后端测试配置

#### build.gradle.kts
```kotlin
dependencies {
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
}

jacoco {
    toolVersion = "0.8.10"
}
```

#### 测试类结构
```
src/test/java/
├── app/inv/
│   ├── service/
│   │   ├── AuthServiceTest.java
│   │   ├── ItemServiceTest.java
│   │   └── TransactionServiceTest.java
│   ├── controller/
│   │   ├── AuthControllerTest.java
│   │   └── ItemControllerTest.java
│   ├── util/
│   │   └── JwtUtilTest.java
│   └── it/
│       └── ApplicationIT.java
```

### 前端测试配置

#### package.json
```json
{
  "scripts": {
    "test": "vitest",
    "test:ui": "vitest --ui",
    "test:coverage": "vitest --coverage",
    "test:e2e": "playwright test"
  },
  "devDependencies": {
    "@vue/test-utils": "^2.4.6",
    "vitest": "^2.1.8",
    "@playwright/test": "^1.49.1"
  }
}
```

#### 测试文件结构
```
src/test/
├── setup.ts
├── stores/
│   └── auth.test.ts
├── views/
│   └── Login.test.ts
└── e2e/
    └── auth.spec.ts
```

## 🏗️ Docker配置

### 后端Dockerfile特点
- **多阶段构建**: 分离构建和运行环境
- **安全**: 非root用户运行
- **健康检查**: 自动监控服务状态
- **优化**: 最小化镜像大小

### 前端Dockerfile特点
- **Nginx**: 高性能静态文件服务
- **缓存**: 静态资源长期缓存
- **代理**: API请求代理到后端
- **SPA支持**: 单页应用路由支持

### Docker Compose配置

#### 生产环境 (docker-compose.yml)
- PostgreSQL数据库
- 后端Spring Boot应用
- 前端Nginx服务
- Redis缓存
- 健康检查和依赖管理

#### 测试环境 (docker-compose.test.yml)
- 独立测试数据库
- 测试专用服务
- E2E测试容器
- 自动化测试流程

## 📈 测试最佳实践

### 1. 测试金字塔
```
    /\
   /  \     E2E测试 (少量)
  /____\    
 /      \   集成测试 (适量)
/________\  单元测试 (大量)
```

### 2. 测试命名规范
- **单元测试**: `should_do_something_when_condition`
- **集成测试**: `IntegrationTest_FeatureName`
- **E2E测试**: `用户应该能够_执行操作`

### 3. 测试数据管理
- 使用工厂模式创建测试数据
- 每个测试独立的数据集
- 测试后清理数据

### 4. 持续集成
- 每次提交自动运行测试
- 测试失败阻止部署
- 生成测试报告和覆盖率

## 🐛 故障排除

### 常见问题

#### 1. Docker构建失败
```bash
# 清理Docker缓存
docker system prune -a

# 重新构建
docker-compose build --no-cache
```

#### 2. 测试数据库连接失败
```bash
# 检查数据库状态
docker-compose ps postgres

# 查看数据库日志
docker-compose logs postgres
```

#### 3. 前端测试失败
```bash
# 安装Playwright浏览器
npx playwright install

# 清理node_modules
rm -rf node_modules package-lock.json
npm install
```

#### 4. 端口冲突
```bash
# 检查端口占用
netstat -tulpn | grep :8080

# 修改docker-compose.yml中的端口映射
```

## 📚 相关文档

- [Spring Boot测试指南](https://spring.io/guides/gs/testing-web/)
- [Vue测试指南](https://vuejs.org/guide/scaling-up/testing.html)
- [Playwright文档](https://playwright.dev/)
- [Docker最佳实践](https://docs.docker.com/develop/dev-best-practices/)

## 🤝 贡献指南

1. 编写测试前先写代码
2. 保持测试覆盖率 > 80%
3. 每个功能都要有对应的测试
4. 测试应该快速、独立、可重复
5. 及时更新测试文档

---

**注意**: 运行测试前请确保所有依赖都已正确安装，Docker服务正在运行。
