---
allowed-tools: Bash, Read, Write, Grep, Glob, TodoWrite
description: Analyze Semgrep scan results to classify true positives, suspected risks, and false positives
argument-hint: <semgrep-json-file> <project-path> [--output <path>]
---


# Semgrep 误报分析

> 通用的 Semgrep 扫描结果误报分析工具，区分真实漏洞、疑似风险和误报

## ⚠️ 重要约束

**本命令是独立的分析工具，禁止使用以下工具**：
- ❌ **禁止使用 Skill 工具**：不得触发任何技能（semgrep-execution、semgrep-rule-development 等）
- ❌ **禁止使用 Task 工具**：不得调用任何智能体（semgrep-agent、sast-agent 等）
- ❌ **禁止使用 SlashCommand 工具**：不得调用其他斜杠命令

**原因**：
- 保持命令执行的简单性和可预测性
- 避免触发 Semgrep 执行或规则验证流程
- 确保分析过程独立、轻量、快速
- 防止循环调用或依赖链问题

**允许的工具**：仅限 `Bash`、`Read`、`Write`、`Grep`、`Glob`、`TodoWrite`

---

## 使用方式

在 Claude Code 中通过命令面板或直接引用此命令，AI 将帮助你分析 Semgrep 扫描结果。

**必需参数**：
1. `<semgrep-json-file>`：Semgrep 扫描结果 JSON 文件路径
2. `<project-path>`：项目源码根目录（用于读取源文件获取上下文）

**可选参数**：
- `--output <path>`：指定输出文件路径（默认输出到标准输出）

## 使用示例

### 示例 1：基本误报分析

```bash
# 在 Claude Code 中执行
/semgrep-fp-analysis \
  workspace/target/analyses/scan-001/semgrep-output.json \
  /absolute/path/to/project

# AI 将自动：
# 1. 读取 Semgrep JSON 输出
# 2. 读取项目源码文件获取上下文
# 3. 分析每个 finding 的代码上下文和数据流
# 4. 评估风险级别
# 5. 输出 JSON 格式报告
```

### 示例 2：指定输出路径

```bash
/semgrep-fp-analysis \
  semgrep-output.json \
  /path/to/project \
  --output workspace/fp-analysis.json

# 输出到指定文件
```

### 示例 3：使用相对路径

```bash
# 如果当前在项目根目录
/semgrep-fp-analysis \
  workspace/target/semgrep-output.json \
  . \
  --output workspace/fp-analysis.json
```

### 示例 4：从 workspace 配置读取项目路径

```bash
# 如果 workspace/target/config.json 中有 projectPath
# 先读取配置获取项目路径
cat workspace/target/config.json | jq -r '.projectPath'

# 然后使用该路径
/semgrep-fp-analysis \
  workspace/target/analyses/scan-001/semgrep-output.json \
  /path/from/config \
  --output workspace/target/fp-analysis.json
```

## 功能描述

对 Semgrep 扫描结果进行智能分析，通过代码上下文、数据流分析和常见模式识别，将发现分类为：
- **真实漏洞**（True Positive）
- **疑似风险点**（Suspected Risk）
- **误报**（False Positive）

## 分析维度

### 1. 代码上下文分析

**目标**：理解代码的真实意图和运行环境

**分析要点**：
- 代码所在的函数/类的职责
- 方法的可见性（public/private/internal）
- 调用链路和数据来源
- 框架特定的安全机制（如 ORM 的参数化查询）

**示例判断逻辑**：
```
如果代码在：
- private 内部方法 + 输入来自硬编码常量 → 可能是误报
- public API 端点 + 输入来自用户请求 → 疑似风险
- 测试代码中 → 可能是误报（但需要检查测试代码是否影响生产）
```

### 2. 数据流追踪

**目标**：判断危险数据是否真正可达漏洞点

**分析要点**：
- 输入来源（用户输入 vs 配置文件 vs 硬编码）
- 中间处理（是否经过验证、清理、编码）
- 输出目标（数据库 vs 日志 vs HTTP 响应）
- 控制流（是否有条件判断或异常处理）

**示例判断逻辑**：
```
数据流分析：
用户输入 → 无验证 → 直接拼接 SQL → 执行
  结论：真实漏洞

用户输入 → 白名单验证 → 参数化查询 → 执行
  结论：误报（已有防护）

配置文件 → 直接拼接 SQL → 执行
  结论：疑似风险（配置文件可信度取决于部署环境）
```

