# Agents

Specialized agents for complex, multi-step tasks.

---

## What Are Agents?

Agents are autonomous Claude instances that handle specific complex tasks. Unlike skills (which provide inline guidance), agents:
- Run as separate sub-tasks
- Work autonomously with minimal supervision
- Have specialized tool access
- Return comprehensive reports when complete

**Key advantage:** Agents are **standalone** - just copy the `.md` file and use immediately!

---

## Available Agents

### VIA System 专用代理（10个）

这些代理是 VIA System（Vulnerability Intelligence Automation System）的核心组件，用于自动化安全分析流程。

### security-orchestrator
**Purpose:** 安全分析总控智能体（Orchestrator / Project Manager）- 整个系统的"总控大脑"

**When to use:**
- 对目标项目进行完整的安全分析
- 需要多阶段、多 Agent 协作的安全任务
- 自动化安全审计流程

**Workflow:**
```
工程理解 → 威胁建模 → 漏洞挖掘 → 验证 → 报告
```

**Core Features:**
- 任务编排和调度
- 黑板（Blackboard）上下文管理
- 多 Agent 协作协调
- 进度追踪和错误处理

**Output:**
- `workspace/{target-name}/` 目录包含所有分析结果
- `workspace/{target-name}/analyses/{analysis-id}/reports/` - 最终安全报告
- `workspace/{target-name}/analyses/{analysis-id}/blackboard.json` - 任务状态和上下文

**Integration:** ✅ Copy as-is

---

### engineering-profiler
**Purpose:** 构建工程画像（Engineering Profile）和代码可读化文档（Deep Code Wiki）

**When to use:**
- 快速理解新代码库的整体架构
- 生成项目技术文档和资产清单
- 识别项目中的功能入口点（API endpoints）
- 为安全审计或代码审查准备基础画像

**Output:**
- `engineering-profile.md` - 工程画像文档
- `engineering-profile.json` - 结构化数据
- `deep-code-wiki.md` - 代码可读化文档

**Integration:** ✅ Copy as-is

---

### threat-modeler
**Purpose:** 威胁建模 - 构建攻击图谱（Threat Graph），基于工程画像进行攻击面推理

**When to use:**
- 基于工程画像进行威胁推理
- 构建功能点→威胁的映射关系
- 生成威胁任务列表供后续漏洞挖掘使用
- 决定"挖哪里"、"先挖什么"

**Input:**
- `engineering-profile.json`（工程画像）

**Output:**
- `threat-model.md` - 威胁模型文档
- `threat-model.json` - 结构化威胁数据
- `threat-task-list.json` - 威胁任务清单

**Note:** 只做威胁推理，不做代码分析和漏洞验证

**Integration:** ✅ Copy as-is

---

### task-planner
**Purpose:** 漏洞挖掘任务规划器 - 将威胁建模输出转化为可执行的 Skill 调用计划（Task Graph）

**When to use:**
- 接收威胁建模输出，生成任务执行计划
- 为每个威胁场景匹配最佳 Skill-Agent 组合
- 协调多模型并行执行（Claude/GPT/DeepSeek/Qwen/Llama）
- 管理任务依赖和执行顺序

**Input:**
- `threat-task-list.json`（威胁任务清单）

**Output:**
- `task-graph.json` - 任务执行图（含依赖关系）
- `execution-plan.md` - 执行计划文档
- `model-assignment.json` - 模型分配方案

**Core Functions:**
- **Task Decomposition** - 将威胁拆解为子任务链（static → dataflow → fuzz → poc → verify → triage）
- **Skill Matching** - 匹配最佳 Skill Agent
- **Model Routing** - 智能选择模型（Claude 深度推理 / DeepSeek 批量扫描 / GPT 代码生成）
- **Parallel Scheduling** - 无依赖任务并行执行

**Note:** 只做任务规划和调度，不做实际漏洞检测

**Integration:** ✅ Copy as-is

---

### validation-agent
**Purpose:** 漏洞验证智能体 - 将 Finding 转化为 Verified Vulnerability（带完整证据链）

**When to use:**
- 验证 Vuln Skills 产出的 Finding
- 需要深度推理判断漏洞可利用性
- 生成 PoC 并执行验证
- 构建完整的漏洞证据链

**Core Modules:**
- **Triage Manager** - 聚类、去重、合并、分级
- **Deep Think** - LLM 深度推理 + 反例验证
- **Static Verify** - 代码模式再确认
- **Reachability Verify** - 数据流可达性分析
- **PoC Lab** - PoC 构造与沙箱执行
- **Browser Verify** - XSS/前端漏洞验证
- **Evidence Builder** - 证据链构建

