# 初始化与运行说明（INIT）

## 先决条件
- JDK 17+
- Node.js 18+
- pnpm 或 npm（推荐 pnpm）
- Docker（可选，用于本地数据库与一键运行）

## 目录结构
```
backend/     # Spring Boot + Gradle 工程
frontend/    # Vue 3 + Vite 工程
docs/        # 文档
```

## 后端初始化
1. 安装依赖并构建：
   ```bash
   cd backend
   ./gradlew build -x test
   ```
2. 运行（本地 profile）：
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=local'
   ```
3. 本地数据库（可选）：
   ```bash
   docker run --name inv-pg -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=inventory -p 5432:5432 -d postgres:16
   ```

## 前端初始化
1. 安装依赖：
   ```bash
   cd frontend
   pnpm install
   ```
2. 启动开发服务器：
   ```bash
   pnpm dev
   ```

## 环境变量
- 后端：`SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD`、`JWT_SECRET`。
- 前端：`VITE_API_BASE` 指向后端，如 `http://localhost:8080/api`。

## 一键启动（可选）
后续将提供 `docker-compose.yml` 实现数据库 + 后端 + 前端一键启动。