### 3. 框架和库的安全机制

**目标**：识别框架自带的安全防护

**常见框架防护机制**：

| 框架/库 | 安全机制 | 判断要点 |
|--------|---------|---------|
| Spring JPA | 参数化查询 | 使用 `@Query` 或 `findBy*` 方法 |
| MyBatis | `#{}` 参数化 | 区分 `#{}` (安全) 和 `${}` (危险) |
| Django ORM | QuerySet API | 使用 `.filter()` 等 API |
| PreparedStatement | 参数绑定 | 使用 `setString()` 等方法 |
| Express.js | 参数化查询 | 使用占位符而非字符串拼接 |

**示例判断逻辑**：
```
发现 SQL 注入规则匹配：
- 使用 PreparedStatement + setString() → 误报
- 使用 MyBatis #{} 占位符 → 误报
- 使用 MyBatis ${} 拼接用户输入 → 真实漏洞
- 使用字符串拼接 + executeQuery() → 疑似风险
```

### 4. 常见误报模式识别

**目标**：识别工具的常见误判场景

**常见误报模式**：

#### 模式 1：测试代码
```java
// Semgrep 可能报告 SQL 注入
@Test
public void testSqlInjection() {
    String maliciousInput = "' OR '1'='1";
    // 这是测试代码，不是真实漏洞
}
```
**判断**：检查文件路径是否包含 `test/`、`spec/`、`__tests__/` 等测试目录

#### 模式 2：日志和调试代码
```python
# Semgrep 可能报告 XSS
logger.debug(f"User input: {user_input}")
# 日志输出通常不直接暴露给用户
```
**判断**：检查是否是日志函数（`log.*`、`print`、`console.*`）

#### 模式 3：内部工具和脚本
```bash
# Semgrep 可能报告命令注入
os.system(f"backup.sh {database_name}")
# 如果 database_name 来自配置文件而非用户输入
```
**判断**：检查文件路径是否包含 `scripts/`、`tools/`、`admin/` 等

#### 模式 4：框架生成的代码
```java
// 框架生成的 DAO 代码
public List<User> findByUsername(String username) {
    return jdbcTemplate.query("SELECT * FROM users WHERE username = ?",
                              new Object[]{username}, userMapper);
}
```
**判断**：使用参数化查询的标准模式

#### 模式 5：常量和枚举
```go
// Semgrep 可能报告 SQL 注入
query := fmt.Sprintf("SELECT * FROM %s WHERE id = ?", TABLE_NAME)
// TABLE_NAME 是常量，不是用户输入
```
**判断**：检查变量名是否为大写（常量约定）或来自枚举

### 5. 严重程度评估

**目标**：对疑似风险点进行优先级排序

**评估因素**：
- 可利用性：是否有明确的攻击路径
- 影响范围：数据泄露、权限提升、系统破坏
- 可达性：代码是否在生产环境执行
- 现有防护：是否有其他安全层（WAF、权限控制）

**风险评级**：
```
critical: 用户输入 + 无防护 + 高影响（如 SQL 注入导致数据泄露）
high: 用户输入 + 部分防护 + 高影响
medium: 间接用户输入 + 无防护 + 中等影响
low: 内部输入 + 低影响
info: 测试代码、日志输出等
```

## 分析流程

```
读取 Semgrep JSON 输出
         │
         ▼
遍历每个 finding
         │
         ▼
提取代码位置信息
         │
         ▼
读取代码上下文
（前后 10-20 行）
         │
         ▼
┌────────┴────────┐
│                 │
▼                 ▼
数据流分析      框架机制识别
│                 │
└────────┬────────┘
         ▼
应用误报模式规则
         │
         ▼
计算风险评分
         │
         ▼
分类和标注
         │
         ├─→ 真实漏洞（confidence >= 0.7）
         ├─→ 疑似风险（0.3 < confidence < 0.7）
         └─→ 误报（confidence <= 0.3）
         │
         ▼
生成 JSON 报告
```

## 输出格式

### JSON 输出结构

