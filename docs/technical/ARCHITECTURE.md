# 架构设计（Architecture）

## 目标
为个人“物品库存 + 记账”场景提供稳定、可扩展、易维护的系统。支持入库（购买）、出库（丢弃/转售/赠与）、库存查询、资产价值统计、记账联动。

## 总体架构
- 前端：Vue 3 + Vite + TypeScript，组件化开发，Pinia 管理状态，Vue Router 做路由。
- 后端：Java 17 + Spring Boot 3，Spring Web、Spring Data JPA、Spring Validation，数据库优先使用 PostgreSQL。
- 构建与依赖：Gradle（Kotlin DSL）。
- 部署：Docker（多阶段构建），可单机或 Compose 启动。
- 认证与鉴权：暂定 JWT（后续可扩展 OAuth2 / 密码学登录）。

## 模块划分
- `backend`
  - `inventory`：物品/库存域（Item、Category、InventoryTransaction）。
  - `accounting`：记账域（Account、LedgerEntry、CategoryMapping）。
  - `shared`：通用（错误、分页、审计字段、安规）。
- `frontend`
  - 页面：仪表盘、入库、出库、库存、记账、报表、设置。
  - 组件：物品表单、交易表格、筛选器、上传（小票/图片）。
  - store：`inventoryStore`、`accountingStore`、`userStore`。

## 领域模型（简化）
- Item（物品）：id、name、categoryId、brand、size、color、purchasePrice、purchaseDate、location、images[]、status（ACTIVE/REMOVED）。
- InventoryTransaction（库存交易）：id、itemId、type（IN/OUT/ADJUST）、quantity、unitPrice、totalAmount、date、notes、reason（SELL/DISPOSE/GIFT/LOST）。
- Account（账户）：id、name、type（CASH/BANK/PLATFORM/OTHER）。
- LedgerEntry（分录）：id、date、amount、direction（DEBIT/CREDIT）、accountId、itemId?、categoryCode、note。
- Category（品类）：id、name、parentId?，支持树状。
- CategoryMapping：库存品类到记账科目的映射。

## 关键用例流
1) 购买入库：录入物品与金额 -> 生成 IN 交易 -> 生成记账分录（借：存货/个人资产；贷：现金/银行）。
2) 出库-转售：选择物品 -> 标记 OUT（SELL） -> 记录售出金额与平台费 -> 生成分录（借：现金；贷：存货/资产 + 收益/损益）。
3) 出库-丢弃/赠与：标记 OUT（DISPOSE/GIFT） -> 生成分录（借：处置损失；贷：存货/资产）。
4) 统计与报表：按品类/时间/品牌统计数量、金额、持有成本、未实现盈亏等。

## API 纲要（初稿）
- `/api/items` GET/POST
- `/api/items/{id}` GET/PUT/DELETE
- `/api/transactions` GET/POST
- `/api/transactions/{id}` GET/PUT/DELETE
- `/api/accounts` GET/POST
- `/api/ledger` GET/POST
- `/api/categories` GET/POST
- `/api/mappings` GET/POST
- 认证：`/api/auth/login`、`/api/auth/refresh`

分页、排序、过滤采用通用查询参数（page, size, sort, q, dateFrom, dateTo, categoryId 等）。

## 数据库设计（简化）
- items(id PK, name, category_id, brand, size, color, purchase_price, purchase_date, location, images_json, status, created_at, updated_at)
- inventory_tx(id PK, item_id FK, type, quantity, unit_price, total_amount, date, reason, notes, created_at)
- accounts(id PK, name, type, created_at)
- ledger(id PK, date, amount, direction, account_id FK, item_id FK null, category_code, note)
- categories(id PK, name, parent_id null)
- category_mapping(id PK, inventory_category_id FK, ledger_category_code)
- users(id PK, username unique, password_hash, created_at)

索引：常用查询字段（item_id、date、category_id、account_id、type）。

## 非功能性要求
- 安全：JWT、输入校验、速率限制（后续 Nginx/网关）。
- 可观测：结构化日志、简单审计字段。
- 性能：N+1 避免，列表分页，必要字段索引。
- 可靠性：数据库迁移（Liquibase/Flyway），单元/集成测试基线。

## 演进路线
MVP -> 扫码入库（小票 OCR/条码）-> 多账户资金流 -> 多用户/家庭共享 -> 移动端优化。


