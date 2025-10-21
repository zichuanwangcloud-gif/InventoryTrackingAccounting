# 数据库配置总结

## 已完成的配置

### 1. PostgreSQL 安装
- ✅ 已安装 PostgreSQL 14
- ✅ 服务已启动并设置为开机自启
- ✅ 版本：PostgreSQL 14.19

### 2. 数据库创建
- ✅ 数据库名称：`inventory`
- ✅ 主用户：`postgres` (密码: `postgres`)
- ✅ 应用用户：`inventory_user` (密码: `inventory_password`)

### 3. 数据库结构
已创建以下表：
- `users` - 用户表
- `categories` - 品类表（已插入默认数据）
- `items` - 物品表
- `inventory_transactions` - 库存交易表
- `accounts` - 账户表
- `ledger_entries` - 记账分录表

### 4. 连接信息
- **主机**: localhost
- **端口**: 5432
- **数据库**: inventory
- **用户名**: postgres
- **密码**: postgres

### 5. 应用配置
项目配置文件中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/inventory
    username: postgres
    password: postgres
```

## 验证结果
- ✅ 数据库连接正常
- ✅ 所有表已创建
- ✅ 默认品类数据已插入
- ✅ 索引已创建

## 下一步
现在可以启动后端应用程序，它将自动连接到配置的PostgreSQL数据库。

## 常用命令
```bash
# 连接数据库
psql -h localhost -U postgres -d inventory

# 查看所有表
\dt

# 查看表结构
\d table_name

# 退出
\q
```