**Output:**
- `verified-vulnerabilities.json` - 已验证漏洞列表
- `evidence-chains/` - 完整证据链目录
- `triage-report.md` - 分诊报告

**Core Principle:** Evidence Chain 是一等公民

**Integration:** ✅ Copy as-is

---

### security-reporter
**Purpose:** 安全报告生成智能体 - 生成项目整体安全报告和漏洞详细报告

**When to use:**
- 安全审计完成后生成报告
- 漏洞验证后生成详细报告
- CI/CD 流水线安全检查输出
- 向管理层/客户交付安全评估报告

**Output:**
- `security-report.md` - 项目整体安全报告
- `vulnerabilities/` - 漏洞详细报告目录
- `report.json` / `report.yaml` - 结构化数据（CI/CD 集成）

**Integration:** ✅ Copy as-is

---

## Skill-Agents (Execution Layer) - 漏洞挖掘执行层

漏洞挖掘智能体执行层 - 提供 **API 级别的精准漏洞检测能力**（非全局扫描器）

### 核心特性

1. **精准级检测** - 针对指定点、指定领域做深度分析
2. **结构化输出** - 所有 Finding 带静态证据（路径/代码片段）
3. **多技能协作** - 漏洞类型 + 工具类型 + 语言生态

### 输出格式

所有 Skill-Agent 输出统一的结构化 Finding：

```json
{
  "finding": "SQL Injection",
  "target": "/api/login",
  "location": "LoginController.java:23",
  "path": ["param user", "dao.query()", "string concat"],
  "evidence": ["no sanitization", "user-provided input"],
  "confidence": 0.83
}
```

---

### 漏洞类型 Agents

#### sqli-agent
**Purpose:** SQL 注入检测智能体 - 精准级 SQL 注入漏洞检测器

**When to use:**
- 分析指定函数/端点是否存在 SQL 注入
- 追踪用户输入到 SQL 执行的污点路径
- 检测 SQL 拼接、格式化字符串等危险模式

**Supported Languages:** Java/Python/PHP/Node.js/Go

**Integration:** ✅ Copy as-is

---

#### xss-agent
**Purpose:** XSS 检测智能体 - 精准级跨站脚本漏洞检测器

**When to use:**
- 检测反射型、存储型、DOM-based XSS
- 追踪用户输入到输出点的数据流
- 识别不安全的输出编码和 DOM 操作
- 支持 Headless 浏览器动态验证

**Integration:** ✅ Copy as-is

---

#### ssrf-agent
**Purpose:** SSRF 检测智能体 - 精准级服务端请求伪造漏洞检测器

**When to use:**
- 识别用户可控的 URL/Host 参数
- 检测不安全的 HTTP 请求函数调用
- 分析 URL 校验逻辑的绕过可能
- 构造绕过 payload（IP 编码、DNS 重绑定等）

**Integration:** ✅ Copy as-is

---

#### rce-agent
**Purpose:** RCE 检测智能体 - 精准级远程代码/命令执行漏洞检测器

**When to use:**
- 检测命令注入（Command Injection）
- 检测代码注入（Code Injection / eval）
- 检测反序列化漏洞（Deserialization）
- 检测模板注入（SSTI → RCE）
- 检测表达式注入（SpEL, OGNL, EL）

**Integration:** ✅ Copy as-is

---

#### file-upload-agent
**Purpose:** 文件上传漏洞检测智能体 - 精准级文件上传安全检测器

**When to use:**
- 检测文件类型验证绕过漏洞
- 识别文件内容验证缺陷
- 分析文件存储路径的安全性
- 检测恶意文件上传风险（WebShell、可执行文件）

**Supported Languages:** Java/Python/PHP/Node.js/Go

**Integration:** ✅ Copy as-is

---

#### path-traversal-agent
**Purpose:** 路径遍历漏洞检测智能体 - 精准级目录遍历安全检测器

**When to use:**
- 检测目录遍历/路径穿越漏洞
- 识别用户可控的文件路径参数
- 分析路径规范化和过滤机制
- 检测 Zip Slip 等高级攻击

**Supported Languages:** Java/Python/PHP/Node.js/Go

**Integration:** ✅ Copy as-is

---

#### idor-agent
**Purpose:** IDOR 漏洞检测智能体 - 精准级不安全直接对象引用检测器

**When to use:**
- 识别用户可控的资源标识符（ID、UUID、文件名等）
- 检测缺失或不完整的访问控制检查
- 分析水平/垂直权限绕过风险
- 评估批量数据泄露可能性

**Supported Languages:** Java/Python/PHP/Node.js/Go

**Integration:** ✅ Copy as-is

---

