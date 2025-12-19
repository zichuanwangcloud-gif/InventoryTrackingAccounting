# API接口规范文档

> **相关文档**：
> - **API设计原则与通用规范**：见 [API_DESIGN.md](./API_DESIGN.md)
> - **功能进度追踪**：见 [PROGRESS.md](../PROGRESS.md)

---

## 📋 文档信息
- **版本**: V2.0
- **更新时间**: 2025-10-21
- **目标**: 完整的API接口规范和实现状态
- **覆盖范围**: V1.0 - V2.0 所有接口

## 🎯 接口设计原则

### 1. RESTful设计
- 使用HTTP动词表示操作（GET、POST、PUT、DELETE）
- 使用名词表示资源
- 使用复数形式表示集合
- 使用层级结构表示资源关系

### 2. 统一响应格式
```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2025-10-21T10:30:00Z"
}
```

### 3. 错误处理
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
  "timestamp": "2025-10-21T10:30:00Z"
}
```

## 🔐 认证接口

### 用户注册 ✅ 已实现
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
  "message": "注册成功",
  "data": {
    "id": "uuid",
    "username": "string",
    "email": "string"
  },
  "timestamp": "2025-10-21T10:30:00Z"
}
```

### 用户登录 ✅ 已实现
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
  "message": "登录成功",
  "data": {
    "id": "uuid",
    "username": "string",
    "email": "string",
    "token": "jwt_token"
  },
  "timestamp": "2025-10-21T10:30:00Z"
}
```

### 刷新Token ⚠️ 待实现
```http
POST /api/v1/auth/refresh
Content-Type: application/json

{
  "refreshToken": "string"
}
```

### 获取当前用户 ✅ 已实现
```http
GET /api/v1/auth/me
Authorization: Bearer {token}
```

## 📦 物品管理接口

### 获取物品列表 ✅ 已实现
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

### 创建物品 ⚠️ 待实现
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

### 获取物品详情 ⚠️ 待实现
```http
GET /api/v1/items/{id}
Authorization: Bearer {token}
```

### 更新物品 ⚠️ 待实现
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

### 删除物品 ⚠️ 待实现
```http
DELETE /api/v1/items/{id}
Authorization: Bearer {token}
```

### 上传图片 ⚠️ 待实现
```http
POST /api/v1/items/{id}/images
Authorization: Bearer {token}
Content-Type: multipart/form-data

files: [file1, file2, ...]
```

## 💰 库存交易接口

### 获取交易列表 ⚠️ 待实现
```http
GET /api/v1/transactions?page=0&size=20&type=IN&itemId=uuid&dateFrom=2025-01-01&dateTo=2025-12-31
Authorization: Bearer {token}
```

### 创建交易 ⚠️ 待实现
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

### 获取交易详情 ⚠️ 待实现
```http
GET /api/v1/transactions/{id}
Authorization: Bearer {token}
```

## 🏦 账户管理接口

### 获取账户列表 ✅ 已实现
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

### 创建账户 ⚠️ 待实现
```http
POST /api/v1/accounts
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "string",
  "type": "CASH"
}
```

### 更新账户 ⚠️ 待实现
```http
PUT /api/v1/accounts/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "string"
}
```

### 删除账户 ⚠️ 待实现
```http
DELETE /api/v1/accounts/{id}
Authorization: Bearer {token}
```

## 📊 记账接口

### 获取分录列表 ⚠️ 待实现
```http
GET /api/v1/ledger?page=0&size=20&accountId=uuid&dateFrom=2025-01-01&dateTo=2025-12-31
Authorization: Bearer {token}
```

### 获取分录详情 ⚠️ 待实现
```http
GET /api/v1/ledger/{id}
Authorization: Bearer {token}
```

## 📈 报表接口

### 库存价值报表 ✅ 已实现
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

### 处置盈亏报表 ✅ 已实现
```http
GET /api/v1/reports/disposal-profit?dateFrom=2025-01-01&dateTo=2025-12-31
Authorization: Bearer {token}
```

### 趋势分析 ⚠️ 待实现
```http
GET /api/v1/reports/trends?period=MONTHLY&dateFrom=2025-01-01&dateTo=2025-12-31
Authorization: Bearer {token}
```

## 🏷️ 品类管理接口

### 获取品类列表 ⚠️ 待实现
```http
GET /api/v1/categories
Authorization: Bearer {token}
```

### 创建品类 ⚠️ 待实现
```http
POST /api/v1/categories
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "string",
  "parentId": "uuid"
}
```

## 📁 文件上传接口

### 上传图片 ⚠️ 待实现
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

## 🔍 搜索接口 (V1.1)

### 高级搜索 ⚠️ 待实现
```http
GET /api/v1/search/items?q=keyword&filters=category:clothing,price:100-500&sort=price:asc
Authorization: Bearer {token}
```

### 搜索建议 ⚠️ 待实现
```http
GET /api/v1/search/suggestions?q=keyword
Authorization: Bearer {token}
```

## 📱 移动端接口 (V1.1)

### 快速添加物品 ⚠️ 待实现
```http
POST /api/v1/mobile/items/quick
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "string",
  "price": 100.00,
  "image": "base64_string"
}
```

### 拍照识别 ⚠️ 待实现
```http
POST /api/v1/mobile/items/recognize
Authorization: Bearer {token}
Content-Type: multipart/form-data

