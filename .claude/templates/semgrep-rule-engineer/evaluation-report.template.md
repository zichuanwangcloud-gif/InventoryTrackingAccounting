# Semgrep 规则评估报告

> **漏洞类型**: {vuln_type}
> **目标语言**: {language}
> **生成时间**: {timestamp}
> **生成者**: semgrep-rule-engineer

---

## 1. 规则概述

### 1.1 基本信息

| 属性 | 值 |
|------|-----|
| 漏洞类型 | {vuln_type} ({cwe_id}) |
| 目标语言 | {language} |
| 规则数量 | {rule_count} |
| 场景覆盖 | {scenario_count} 个场景 |
| 知识库来源 | {knowledge_source} |

### 1.2 规则清单

| 规则 ID | 文件名 | 描述 | 严重级别 |
|---------|--------|------|----------|
| {rule_id_1} | {file_name_1} | {description_1} | {severity_1} |
| {rule_id_2} | {file_name_2} | {description_2} | {severity_2} |

### 1.3 规则设计理念

{design_philosophy}

---

## 2. 场景覆盖分析

### 2.1 覆盖的场景

| 场景 ID | 描述 | Source | Sink | 框架/库 |
|---------|------|--------|------|---------|
| {scenario_1} | {desc_1} | {source_1} | {sink_1} | {framework_1} |
| {scenario_2} | {desc_2} | {source_2} | {sink_2} | {framework_2} |

### 2.2 场景来源分析

```
知识库场景: {kb_scenario_count} 个
├── 已覆盖: {kb_covered} 个
└── 未覆盖: {kb_uncovered} 个

模型补充场景: {model_scenario_count} 个
├── 新增框架: {new_frameworks}
└── 新增模式: {new_patterns}
```

### 2.3 未覆盖的已知场景

| 场景 | 原因 | 建议 |
|------|------|------|
| {uncovered_1} | {reason_1} | {suggestion_1} |

---

## 3. 测试结果

### 3.1 测试汇总

| 指标 | 结果 |
|------|------|
| **总体状态** | {overall_status} |
| 测试时间 | {test_time} |
| 正例测试 | {positive_passed}/{positive_total} 通过 ({positive_rate}%) |
| 负例测试 | {negative_passed}/{negative_total} 通过 ({negative_rate}%) |
| **检出率** | {detection_rate}% |
| **误报率** | {false_positive_rate}% |

### 3.2 正例测试详情

正例测试验证规则能够检出已知的漏洞代码模式。

| 测试文件 | 漏洞场景 | 期望 | 实际 | 状态 |
|----------|----------|------|------|------|
| {positive_file_1} | {scenario_1} | 检出 | {actual_1} | {status_1} |
| {positive_file_2} | {scenario_2} | 检出 | {actual_2} | {status_2} |

### 3.3 负例测试详情

负例测试验证规则不会对安全代码产生误报。

| 测试文件 | 安全模式 | 期望 | 实际 | 状态 |
|----------|----------|------|------|------|
| {negative_file_1} | {safe_pattern_1} | 无告警 | {actual_1} | {status_1} |
| {negative_file_2} | {safe_pattern_2} | 无告警 | {actual_2} | {status_2} |

### 3.4 测试失败分析

{test_failure_analysis}

---

## 4. 误报分析

### 4.1 误报分析汇总

| 误报类型 | 预估发生率 | 风险等级 | 主要原因 |
|----------|------------|----------|----------|
| 语义误报 | {semantic_fp_rate}% | {semantic_risk} | {semantic_cause} |
| 上下文误报 | {context_fp_rate}% | {context_risk} | {context_cause} |
| 框架误报 | {framework_fp_rate}% | {framework_risk} | {framework_cause} |
| 业务误报 | {business_fp_rate}% | {business_risk} | {business_cause} |
| **综合误报率** | **{total_fp_rate}%** | | |

### 4.2 语义误报（Semantic False Positive）

代码结构匹配但实际安全的情况。

#### 场景描述

{semantic_fp_description}

#### 典型示例

```{language}
{semantic_fp_example}
```

#### 缓解措施

{semantic_fp_mitigation}

### 4.3 上下文误报（Context False Positive）

存在上游/下游安全处理但规则未识别的情况。

#### 场景描述

{context_fp_description}

#### 典型示例

```{language}
{context_fp_example}
```

#### 缓解措施

{context_fp_mitigation}

