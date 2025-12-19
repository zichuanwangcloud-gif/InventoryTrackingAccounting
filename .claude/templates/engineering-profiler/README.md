# Engineering Profiler Templates

这些模板由 `engineering-profiler` agent 使用，用于生成结构化的工程文档。

## 模板文件

| 文件 | 用途 | 输出格式 |
|-----|------|---------|
| `engineering-profile.template.md` | 工程画像模板 | Markdown |
| `engineering-profile.template.json` | 结构化数据模板 | JSON |
| `deep-code-wiki.template.md` | 代码 Wiki 模板 | Markdown |

## 模板语法

模板使用 Handlebars 风格的占位符：

- `{{VARIABLE}}` - 简单变量替换
- `{{#each ARRAY}}...{{/each}}` - 数组迭代
- `{{#if CONDITION}}...{{/if}}` - 条件渲染

## 输出说明

### engineering-profile.md

工程画像文档，包含：
- 项目概述和技术栈
- 目录结构说明
- 配置文件清单
- API 端点列表（按功能分类）
- 资产清单（密钥位置、数据库访问点、敏感字段）
- 统计信息

### engineering-profile.json

结构化的工程数据，便于：
- 程序化处理
- 与其他工具集成
- 安全扫描工具消费
- CI/CD 流程集成

### deep-code-wiki.md

代码可读化文档，包含：
- 快速开始指南
- 架构概览
- 模块详解
- 数据模型
- API 参考
- 配置指南
- 开发指南
- 故障排查

## 自定义模板

可以根据项目需求修改这些模板：

1. 添加项目特定的章节
2. 调整表格列
3. 修改 Mermaid 图表样式
4. 添加团队特定的约定

## 注意事项

- 模板中的占位符由 agent 在运行时替换
- JSON 模板必须保持有效的 JSON 结构
- Mermaid 图表需要渲染支持（GitHub、GitLab 等）