```json
{
  "metadata": {
    "analysis_time": "2025-01-15T10:30:00Z",
    "source_file": "semgrep-output.json",
    "total_findings": 150,
    "analyzer_version": "1.0"
  },
  "summary": {
    "true_positives": 25,
    "suspected_risks": 40,
    "false_positives": 85
  },
  "classifications": [
    {
      "finding_id": "semgrep-001",
      "original_finding": {
        "check_id": "java.lang.security.sql-injection",
        "path": "src/main/java/UserController.java",
        "line": 45,
        "message": "Detected SQL injection vulnerability"
      },
      "classification": "true_positive",
      "confidence": 0.95,
      "risk_level": "critical",
      "reasoning": {
        "data_source": "用户输入（HTTP 请求参数）",
        "data_flow": "request.getParameter() → 字符串拼接 → executeQuery()",
        "protection": "无验证、无参数化查询",
        "context": "public API 端点，直接处理用户输入",
        "exploitability": "高（可通过 URL 参数直接利用）"
      },
      "recommendation": "使用 PreparedStatement 和参数绑定替代字符串拼接"
    },
    {
      "finding_id": "semgrep-002",
      "original_finding": {
        "check_id": "java.lang.security.sql-injection",
        "path": "src/main/java/UserDao.java",
        "line": 23,
        "message": "Detected SQL injection vulnerability"
      },
      "classification": "false_positive",
      "confidence": 0.1,
      "risk_level": "info",
      "reasoning": {
        "data_source": "内部常量（TABLE_NAME）",
        "data_flow": "常量 → 字符串拼接 → PreparedStatement",
        "protection": "使用 PreparedStatement 参数化查询",
        "context": "表名来自常量，查询条件使用占位符",
        "exploitability": "无（常量不可控，查询已参数化）"
      },
      "recommendation": "无需修复（已使用参数化查询）"
    },
    {
      "finding_id": "semgrep-003",
      "original_finding": {
        "check_id": "python.django.security.xss",
        "path": "src/views/dashboard.py",
        "line": 67,
        "message": "Unescaped variable in template"
      },
      "classification": "suspected_risk",
      "confidence": 0.5,
      "risk_level": "medium",
      "reasoning": {
        "data_source": "数据库查询结果（可能包含用户输入）",
        "data_flow": "数据库 → 模板渲染（使用 |safe 过滤器）",
        "protection": "部分防护（数据库存储时可能已清理）",
        "context": "Django 模板，使用 |safe 过滤器跳过转义",
        "exploitability": "中等（取决于数据库内容的来源和清理）"
      },
      "recommendation": "审查数据存储时是否进行了 HTML 转义，避免使用 |safe 过滤器，或使用 bleach 库清理 HTML"
    }
  ],
  "statistics": {
    "by_risk_level": {
      "critical": 5,
      "high": 20,
      "medium": 40,
      "low": 35,
      "info": 50
    },
    "by_classification": {
      "true_positive": 25,
      "suspected_risk": 40,
      "false_positive": 85
    },
    "by_vulnerability_type": {
      "sql_injection": {
        "total": 60,
        "true_positive": 10,
        "suspected_risk": 15,
        "false_positive": 35
      },
      "xss": {
        "total": 40,
        "true_positive": 8,
        "suspected_risk": 12,
        "false_positive": 20
      }
    }
  }
}
```

### 字段说明

**classification（分类）**：
- `true_positive`：真实漏洞，需要立即修复
- `suspected_risk`：疑似风险点，需要人工审查
- `false_positive`：误报，无需修复

**confidence（置信度）**：
- 0.0 - 1.0 之间的浮点数
- >= 0.7：高置信度（真实漏洞）
- 0.3 - 0.7：中等置信度（疑似风险）
- <= 0.3：低置信度（误报）

**risk_level（风险级别）**：
- `critical`：严重（需要立即修复）
- `high`：高风险（24小时内修复）
- `medium`：中风险（1周内修复）
- `low`：低风险（计划修复）
- `info`：信息（无需修复）

**reasoning（推理依据）**：
- `data_source`：数据来源（用户输入、配置文件、常量等）
- `data_flow`：数据流路径
- `protection`：现有防护机制
- `context`：代码上下文
- `exploitability`：可利用性评估

## 通用分析方法

### 判断真实漏洞的核心原则

1. **输入可控性**：数据是否来自不可信源（用户输入、外部 API）
2. **处理缺失性**：数据流中是否缺少必要的验证/清理/编码
3. **输出危险性**：数据是否流向危险函数（SQL 执行、命令执行、HTML 输出）
4. **防护缺失性**：是否缺少框架或库提供的安全机制

### 判断误报的核心原则