image: [file]
```

## 🧠 规则引擎接口 (V1.2)

### 获取规则列表 ⚠️ 待实现
```http
GET /api/v1/rules
Authorization: Bearer {token}
```

### 创建规则 ⚠️ 待实现
```http
POST /api/v1/rules
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "string",
  "condition": "string",
  "action": "string",
  "priority": 1
}
```

### 测试规则 ⚠️ 待实现
```http
POST /api/v1/rules/test
Authorization: Bearer {token}
Content-Type: application/json

{
  "ruleId": "uuid",
  "testData": {}
}
```

## 📊 数据导入导出接口 (V1.2)

### 导入数据 ⚠️ 待实现
```http
POST /api/v1/import/items
Authorization: Bearer {token}
Content-Type: multipart/form-data

file: [excel_file]
```

### 导出数据 ⚠️ 待实现
```http
GET /api/v1/export/ledger?format=csv&dateFrom=2025-01-01&dateTo=2025-12-31
Authorization: Bearer {token}
```

## 🤖 AI推荐接口 (V2.0)

### 获取推荐 ⚠️ 待实现
```http
GET /api/v1/ai/recommendations?userId=uuid&limit=10
Authorization: Bearer {token}
```

### 训练模型 ⚠️ 待实现
```http
POST /api/v1/ai/models/train
Authorization: Bearer {token}
Content-Type: application/json

{
  "modelType": "recommendation",
  "trainingData": {}
}
```

### 异常检测 ⚠️ 待实现
```http
GET /api/v1/ai/anomalies?userId=uuid&dateFrom=2025-01-01&dateTo=2025-12-31
Authorization: Bearer {token}
```

## 👥 协作接口 (V2.0)

### 分享物品 ⚠️ 待实现
```http
POST /api/v1/collaboration/share
Authorization: Bearer {token}
Content-Type: application/json

{
  "itemId": "uuid",
  "shareWith": ["user1", "user2"]
}
```

### 添加评论 ⚠️ 待实现
```http
POST /api/v1/collaboration/items/{id}/comments
Authorization: Bearer {token}
Content-Type: application/json

{
  "comment": "string"
}
```

### 获取团队成员 ⚠️ 待实现
```http
GET /api/v1/collaboration/team
Authorization: Bearer {token}
```

## 📊 高级分析接口 (V2.0)

### 预测分析 ⚠️ 待实现
```http
GET /api/v1/analytics/predictions?userId=uuid&months=6
Authorization: Bearer {token}
```

### 趋势分析 ⚠️ 待实现
```http
GET /api/v1/analytics/trends?userId=uuid&startDate=2025-01-01&endDate=2025-12-31
Authorization: Bearer {token}
```

### 用户画像 ⚠️ 待实现
```http
GET /api/v1/analytics/profile?userId=uuid
Authorization: Bearer {token}
```

## 🔌 第三方集成接口 (V2.0)

### 电商平台集成 ⚠️ 待实现
```http
POST /api/v1/integrations/ecommerce/sync
Authorization: Bearer {token}
Content-Type: application/json

