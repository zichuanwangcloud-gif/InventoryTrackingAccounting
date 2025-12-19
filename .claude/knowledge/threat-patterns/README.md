# Threat Patterns Knowledge Base

威胁模式知识库，供 `threat-modeler` agent 使用。

## 文件说明

| 文件 | 用途 | 内容 |
|-----|------|-----|
| `endpoint-threats.json` | 端点→威胁映射 | 基于 URL 路径特征的威胁规则 |
| `component-threats.json` | 组件→威胁映射 | 基于技术组件的威胁规则 |
| `architecture-threats.json` | 架构→威胁映射 | 基于架构模式的威胁规则 |

## 威胁分类

### 按端点类型

- **authentication** - 认证相关端点（登录、注册、密码重置）
- **fileOperation** - 文件操作端点（上传、下载、导入导出）
- **dataOperation** - 数据操作端点（CRUD）
- **proxy** - 代理/重定向端点
- **template** - 模板渲染端点
- **execution** - 执行/命令端点
- **serialization** - 序列化/数据格式端点
- **admin** - 管理后台端点

### 按组件类型

- **databases** - MySQL、PostgreSQL、MongoDB、Redis、Elasticsearch
- **messageQueues** - RabbitMQ、Kafka
- **cloudServices** - S3、Lambda
- **externalAPIs** - Payment、OAuth、SMTP

### 按架构模式

- **mvc** - MVC 架构
- **restApi** - REST API
- **graphql** - GraphQL API
- **microservices** - 微服务架构
- **rpc** - RPC/gRPC
- **serverless** - 无服务器架构
- **spa** - 单页应用

## 优先级定义

| 优先级 | 定义 | 示例漏洞 |
|-------|------|---------|
| P0 | 可能导致 RCE、数据泄露、认证绕过 | SQLi、文件上传、SSRF、反序列化 |
| P1 | 可能导致权限问题、敏感操作 | IDOR、XSS、开放重定向 |
| P2 | 可能导致信息泄露、低危问题 | 用户枚举、错误信息泄露 |

## 数据结构

### 威胁定义

```json
{
  "id": "AUTH-001",
  "name": "Brute Force Attack",
  "nameZh": "暴力破解",
  "priority": "P1",
  "cwes": ["CWE-307"],
  "conditions": ["optional_conditions"],
  "checkPoints": [
    "检查是否有登录频率限制",
    "检查是否有账户锁定机制"
  ],
  "suggestedAgent": "auth-bypass-agent"
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|-----|------|-----|
| `id` | string | 威胁唯一标识 |
| `name` | string | 英文名称 |
| `nameZh` | string | 中文名称 |
| `priority` | P0/P1/P2 | 优先级 |
| `cwes` | string[] | 关联的 CWE 编号 |
| `conditions` | string[] | 触发条件（可选） |
| `checkPoints` | string[] | 检查点列表 |
| `suggestedAgent` | string | 建议使用的漏洞检测 Agent |

## 扩展知识库

### 添加新的端点威胁

在 `endpoint-threats.json` 中添加新的模式：

```json
{
  "patterns": {
    "newCategory": {
      "pathPatterns": ["/new", "/pattern"],
      "threats": [
        {
          "id": "NEW-001",
          "name": "New Threat",
          "nameZh": "新威胁",
          "priority": "P1",
          "cwes": ["CWE-XXX"],
          "checkPoints": ["检查点1", "检查点2"]
        }
      ]
    }
  }
}
```

### 添加新的组件威胁

在 `component-threats.json` 中添加：

```json
{
  "components": {
    "newCategory": {
      "newComponent": {
        "name": "Component Name",
        "threats": [...]
      }
    }
  }
}
```

### 添加新的架构威胁

在 `architecture-threats.json` 中添加：

```json
{
  "architectures": {
    "newArch": {
      "name": "Architecture Name",
      "indicators": ["indicator1", "indicator2"],
      "threats": [...]
    }
  }
}
```

## 使用方式

威胁建模 Agent 会自动加载这些知识库文件，用于：

1. **路径匹配** - 将端点路径与 `pathPatterns` 匹配
2. **组件匹配** - 将技术栈与组件类型匹配
3. **架构推断** - 根据 `indicators` 推断架构模式
4. **威胁映射** - 输出匹配的威胁列表

## 维护建议

1. **定期更新** - 跟踪新的漏洞类型和攻击模式
2. **添加 CWE** - 确保每个威胁都有对应的 CWE 编号
3. **细化检查点** - 检查点应该具体且可操作
4. **关联 Agent** - 为常见威胁指定专门的检测 Agent