1. **数据不可控**：数据来自常量、枚举、配置文件（可信环境）
2. **已有防护**：使用了参数化查询、输出编码、白名单验证等
3. **非生产代码**：测试代码、示例代码、注释代码
4. **框架防护**：框架自动提供了安全防护

### 判断疑似风险的核心原则

1. **部分防护**：有防护但可能不完整（如只验证长度，不验证内容）
2. **间接输入**：数据来自间接源（数据库、缓存），但原始数据可能是用户输入
3. **复杂流程**：数据流路径复杂，难以静态分析
4. **配置依赖**：安全性取决于运行时配置或部署环境

## 不依赖特定规则的通用分析

**本命令不针对特定漏洞类型**（如只分析 SQL 注入），而是提供通用的分析框架：

### 通用分析模式

```
对于任何 Semgrep finding：
1. 提取代码位置
2. 读取上下文（函数、类、模块）
3. 识别数据来源（分析变量定义和赋值）
4. 追踪数据流（向前和向后追踪）
5. 检测防护措施（验证、清理、编码函数调用）
6. 评估可利用性（综合判断）
7. 分类并给出置信度
```

### 语言无关的分析要点

无论什么编程语言，都关注：
- **变量来源**：参数、返回值、全局变量
- **函数调用**：危险函数（exec、eval、query、render）vs 安全函数（escape、validate、sanitize）
- **控制流**：条件判断、异常处理
- **代码位置**：文件路径（是否是测试/工具代码）

### 示例：通用 SQL 注入分析

```
步骤 1: 识别 SQL 执行函数
  - Java: executeQuery(), executeUpdate()
  - Python: execute(), executemany()
  - JavaScript: query(), exec()
  - PHP: query(), mysqli_query()

步骤 2: 向后追踪 SQL 字符串的构造
  - 字符串拼接（+ 或 concat）→ 疑似风险
  - 字符串插值（f-string, template literal）→ 疑似风险
  - 参数绑定（?, :param）→ 安全

步骤 3: 向前追踪变量来源
  - 函数参数 → 检查调用者
  - 用户输入（request, $_GET）→ 高风险
  - 常量或配置 → 低风险

步骤 4: 检测验证和清理
  - 白名单验证 → 降低风险
  - 正则匹配 → 检查正则是否严格
  - 转义函数 → 检查是否适用于 SQL

步骤 5: 综合判断
  - 用户输入 + 字符串拼接 + 无验证 → 真实漏洞
  - 用户输入 + 参数绑定 → 误报
  - 配置文件 + 字符串拼接 → 疑似风险
```

## 执行步骤

### 1. 验证输入参数

```bash
# 检查 Semgrep 输出文件是否存在
if [ ! -f "$SEMGREP_JSON" ]; then
    echo "错误：Semgrep 输出文件不存在: $SEMGREP_JSON"
    exit 1
fi

# 检查项目路径是否存在
if [ ! -d "$PROJECT_PATH" ]; then
    echo "错误：项目路径不存在: $PROJECT_PATH"
    exit 1
fi
```

### 2. 解析 Semgrep 输出

使用 `Read` 工具读取 Semgrep JSON 文件：

```bash
# 读取 JSON 文件，获取 findings 总数
cat semgrep-output.json | jq '.results | length'

# 查看 findings 的基本信息
cat semgrep-output.json | jq '.results[] | {path, line: .start.line, rule: .check_id}'
```

### 3. 读取源代码上下文

对于每个 finding，使用 `Read` 工具读取源文件：

```bash
# Semgrep JSON 中的路径是相对路径
RELATIVE_PATH="src/main/java/UserController.java"
LINE_NUMBER=45

# 构造绝对路径
ABSOLUTE_PATH="$PROJECT_PATH/$RELATIVE_PATH"

# 使用 Read 工具读取代码上下文（前后 10 行）
# Claude 会使用 Read 工具读取该文件
```

**注意**：
- Semgrep JSON 中的 `path` 字段是相对于项目根目录的相对路径
- 需要将相对路径与 `project-path` 参数拼接得到绝对路径
- 使用 `Read` 工具读取文件时可以指定 offset 和 limit 参数获取上下文

### 4. 分析每个 finding

对于每个 finding，执行以下分析：

