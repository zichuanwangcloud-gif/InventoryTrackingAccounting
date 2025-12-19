# API设计文档

> **相关文档**：
> - **具体API端点与实现状态**：见 [API_SPECIFICATION.md](./API_SPECIFICATION.md)
> - **功能进度追踪**：见 [PROGRESS.md](../PROGRESS.md)

---

## 1. API设计原则

### 1.1 RESTful设计
- 使用HTTP动词表示操作（GET、POST、PUT、DELETE）
- 使用名词表示资源
- 使用复数形式表示集合
- 使用层级结构表示资源关系

### 1.2 统一响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2025-10-20T10:30:00Z"
}
```

### 1.3 错误处理
```json
{
  "code": 400,
  "message": "validation error",
  "errors": [
    {
      "field": "name",
      "message": "name is required"
    }
  ],
  "timestamp": "2025-10-20T10:30:00Z"
}
```

## 2. 认证接口

### 2.1 用户注册
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "username": "string",
  "email": "string",
  "password": "string"
}
```

**响应**：
```json
{
  "code": 201,
  "message": "user created successfully",
  "data": {
    "id": "uuid",
    "username": "string",
    "email": "string",
    "token": "jwt_token"
  }
}
```

### 2.2 用户登录
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "username": "string",
  "password": "string"
}
```

**响应**：
```json
{
  "code": 200,
  "message": "login successful",
  "data": {
    "id": "uuid",
    "username": "string",
    "email": "string",
    "token": "jwt_token",
    "refreshToken": "refresh_token"
  }
}
```

### 2.3 刷新Token
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "string"
}
```

## 3. 物品管理接口

### 3.1 获取物品列表
```http
GET /api/v1/items?page=0&size=20&sort=createdAt,desc&search=keyword&categoryId=uuid&status=ACTIVE
Authorization: Bearer {token}
```

**查询参数**：
- `page`: 页码（从0开始）
- `size`: 每页大小（默认20）
- `sort`: 排序字段和方向
- `search`: 搜索关键字
- `categoryId`: 品类ID
- `status`: 物品状态

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "content": [
      {
        "id": "uuid",
        "name": "string",
        "brand": "string",
        "purchasePrice": 100.00,
        "purchaseDate": "2025-10-20",
        "status": "ACTIVE",
        "images": ["url1", "url2"],
        "createdAt": "2025-10-20T10:30:00Z"
      }
    ],
    "totalElements": 100,
    "totalPages": 5,
    "size": 20,
    "number": 0
  }
}
```

### 3.2 创建物品
```http
POST /api/v1/items
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "string",
  "categoryId": "uuid",
  "brand": "string",
  "size": "string",
  "color": "string",
  "purchasePrice": 100.00,
  "purchaseDate": "2025-10-20",
  "location": "string"
}
```

### 3.3 获取物品详情
```http
GET /api/v1/items/{id}
Authorization: Bearer {token}
```

### 3.4 更新物品
```http
PUT /api/v1/items/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "string",
  "brand": "string",
  "purchasePrice": 150.00
}
```

### 3.5 删除物品
```http
DELETE /api/v1/items/{id}
Authorization: Bearer {token}
```

### 3.6 上传图片
```http
POST /api/v1/items/{id}/images
Authorization: Bearer {token}
Content-Type: multipart/form-data

files: [file1, file2, ...]
```

## 4. 库存交易接口

### 4.1 获取交易列表
```http
GET /api/v1/transactions?page=0&size=20&type=IN&itemId=uuid&dateFrom=2025-01-01&dateTo=2025-12-31
Authorization: Bearer {token}
```

### 4.2 创建交易
```http
POST /api/v1/transactions
Authorization: Bearer {token}
Content-Type: application/json