#### xxe-agent
**Purpose:** XXE 漏洞检测智能体 - 精准级 XML 外部实体注入检测器

**When to use:**
- 识别 XML 解析入口点和解析器配置
- 检测不安全的 XML 解析器配置
- 分析外部实体和 DTD 处理风险
- 检测 XXE → SSRF、XXE → 文件读取等利用链

**Supported Languages:** Java/Python/PHP/Node.js/Go/.NET

**Integration:** ✅ Copy as-is

---

#### auth-bypass-agent
**Purpose:** 认证绕过漏洞检测智能体 - 精准级认证授权安全检测器

**When to use:**
- 识别认证机制漏洞（JWT、Session、OAuth、API Key）
- 检测授权逻辑绕过点
- 分析密码重置和账户恢复流程缺陷
- 检测 2FA 绕过风险

**Supported Languages:** Java/Python/PHP/Node.js/Go

**Integration:** ✅ Copy as-is

---

### 工具类型 Agents

#### sast-agent
**Purpose:** 静态应用安全测试执行器 - 规则 + AST + LLM 三层检测

**When to use:**
- 对指定代码范围运行 Semgrep/ast-grep/CodeQL
- AST/DFG 数据流分析
- LLM 语义分析和误报过滤
- 增量分析（只分析变更代码）

**Detection Categories:** Injection, Crypto, Auth, Secrets, Config

**Integration:** ✅ Copy as-is

---

#### fuzz-agent
**Purpose:** 模糊测试执行器 - 针对特定参数的定向 Fuzz

**When to use:**
- 对指定参数/端点进行智能变异
- 边界值和异常值测试
- 协议感知的 Payload 构造
- 反馈驱动的变异策略

**Integration:** ✅ Copy as-is

---

#### sca-agent
**Purpose:** 软件组成分析执行器 - 依赖安全分析

**When to use:**
- 依赖漏洞检测（CVE/NVD 匹配）
- 许可证合规分析
- 依赖版本过时检测
- 可达性分析（漏洞是否被实际调用）
- 供应链风险评估

**Supported:** npm/pip/maven/go/composer

**Integration:** ✅ Copy as-is

---

### 语言生态 Agents

#### lang-java-agent
**Purpose:** Java 语言生态安全专家 - Java/Spring/Servlet

**Specialties:**
- Spring SpEL 注入
- Java 反序列化分析
- MyBatis/Hibernate ORM 安全
- Servlet/JSP 漏洞

**Integration:** ✅ Copy as-is

---

#### lang-python-agent
**Purpose:** Python 语言生态安全专家 - Python/Django/Flask/FastAPI

**Specialties:**
- Jinja2/Mako SSTI
- pickle 反序列化
- Django ORM/SQLAlchemy 安全
- Flask/FastAPI 配置

**Integration:** ✅ Copy as-is

---

#### lang-node-agent
**Purpose:** Node.js 语言生态安全专家 - Node.js/Express/Koa

**Specialties:**
- 原型污染 (Prototype Pollution)
- EJS/Pug 模板安全
- npm 依赖安全
- Node.js 特有问题

**Integration:** ✅ Copy as-is

---

#### lang-php-agent
**Purpose:** PHP 语言生态安全专家 - PHP/Laravel/WordPress

**Specialties:**
- PHP 反序列化
- Laravel Eloquent/Blade 安全
- 传统 PHP 安全问题
- WordPress 插件安全

**Integration:** ✅ Copy as-is

---

#### lang-go-agent
**Purpose:** Go 语言生态安全专家 - Go/Gin/Echo

**Specialties:**
- Go 模板安全
- 并发安全问题
- HTTP 客户端安全
- 内存安全

**Integration:** ✅ Copy as-is

---

## Skill-Agents 状态一览

### 漏洞类型 Agents
| Agent | 状态 | 功能 |
|-------|------|------|
| sqli-agent | ✅ | SQL 注入检测 |
| xss-agent | ✅ | 跨站脚本检测 |
| ssrf-agent | ✅ | 服务端请求伪造检测 |
| rce-agent | ✅ | 远程代码执行检测 |
| file-upload-agent | ✅ | 文件上传漏洞检测 |
| path-traversal-agent | ✅ | 路径遍历漏洞检测 |
| idor-agent | ✅ | 不安全直接对象引用检测 |
| xxe-agent | ✅ | XML 外部实体注入检测 |
| auth-bypass-agent | ✅ | 认证绕过检测 |

### 工具类型 Agents
| Agent | 状态 | 功能 |
|-------|------|------|
| sast-agent | ✅ | 静态安全测试 |
| fuzz-agent | ✅ | 模糊测试 |
| sca-agent | ✅ | 依赖安全分析 |