```
1. 提取基本信息：
   - 文件路径：result['path']
   - 行号：result['start']['line']
   - 规则ID：result['check_id']
   - 规则消息：result['extra']['message']

2. 读取代码上下文：
   - 使用 Read 工具读取目标文件
   - 获取前后 10-20 行代码
   - 识别所在的函数、类、模块

3. 数据流分析：
   - 向后追踪变量定义和赋值
   - 向前追踪变量使用和输出
   - 识别危险函数调用
   - 识别安全防护函数

4. 应用通用分析规则：
   - 检查是否是测试代码（路径包含 test/）
   - 检查是否使用参数化查询
   - 检查是否有输入验证
   - 检查数据来源（用户输入 vs 常量）

5. 计算置信度和风险级别：
   - 综合评估各项因素
   - 给出 0-1 之间的置信度分数
   - 确定风险级别（critical/high/medium/low/info）

6. 生成分类和推理依据：
   - classification: true_positive / suspected_risk / false_positive
   - reasoning: 详细的判断依据
   - recommendation: 修复建议
```

### 5. 生成 JSON 报告

```bash
# 构造输出 JSON 结构
{
  "metadata": {
    "analysis_time": "2025-01-15T10:30:00Z",
    "source_file": "semgrep-output.json",
    "project_path": "/absolute/path/to/project",
    "total_findings": 150
  },
  "summary": {...},
  "classifications": [...],
  "statistics": {...}
}

# 如果指定了 --output 参数，写入文件
# 否则输出到标准输出
```

### 6. 输出统计摘要

```bash
# 使用 Bash + jq 输出统计信息
echo "=== 误报分析统计 ==="
echo "总发现数: $(jq '.metadata.total_findings' fp-analysis.json)"
echo "真实漏洞: $(jq '.summary.true_positives' fp-analysis.json)"
echo "疑似风险: $(jq '.summary.suspected_risks' fp-analysis.json)"
echo "误报: $(jq '.summary.false_positives' fp-analysis.json)"
```

## 注意事项

1. **自动化分析的局限性**：本分析基于静态代码分析，无法覆盖所有运行时行为
2. **人工审查建议**：对于 `suspected_risk` 分类，建议进行人工审查
3. **上下文依赖**：分析结果依赖于代码上下文的完整性，对于复杂数据流可能需要更深入分析
4. **框架更新**：随着框架和库的更新，安全机制可能变化，需要定期更新分析规则
5. **配置文件可信度**：配置文件的安全性取决于部署环境和访问控制

## 后续行动

分析完成后，建议按以下优先级处理：

1. **立即修复**：`true_positive` + `critical/high` 风险级别
2. **计划修复**：`true_positive` + `medium/low` 风险级别
3. **人工审查**：`suspected_risk` 分类的所有发现
4. **记录备案**：`false_positive` 分类，用于改进规则

## 相关命令

- `/security-audit`：全面安全审计
- `/code-review`：代码审查
- `/debug`：调试分析

## 技术实现提示

当执行此命令时，必须遵循以下规范：

### 工具使用规范

**允许使用的工具**（仅限以下工具）：
1. **Read 工具**：读取 Semgrep JSON 输出和源代码文件
   ```
   # 读取 Semgrep 输出
   Read: semgrep-output.json

   # 读取源代码（拼接项目路径 + 相对路径）
   Read: {project_path}/{relative_path}

   # 读取上下文（指定行号范围）
   Read: {project_path}/{relative_path}, offset={line-10}, limit=20
   ```

2. **Grep 工具**：搜索相关的函数定义和调用
   ```
   # 在项目中搜索函数定义
   Grep: pattern="def function_name", path={project_path}

   # 搜索安全防护函数
   Grep: pattern="PreparedStatement|setString", path={project_path}
   ```

3. **Glob 工具**：查找测试文件和配置文件
   ```
   # 查找测试文件
   Glob: **/test/**/*.java, path={project_path}

   # 查找配置文件
   Glob: **/*.properties, path={project_path}
   ```

4. **Bash 工具**：执行简单的文件检查和 JSON 处理
   ```bash
   # 检查文件是否存在
   [ -f "$FILE_PATH" ] && echo "exists"

   # 使用 jq 解析 JSON
   cat semgrep-output.json | jq '.results | length'
   ```

5. **Write 工具**：输出分析报告（仅当指定 --output 参数时）
   ```
   # 写入 JSON 报告
   Write: {output_path}, content={json_report}
   ```