{
  "platform": "taobao",
  "credentials": {}
}
```

### 支付系统集成 ⚠️ 待实现
```http
POST /api/v1/integrations/payment/process
Authorization: Bearer {token}
Content-Type: application/json

{
  "amount": 100.00,
  "currency": "CNY",
  "method": "alipay"
}
```

## 📱 实时通知接口 (V2.0)

### WebSocket连接 ⚠️ 待实现
```javascript
// WebSocket连接
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    // 订阅通知
    stompClient.subscribe('/user/queue/notifications', function(message) {
        const notification = JSON.parse(message.body);
        // 处理通知
    });
});
```

### 推送通知 ⚠️ 待实现
```http
POST /api/v1/notifications/push
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "string",
  "body": "string",
  "data": {}
}
```

## 🚨 错误码定义

### HTTP状态码
- `200`: 成功
- `201`: 创建成功
- `400`: 请求参数错误
- `401`: 未授权
- `403`: 禁止访问
- `404`: 资源不存在
- `409`: 资源冲突
- `500`: 服务器内部错误

### 业务错误码
- `10001`: 用户不存在
- `10002`: 密码错误
- `10003`: Token过期
- `20001`: 物品不存在
- `20002`: 物品状态错误
- `30001`: 账户不存在
- `30002`: 账户余额不足
- `40001`: 文件上传失败
- `40002`: 文件格式不支持
- `50001`: 规则执行失败
- `50002`: 模型训练失败
- `60001`: 协作权限不足
- `60002`: 分享失败

## 📄 分页规范

### 分页参数
- `page`: 页码（从0开始）
- `size`: 每页大小（默认20，最大100）
- `sort`: 排序字段，格式：`field,direction`

### 分页响应
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

## 🔍 搜索规范

### 搜索参数
- `search`: 关键字搜索（支持名称、品牌）
- `categoryId`: 品类筛选
- `status`: 状态筛选
- `dateFrom`: 开始日期
- `dateTo`: 结束日期
- `priceFrom`: 最低价格
- `priceTo`: 最高价格

### 搜索响应
搜索结果高亮显示匹配字段，支持模糊匹配和分词搜索。

## 📊 实现状态统计

### V1.0 接口实现状态
- ✅ 认证接口: 3/4 (75%)
- ⚠️ 物品管理: 1/6 (17%)
- ⚠️ 交易管理: 0/3 (0%)
- ✅ 账户管理: 1/4 (25%)
- ⚠️ 记账接口: 0/2 (0%)
- ✅ 报表接口: 2/3 (67%)

### V1.1 接口实现状态
- ⚠️ 搜索接口: 0/2 (0%)
- ⚠️ 移动端接口: 0/2 (0%)

### V1.2 接口实现状态
- ⚠️ 规则引擎: 0/3 (0%)
- ⚠️ 数据导入导出: 0/2 (0%)

### V2.0 接口实现状态
- ⚠️ AI推荐: 0/3 (0%)
- ⚠️ 协作功能: 0/3 (0%)
- ⚠️ 高级分析: 0/3 (0%)
- ⚠️ 第三方集成: 0/2 (0%)
- ⚠️ 实时通知: 0/2 (0%)

## 📝 开发优先级

### 高优先级 (V1.0)
1. 完善物品管理接口
2. 实现交易管理接口
3. 完善账户管理接口
4. 实现记账接口

### 中优先级 (V1.1)
1. 实现搜索接口
2. 实现移动端接口

### 低优先级 (V1.2 & V2.0)
1. 规则引擎接口
2. AI推荐接口
3. 协作功能接口
4. 高级分析接口

---

**文档版本**: V2.0  
**最后更新**: 2025-10-21  
**负责人**: 技术负责人  
**审核人**: 架构师