{
  "itemId": "uuid",
  "type": "IN",
  "quantity": 1,
  "unitPrice": 100.00,
  "totalAmount": 100.00,
  "transactionDate": "2025-10-20",
  "reason": "PURCHASE",
  "notes": "string",
  "accountId": "uuid"
}
```

### 4.3 获取交易详情
```http
GET /api/v1/transactions/{id}
Authorization: Bearer {token}
```

## 5. 账户管理接口

### 5.1 获取账户列表
```http
GET /api/v1/accounts
Authorization: Bearer {token}
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "uuid",
      "name": "现金",
      "type": "CASH",
      "balance": 1000.00,
      "createdAt": "2025-10-20T10:30:00Z"
    }
  ]
}
```

### 5.2 创建账户
```http
POST /api/v1/accounts
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "string",
  "type": "CASH"
}
```

### 5.3 更新账户
```http
PUT /api/v1/accounts/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "string"
}
```

### 5.4 删除账户
```http
DELETE /api/v1/accounts/{id}
Authorization: Bearer {token}
```

## 6. 记账接口

### 6.1 获取分录列表
```http
GET /api/v1/ledger?page=0&size=20&accountId=uuid&dateFrom=2025-01-01&dateTo=2025-12-31
Authorization: Bearer {token}
```

### 6.2 获取分录详情
```http
GET /api/v1/ledger/{id}
Authorization: Bearer {token}
```

## 7. 报表接口

### 7.1 库存价值报表
```http
GET /api/v1/reports/inventory-value?groupBy=category&dateFrom=2025-01-01&dateTo=2025-12-31
Authorization: Bearer {token}
```

**响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalValue": 5000.00,
    "groups": [
      {
        "name": "服装",
        "value": 3000.00,
        "count": 15
      }
    ]
  }
}
```

### 7.2 处置盈亏报表
```http
GET /api/v1/reports/disposal-profit?dateFrom=2025-01-01&dateTo=2025-12-31
Authorization: Bearer {token}
```

### 7.3 趋势分析
```http
GET /api/v1/reports/trends?period=MONTHLY&dateFrom=2025-01-01&dateTo=2025-12-31
Authorization: Bearer {token}
```

## 8. 品类管理接口

### 8.1 获取品类列表
```http
GET /api/v1/categories
Authorization: Bearer {token}
```

### 8.2 创建品类
```http
POST /api/v1/categories
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "string",
  "parentId": "uuid"
}
```

## 9. 文件上传接口

### 9.1 上传图片
```http
POST /api/v1/files/images
Authorization: Bearer {token}
Content-Type: multipart/form-data

file: [file]
```

**响应**：
```json
{
  "code": 200,
  "message": "upload successful",
  "data": {
    "url": "https://example.com/images/uuid.jpg",
    "filename": "image.jpg",
    "size": 1024000
  }
}
```

## 10. 错误码定义

### 10.1 HTTP状态码
- `200`: 成功
- `201`: 创建成功
- `400`: 请求参数错误
- `401`: 未授权
- `403`: 禁止访问
- `404`: 资源不存在
- `409`: 资源冲突
- `500`: 服务器内部错误

### 10.2 业务错误码
- `10001`: 用户不存在
- `10002`: 密码错误
- `10003`: Token过期
- `20001`: 物品不存在
- `20002`: 物品状态错误
- `30001`: 账户不存在
- `30002`: 账户余额不足
- `40001`: 文件上传失败
- `40002`: 文件格式不支持

## 11. 分页规范

### 11.1 分页参数
- `page`: 页码（从0开始）
- `size`: 每页大小（默认20，最大100）
- `sort`: 排序字段，格式：`field,direction`

### 11.2 分页响应
```json
{
  "content": [],
  "totalElements": 100,
  "totalPages": 5,
  "size": 20,
  "number": 0,
  "first": true,
  "last": false
}
```

## 12. 搜索规范

### 12.1 搜索参数
- `search`: 关键字搜索（支持名称、品牌）
- `categoryId`: 品类筛选
- `status`: 状态筛选
- `dateFrom`: 开始日期
- `dateTo`: 结束日期
- `priceFrom`: 最低价格
- `priceTo`: 最高价格

### 12.2 搜索响应
搜索结果高亮显示匹配字段，支持模糊匹配和分词搜索。