6. **TodoWrite 工具**：跟踪分析进度（如果发现数量很多，> 50 个）
   ```
   # 创建待办事项跟踪分析进度
   TodoWrite: [
     "解析 Semgrep 输出",
     "分析前 50 个 findings",
     "分析剩余 findings",
     "生成统计报告",
     "输出 JSON 文件"
   ]
   ```

**严格禁止使用的工具**：
- ❌ **Skill 工具**：不得触发任何技能
- ❌ **Task 工具**：不得调用任何智能体
- ❌ **SlashCommand 工具**：不得调用其他命令
- ❌ **NotebookEdit 工具**：不相关
- ❌ **WebFetch/WebSearch 工具**：不需要网络访问

### 路径处理规范

**关键点**：Semgrep JSON 中的路径是**相对路径**，需要与 `project-path` 参数拼接：

```bash
# Semgrep JSON 示例
{
  "results": [{
    "path": "src/main/java/UserController.java",  # 相对路径
    "start": {"line": 45, "col": 12}
  }]
}

# 处理方式
PROJECT_PATH="/absolute/path/to/project"  # 用户提供的参数
RELATIVE_PATH="src/main/java/UserController.java"  # 从 JSON 读取
ABSOLUTE_PATH="$PROJECT_PATH/$RELATIVE_PATH"  # 拼接得到绝对路径

# 使用 Read 工具读取
Read: $ABSOLUTE_PATH
```

### 错误处理规范

```bash
# 1. 检查必需参数
if [ -z "$SEMGREP_JSON" ] || [ -z "$PROJECT_PATH" ]; then
    echo "错误：缺少必需参数"
    echo "使用方式: /semgrep-fp-analysis <semgrep-json> <project-path> [--output <path>]"
    exit 1
fi

# 2. 验证文件和路径存在
if [ ! -f "$SEMGREP_JSON" ]; then
    echo "错误：Semgrep 输出文件不存在: $SEMGREP_JSON"
    exit 1
fi

if [ ! -d "$PROJECT_PATH" ]; then
    echo "错误：项目路径不存在或不是目录: $PROJECT_PATH"
    exit 1
fi

# 3. 验证 JSON 格式
if ! jq empty "$SEMGREP_JSON" 2>/dev/null; then
    echo "错误：无效的 JSON 格式: $SEMGREP_JSON"
    exit 1
fi

# 4. 检查是否有结果
RESULT_COUNT=$(jq '.results | length' "$SEMGREP_JSON")
if [ "$RESULT_COUNT" -eq 0 ]; then
    echo "提示：Semgrep 输出中没有发现任何问题"
    exit 0
fi
```

### 分析流程伪代码

```
1. 验证输入参数
   ├─ 检查 semgrep-json-file 存在且为有效 JSON
   ├─ 检查 project-path 存在且为目录
   └─ 解析可选的 --output 参数

2. 初始化统计变量
   ├─ total_findings = 0
   ├─ true_positives = 0
   ├─ suspected_risks = 0
   └─ false_positives = 0

3. 使用 Read 工具读取 Semgrep JSON
   ├─ 解析 JSON 结构
   └─ 提取 results 数组

4. 如果 findings 数量 > 50，使用 TodoWrite 创建进度跟踪

5. 遍历每个 result：
   FOR EACH result IN results:
     ├─ 提取基本信息（path, line, check_id, message）
     ├─ 构造绝对路径：absolute_path = project_path + "/" + relative_path
     ├─ 使用 Read 工具读取源文件上下文
     ├─ 分析代码上下文（函数、类、数据流）
     ├─ 应用通用分析规则
     ├─ 计算置信度和风险级别
     ├─ 生成分类（true_positive/suspected_risk/false_positive）
     ├─ 生成推理依据（reasoning）
     ├─ 更新统计计数
     └─ 如果使用了 TodoWrite，更新进度

6. 构造输出 JSON 结构
   ├─ metadata（分析时间、源文件、项目路径）
   ├─ summary（统计摘要）
   ├─ classifications（详细分类结果）
   └─ statistics（按风险级别和漏洞类型的统计）

7. 输出结果
   ├─ 如果指定了 --output，使用 Write 工具写入文件
   ├─ 否则直接输出到标准输出（用户可见）
   └─ 输出统计摘要（控制台输出）

8. 清理并退出
```

------

**最后更新**：2025-01-15
**维护者**：Security Team
**版本**：1.0.0