### 4.4 框架误报（Framework False Positive）

框架自动提供安全保护的情况。

#### 场景描述

{framework_fp_description}

#### 已知安全框架/API

| 框架/库 | 安全 API | 保护机制 |
|---------|----------|----------|
| {fw_1} | {api_1} | {mechanism_1} |
| {fw_2} | {api_2} | {mechanism_2} |

#### 典型示例

```{language}
{framework_fp_example}
```

#### 缓解措施

{framework_fp_mitigation}

### 4.5 业务逻辑误报（Business Logic False Positive）

业务上下文决定输入可信的情况。

#### 场景描述

{business_fp_description}

#### 典型场景

- 内部管理系统（仅授权用户访问）
- 批处理脚本（输入来自受控数据源）
- 测试代码（非生产环境）
- {additional_business_scenarios}

#### 建议处理方式

{business_fp_mitigation}

### 4.6 可接受的误报

以下场景的误报是可接受的（宁可误报不可漏报）：

1. **{acceptable_fp_1}**
   - 原因: {reason_1}
   - 处理建议: 使用 `// nosemgrep` 或 `@semgrep-ignore` 注释

2. **{acceptable_fp_2}**
   - 原因: {reason_2}
   - 处理建议: {suggestion_2}

---

## 5. 使用建议

### 5.1 推荐使用场景

- {recommended_scenario_1}
- {recommended_scenario_2}
- {recommended_scenario_3}

### 5.2 不推荐使用场景

- {not_recommended_1}
- {not_recommended_2}

### 5.3 配置建议

```yaml
# 推荐的 Semgrep 配置
rules:
  - id: {rule_id}
    # 建议排除的目录
    paths:
      exclude:
        - "**/test/**"
        - "**/tests/**"
        - "**/mock/**"
        - {additional_excludes}
```

### 5.4 误报处理指南

当遇到误报时，推荐以下处理方式：

1. **行级抑制**
   ```{language}
   {line_suppression_example}
   ```

2. **文件级抑制**
   ```{language}
   {file_suppression_example}
   ```

3. **目录级排除**
   在 `.semgrepignore` 文件中添加:
   ```
   {directory_ignore_example}
   ```

---

## 6. 改进方向

### 6.1 短期改进（高优先级）

| 改进项 | 描述 | 预期效果 |
|--------|------|----------|
| {improvement_1} | {desc_1} | {effect_1} |
| {improvement_2} | {desc_2} | {effect_2} |

### 6.2 中期改进（中优先级）

| 改进项 | 描述 | 预期效果 |
|--------|------|----------|
| {improvement_3} | {desc_3} | {effect_3} |
| {improvement_4} | {desc_4} | {effect_4} |

### 6.3 长期改进（低优先级）

| 改进项 | 描述 | 预期效果 |
|--------|------|----------|
| {improvement_5} | {desc_5} | {effect_5} |

### 6.4 知识库更新建议

如果本次生成发现了有价值的新场景或模式，建议更新知识库：

```markdown
# 建议添加到 knowledge/{vuln_type}/{language}.md

## 新增场景

{new_scenario_content}

## 新增 Sanitizer

{new_sanitizer_content}
```

---

## 7. 附录

### 7.1 规则文件列表

```
rules/semgrep/prefilter/{vuln_type}/{language}/
├── {rule_file_1}
├── {rule_file_2}
├── tests/
│   ├── positive/
│   │   ├── {positive_test_1}
│   │   └── {positive_test_2}
│   └── negative/
│       ├── {negative_test_1}
│       └── {negative_test_2}
└── evaluation-report.md
```

### 7.2 测试命令

```bash
# 运行完整测试
python3 /opt/Vul-AI/scripts/test_semgrep_rule.py \
  --rule-dir rules/semgrep/prefilter/{vuln_type}/{language}/ \
  --verbose

# 生成测试报告
python3 /opt/Vul-AI/scripts/test_semgrep_rule.py \
  --rule-dir rules/semgrep/prefilter/{vuln_type}/{language}/ \
  --markdown test-report.md
```

### 7.3 参考资料

- CWE-{cwe_number}: {cwe_url}
- Semgrep 规则语法: https://semgrep.dev/docs/writing-rules/rule-syntax/
- 项目知识库: knowledge/{vuln_type}/{language}.md

---

**报告生成者**: semgrep-rule-engineer
**版本**: 1.0
**最后更新**: {timestamp}