### 语言生态 Agents
| Agent | 状态 | 功能 |
|-------|------|------|
| lang-java-agent | ✅ | Java/Spring 安全 |
| lang-python-agent | ✅ | Python/Django/Flask 安全 |
| lang-node-agent | ✅ | Node.js/Express 安全 |
| lang-php-agent | ✅ | PHP/Laravel 安全 |
| lang-go-agent | ✅ | Go/Gin 安全 |

---

## How to Integrate an Agent

### Standard Integration (Most Agents)

**Step 1: Copy the file**
```bash
cp showcase/.claude/agents/agent-name.md \\
   your-project/.claude/agents/
```

**Step 2: Verify (optional)**
```bash
# Check for hardcoded paths
grep -n "~/git/\|/root/git/\|/Users/" your-project/.claude/agents/agent-name.md
```

**Step 3: Use it**
Ask Claude: "Use the [agent-name] agent to [task]"

That's it! Agents work immediately.

---

### Agents Requiring Customization

**frontend-error-fixer:**
- May reference screenshot paths
- Ask user: "Where should screenshots be saved?"
- Update paths in agent file

**auth-route-tester / auth-route-debugger:**
- Require JWT cookie authentication
- Update service URLs from examples
- Customize for user's auth setup

**auto-error-resolver:**
- May have hardcoded project paths
- Update to use `$CLAUDE_PROJECT_DIR` or relative paths

---

## When to Use Agents vs Skills

| Use Agents When... | Use Skills When... |
|-------------------|-------------------|
| Task requires multiple steps | Need inline guidance |
| Complex analysis needed | Checking best practices |
| Autonomous work preferred | Want to maintain control |
| Task has clear end goal | Ongoing development work |
| Example: "Review all controllers" | Example: "Creating a new route" |

**Both can work together:**
- Skill provides patterns during development
- Agent reviews the result when complete

---

## Agent Quick Reference

| Agent | Complexity | Customization | Auth Required |
|-------|-----------|---------------|---------------|
| security-orchestrator | High | ✅ None | No |
| engineering-profiler | Medium | ✅ None | No |
| threat-modeler | Medium | ✅ None | No |
| task-planner | Medium | ✅ None | No |
| validation-agent | High | ✅ None | No |
| security-reporter | Medium | ✅ None | No |
| sqli-agent | Medium | ✅ None | No |
| xss-agent | Medium | ✅ None | No |
| ssrf-agent | Medium | ✅ None | No |
| rce-agent | High | ✅ None | No |

---

## For Claude Code

**When integrating agents for a user:**

1. **Read [CLAUDE_INTEGRATION_GUIDE.md](../../CLAUDE_INTEGRATION_GUIDE.md)**
2. **Just copy the .md file** - agents are standalone
3. **Check for hardcoded paths:**
   ```bash
   grep "~/git/\|/root/" agent-name.md
   ```
4. **Update paths if found** to `$CLAUDE_PROJECT_DIR` or `.`
5. **For auth agents:** Ask if they use JWT cookie auth first

**That's it!** Agents are the easiest components to integrate.

---

## Creating Your Own Agents

Agents are markdown files with optional YAML frontmatter:

```markdown
# Agent Name

## Purpose
What this agent does

## Instructions
Step-by-step instructions for autonomous execution

## Tools Available
List of tools this agent can use

## Expected Output
What format to return results in
```

**Tips:**
- Be very specific in instructions
- Break complex tasks into numbered steps
- Specify exactly what to return
- Include examples of good output
- List available tools explicitly

---

## Troubleshooting

### Agent not found

**Check:**
```bash
# Is agent file present?
ls -la .claude/agents/[agent-name].md
```

### Agent fails with path errors

**Check for hardcoded paths:**
```bash
grep "~/\|/root/\|/Users/" .claude/agents/[agent-name].md
```

**Fix:**
```bash
sed -i 's|~/git/.*project|$CLAUDE_PROJECT_DIR|g' .claude/agents/[agent-name].md
```

---

## Next Steps

1. **Browse agents above** - Find ones useful for your work
2. **Copy what you need** - Just the .md file
3. **Ask Claude to use them** - "Use [agent] to [task]"
4. **Create your own** - Follow the pattern for your specific needs

**Questions?** See [CLAUDE_INTEGRATION_GUIDE.md](../../CLAUDE_INTEGRATION_GUIDE.md)

---

## 相关文档

- [Agent 依赖关系图](./AGENT_DEPENDENCIES.md) - 详细的 Agent 协作流程和依赖关系
- [合理性分析报告](./ANALYSIS.md) - Agents 目录的设计分析和改进建议
