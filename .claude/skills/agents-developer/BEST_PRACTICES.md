# Agent Best Practices

Comprehensive guidelines for designing, implementing, and maintaining high-quality agents.

## Table of Contents

- [Design Principles](#design-principles)
- [Naming Conventions](#naming-conventions)
- [Structure Guidelines](#structure-guidelines)
- [Error Handling](#error-handling)
- [Output Formatting](#output-formatting)
- [Tool Usage](#tool-usage)
- [Performance](#performance)
- [Testing](#testing)

---

## Design Principles

### 1. Single Responsibility Principle

**✅ DO:**
- One agent = One clear purpose
- Focused domain expertise
- Clear boundaries

**❌ DON'T:**
- God agents that do everything
- Mixed concerns
- Unclear responsibilities

**Example:**
```
✅ Good: validation-agent (validates vulnerabilities)
❌ Bad: security-agent (does everything security-related)
```

### 2. Standalone Principle

**✅ DO:**
- Self-contained agent files
- No external dependencies
- Works immediately after copy

**❌ DON'T:**
- Require specific project structure
- Depend on other agents being present
- Need manual configuration

**Example:**
```
✅ Good: Agent works in any project
❌ Bad: Agent requires specific directory structure
```

### 3. Explicit Instructions

**✅ DO:**
- Step-by-step numbered instructions
- Clear decision points
- Explicit tool usage

**❌ DON'T:**
- Vague or ambiguous instructions
- Implicit assumptions
- Unclear workflows

**Example:**
```
✅ Good:
1. Read file from path X
2. Parse JSON structure
3. Extract field Y
4. Write to output file Z

❌ Bad:
Process the data and create output
```

### 4. Structured Output

**✅ DO:**
- Predictable output format
- Consistent file locations
- Clear data structures

**❌ DON'T:**
- Ad-hoc output formats
- Inconsistent locations
- Unstructured data

**Example:**
```
✅ Good:
- output/report.md (always here)
- output/data.json (structured format)
- output/evidence/ (organized directory)

❌ Bad:
- Random file names
- Inconsistent locations
- Mixed formats
```

---

## Naming Conventions

### Agent Names

**Format:** `{domain}-{type}-{purpose}`

**Examples:**
- `security-orchestrator` - Security domain, orchestrator type
- `validation-agent` - Validation purpose
- `engineering-profiler` - Engineering domain, profiling purpose
- `threat-modeler` - Threat domain, modeling purpose

**Rules:**
- Lowercase only
- Hyphens for word separation
- Descriptive and specific
- Avoid abbreviations unless common

### File Names

**Format:** `{agent-name}.md`

**Location:** `.claude/agents/{agent-name}.md`

**Example:**
- Agent: `security-orchestrator`
- File: `.claude/agents/security-orchestrator.md`

### Output Directories

**Format:** `.{domain}/{task-id}/` or `output/`

**Examples:**
- `workspace/{target-name}/analyses/{analysis-id}/`
- `output/`
- `.work/{agent-name}/`

---

## Structure Guidelines

### YAML Frontmatter

**Required Fields:**
```yaml
---
name: agent-name
description: |
  Multi-line description with keywords
model: inherit
---
```

**Optional Fields:**
```yaml
color: blue | green | red | orange | purple
```

**Best Practices:**
- Include all trigger keywords in description
- Add `<example>` blocks for usage
- Keep description under 1024 chars
- Use `inherit` for model (default)

### Document Structure

**Required Sections:**
1. **Purpose** - What and why
2. **Core Responsibilities** - Main tasks
3. **Execution Flow** - Step-by-step process
4. **Output Format** - What to return

**Optional Sections:**
- System Architecture (for orchestrators)
- Input Format (if complex)
- Error Handling (if specific)
- Tools Available (if limited set)

**Order:**
1. Purpose
2. Core Responsibilities
3. System Architecture (if applicable)
4. Input Format (if applicable)
5. Execution Flow
6. Output Format
7. Tools Available
8. Error Handling

---

## Error Handling

### Error Types

**1. Input Validation Errors**
```markdown
## Error Handling

### Invalid Input
- Check input format
- Validate required fields
- Return clear error message with expected format
```

**2. File Not Found**
```markdown
### File Not Found
- Check file existence before reading
- Suggest alternative paths
- Return helpful error message
```

**3. Processing Errors**
```markdown
### Processing Errors
- Log error details
- Save partial results if possible
- Return error summary
- Suggest recovery steps
```

### Error Messages

**✅ Good Error Messages:**
- Clear and specific
- Actionable suggestions
- Include context

**❌ Bad Error Messages:**
- Vague or generic
- No suggestions
- Missing context

**Example:**
```
✅ Good:
Error: File not found at path 'src/api/controller.ts'
Suggestion: Check if file exists or use codebase_search to find similar files

❌ Bad:
Error: File not found
```

---

## Output Formatting

### File Organization

**Structure:**
```
output/
├── report.md          # Main report
├── data.json          # Structured data
├── evidence/          # Evidence files
│   ├── finding-001.md
│   └── finding-002.md
└── summary.md         # Quick summary
```

### Report Format

**Markdown Reports:**
```markdown
# Report Title

## Summary
Brief overview

## Details
Detailed information

## Findings
- Finding 1
- Finding 2

## Recommendations
- Recommendation 1
- Recommendation 2
```

### JSON Format

**Structured Data:**
```json
{
  "meta": {
    "agent": "agent-name",
    "timestamp": "2024-01-01T00:00:00Z",
    "version": "1.0"
  },
  "results": [
    {
      "id": "result-001",
      "type": "type",
      "data": {}
    }
  ]
}
```

---

## Tool Usage

### Recommended Tools

**File Operations:**
- `read_file` - Read files
- `write` - Create files
- `grep` - Search patterns
- `codebase_search` - Semantic search

**Code Analysis:**
- `codebase_search` - Find related code
- `grep` - Pattern matching
- `read_file` - Read source code

**Execution:**
- `run_terminal_cmd` - Run commands
- `read_lints` - Check for errors

### Tool Usage Patterns

**Pattern 1: Read-Process-Write**
```markdown
1. Use `read_file` to load input
2. Process data
3. Use `write` to save output
```

**Pattern 2: Search-Analyze-Report**
```markdown
1. Use `codebase_search` to find relevant code
2. Use `read_file` to analyze details
3. Use `write` to create report
```

**Pattern 3: Execute-Validate-Report**
```markdown
1. Use `run_terminal_cmd` to execute
2. Use `read_file` to check results
3. Use `write` to create report
```

### Tool Limitations

**Be Aware Of:**
- Rate limits on API calls
- File size limits
- Execution timeouts
- Memory constraints

**Best Practices:**
- Batch operations when possible
- Handle timeouts gracefully
- Check file sizes before reading
- Use streaming for large files

---

## Performance

### Optimization Strategies

**1. Parallel Processing**
- Process independent tasks in parallel
- Use batch operations
- Minimize sequential dependencies

**2. Caching**
- Cache expensive computations
- Reuse intermediate results
- Store state for retries

**3. Early Termination**
- Stop on critical errors
- Skip unnecessary steps
- Return partial results when appropriate

### Performance Guidelines

**Target Metrics:**
- Simple agents: < 1 minute
- Medium agents: < 5 minutes
- Complex agents: < 15 minutes

**Optimization Tips:**
- Minimize file I/O
- Use efficient search patterns
- Batch tool calls
- Cache results

---

## Testing

### Test Scenarios

**1. Happy Path**
- Test with valid inputs
- Verify correct output
- Check file creation

**2. Error Cases**
- Invalid inputs
- Missing files
- Processing errors

**3. Edge Cases**
- Empty inputs
- Large files
- Special characters

### Testing Checklist

- [ ] Agent executes successfully
- [ ] Output format matches specification
- [ ] Files created in correct locations
- [ ] Error handling works
- [ ] Performance acceptable
- [ ] Works in different projects
- [ ] No hardcoded paths

### Manual Testing

**Test Command:**
```
Ask Claude: "Use the {agent-name} to [task]"
```

**Verify:**
- Agent activates correctly
- Executes all steps
- Creates expected output
- Handles errors gracefully

---

## Common Pitfalls

### Pitfall 1: Hardcoded Paths

**❌ Bad:**
```markdown
Read file from: /Users/username/project/src/file.ts
```

**✅ Good:**
```markdown
Read file from: $CLAUDE_PROJECT_DIR/src/file.ts
Or: Use relative path: src/file.ts
```

### Pitfall 2: Vague Instructions

**❌ Bad:**
```markdown
Analyze the code and create a report
```

**✅ Good:**
```markdown
1. Use codebase_search to find all API endpoints
2. For each endpoint, read the controller file
3. Extract route, method, and handler
4. Write to output/api-endpoints.json
```

### Pitfall 3: Missing Error Handling

**❌ Bad:**
```markdown
Read the file and process it
```

**✅ Good:**
```markdown
1. Check if file exists
2. If not found, return error with suggestion
3. Read file
4. If read fails, return error
5. Process data
6. If processing fails, save partial results
```

### Pitfall 4: Unclear Output

**❌ Bad:**
```markdown
Return the results
```

**✅ Good:**
```markdown
Create the following files:
- output/report.md: Main report with findings
- output/data.json: Structured data in JSON format
- output/evidence/: Directory with evidence files

Return summary message with file locations.
```

---

## Maintenance

### Version Control

- Keep agents in version control
- Document changes in commit messages
- Tag stable versions

### Documentation Updates

- Update README when adding agents
- Keep examples current
- Document breaking changes

### Refactoring

- Extract common patterns
- Consolidate similar agents
- Remove deprecated agents

---

## Summary

**Key Principles:**
1. Single responsibility
2. Standalone deployment
3. Explicit instructions
4. Structured output

**Best Practices:**
- Clear naming conventions
- Consistent structure
- Comprehensive error handling
- Thorough testing

**Avoid:**
- Hardcoded paths
- Vague instructions
- Missing error handling
- Unclear output

---

**Next Steps:**
- See [AGENT_TEMPLATE.md](AGENT_TEMPLATE.md) for templates
- See [SKILL.md](SKILL.md) for complete guide

