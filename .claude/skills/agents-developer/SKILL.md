---
name: agents-developer
description: Create and manage Claude Code agents following best practices. Use when creating new agents, modifying agent files, understanding agent structure, working with agent templates, debugging agent execution, or implementing multi-agent workflows. Covers agent structure, YAML frontmatter, agent types (orchestrator, specialist, validator), tool access patterns, output formats, agent coordination, and the standalone principle.
---

# Agent Developer Guide

## Purpose

Comprehensive guide for creating and managing agents in Claude Code, following best practices for autonomous task execution, multi-agent coordination, and standalone deployment.

## When to Use This Skill

Automatically activates when you mention:
- Creating or adding agents
- Modifying agent files or structure
- Understanding how agents work
- Working with agent templates
- Debugging agent execution
- Multi-agent workflows
- Agent coordination
- Agent vs skill decisions
- YAML frontmatter for agents
- Standalone agent deployment

---

## System Overview

### What Are Agents?

Agents are **autonomous Claude instances** that handle specific complex tasks. Unlike skills (which provide inline guidance), agents:

- **Run as separate sub-tasks** - Execute independently
- **Work autonomously** - Minimal supervision required
- **Have specialized tool access** - Focused capabilities
- **Return comprehensive reports** - Structured output when complete
- **Standalone** - Just copy the `.md` file and use immediately

### Agent vs Skill

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

## Agent Types

### 1. Orchestrator Agents

**Purpose:** Coordinate multiple agents and manage complex workflows

**Characteristics:**
- Type: `orchestrator`
- Manages task sequencing
- Coordinates multiple specialist agents
- Maintains shared context (blackboard)
- Handles error recovery and retries

**Examples:**
- `security-orchestrator` - Coordinates security analysis workflow
- `task-planner` - Plans and schedules vulnerability mining tasks

**When to Use:**
- Multi-phase workflows
- Complex task coordination
- Need to manage shared state
- Sequential dependencies

### 2. Specialist Agents

**Purpose:** Handle specific domain tasks autonomously

**Characteristics:**
- Type: `specialist`
- Focused domain expertise
- Single responsibility
- Autonomous execution
- Returns structured results

**Examples:**
- `engineering-profiler` - Builds engineering profiles
- `threat-modeler` - Creates threat models
- `validation-agent` - Validates vulnerabilities

**When to Use:**
- Well-defined single tasks
- Domain-specific expertise needed
- Can work independently
- Clear input/output format

### 3. Validator Agents

**Purpose:** Verify, test, or validate results

**Characteristics:**
- Type: `validator`
- Focus on verification
- May run tests or PoCs
- Evidence collection
- Quality assurance

**Examples:**
- `validation-agent` - Validates vulnerabilities with PoC
- `code-architecture-reviewer` - Reviews architectural consistency

**When to Use:**
- Need verification step
- Quality assurance required
- Evidence collection needed
- Testing or validation workflows

---

## Quick Start: Creating a New Agent

### Step 1: Create Agent File

**Location:** `.claude/agents/{agent-name}.md`

**Template:**
```markdown
---
name: my-new-agent
description: |
  Brief description including keywords that trigger this agent.
  Mention purpose, use cases, and when to activate.
  Include example scenarios.

  <example>
  Context: When user needs to do X
  user: "example user request"
  assistant: "example agent response"
  </example>
model: inherit
color: blue
---

# My New Agent

## Purpose
What this agent does and why it exists

## Core Responsibilities
- Responsibility 1
- Responsibility 2
- Responsibility 3

## Input Format
What the agent expects as input

## Execution Flow
Step-by-step process

## Output Format
What the agent returns

## Tools Available
List of tools this agent can use
```

**Best Practices:**
- ✅ **Name**: Lowercase, hyphens, descriptive (e.g., `security-orchestrator`)
- ✅ **Description**: Include ALL trigger keywords/phrases (max 1024 chars)
- ✅ **Examples**: Real usage scenarios in `<example>` blocks
- ✅ **Structure**: Clear sections with Purpose, Responsibilities, Flow, Output
- ✅ **Standalone**: No external dependencies, works immediately after copy

### Step 2: Define Agent Structure

**Required Sections:**

1. **YAML Frontmatter**
   - `name`: Agent identifier
   - `description`: Trigger keywords and usage examples
   - `model`: Usually `inherit` (uses Claude's default)
   - `color`: Optional, for UI display

2. **Purpose Section**
   - Clear statement of what the agent does
   - When to use it
   - Value proposition

3. **Core Responsibilities**
   - List of main tasks
   - Clear boundaries
   - Single responsibility principle

4. **Execution Flow**
   - Step-by-step process
   - Decision points
   - Error handling

5. **Output Format**
   - Structured output specification
   - File locations
   - Report formats

### Step 3: Test Agent

**Manual Testing:**
```bash
# Check agent file exists
ls -la .claude/agents/my-new-agent.md

# Verify YAML frontmatter
head -20 .claude/agents/my-new-agent.md

# Test with Claude
# Ask: "Use the my-new-agent to [task]"
```

**Integration Testing:**
- Test with real scenarios
- Verify output format
- Check error handling
- Validate tool usage

### Step 4: Document Usage

**Add to README:**
- Update `.claude/agents/README.md`
- Add agent description
- Include usage examples
- Note any customization needed

---

## YAML Frontmatter Reference

### Complete Schema

```yaml
---
name: agent-name
description: |
  Multi-line description with:
  - Purpose
  - Use cases
  - Trigger keywords
  - Example scenarios

  <example>
  Context: When to use
  user: "example request"
  assistant: "example response"
  </example>
model: inherit | claude-3-5-sonnet | claude-3-opus
color: blue | green | red | orange | purple
---
```

### Field Guide

**name** (required)
- Lowercase, hyphens
- Descriptive and unique
- Example: `security-orchestrator`, `validation-agent`

**description** (required)
- Multi-line YAML string
- Include trigger keywords
- Add `<example>` blocks for usage
- Max 1024 characters recommended

**model** (optional, default: `inherit`)
- `inherit`: Use Claude's default model
- `claude-3-5-sonnet`: Fast, cost-effective
- `claude-3-opus`: Most capable, slower

**color** (optional)
- UI display color
- Options: `blue`, `green`, `red`, `orange`, `purple`
- Helps distinguish agents visually

---

## Agent Structure Patterns

### Pattern 1: Simple Specialist

```markdown
# Simple Specialist Agent

## Purpose
Single, focused task

## Instructions
1. Step one
2. Step two
3. Step three

## Output
Return structured result
```

### Pattern 2: Orchestrator

```markdown
# Orchestrator Agent

## Purpose
Coordinate multiple agents

## Workflow
1. Phase 1 → Agent A
2. Phase 2 → Agent B
3. Phase 3 → Agent C

## Blackboard Management
- Shared state structure
- Update protocol
- Error recovery
```

### Pattern 3: Validator

```markdown
# Validator Agent

## Purpose
Verify and validate results

## Verification Steps
1. Input validation
2. Static analysis
3. Dynamic testing
4. Evidence collection

## Output
- Verification report
- Evidence chain
- Confidence scores
```

---

## Best Practices

### ✅ DO

- **Keep agents focused** - Single responsibility
- **Make them standalone** - No external dependencies
- **Use clear instructions** - Step-by-step, numbered
- **Specify output format** - Structured, predictable
- **Include examples** - Real usage scenarios
- **Handle errors gracefully** - Clear error messages
- **Document tool usage** - List available tools
- **Test thoroughly** - Multiple scenarios

### ❌ DON'T

- **Don't create god agents** - Too many responsibilities
- **Don't hardcode paths** - Use `$CLAUDE_PROJECT_DIR` or relative paths
- **Don't skip error handling** - Always handle failures
- **Don't assume context** - Be explicit about inputs
- **Don't create circular dependencies** - Agents should be independent
- **Don't mix concerns** - Keep agents focused

---

## Common Patterns

### Pattern: File-Based Output

```markdown
## Output Format

Create the following files:
- `output/report.md` - Main report
- `output/data.json` - Structured data
- `output/evidence/` - Evidence files
```

### Pattern: Blackboard Coordination

```markdown
## Blackboard Management

Read from: `workspace/{targetName}/analyses/{analysisId}/blackboard.json`
Update: Add results to `blackboard.findings[]`
Format: Follow blackboard schema
```

### Pattern: Multi-Phase Workflow

```markdown
## Execution Flow

### Phase 1: Initialization
1. Create work directory
2. Initialize state files
3. Validate inputs

### Phase 2: Analysis
1. Load data
2. Process
3. Generate results

### Phase 3: Reporting
1. Format output
2. Create reports
3. Cleanup
```

---

## Testing Checklist

When creating a new agent, verify:

- [ ] Agent file created in `.claude/agents/{name}.md`
- [ ] Proper YAML frontmatter with name and description
- [ ] Clear purpose and responsibilities
- [ ] Step-by-step execution flow
- [ ] Output format specified
- [ ] Tools listed explicitly
- [ ] Examples included in description
- [ ] Error handling documented
- [ ] No hardcoded paths (use variables or relative)
- [ ] Tested with real scenarios
- [ ] Added to `.claude/agents/README.md`
- [ ] Works standalone (no external deps)

---

## Reference Files

For detailed information on specific topics, see:

### [AGENT_TEMPLATE.md](AGENT_TEMPLATE.md)
Ready-to-use agent templates:
- Simple specialist template
- Orchestrator template
- Validator template
- Copy-paste ready

### [BEST_PRACTICES.md](BEST_PRACTICES.md)
Comprehensive best practices:
- Agent design principles
- Naming conventions
- Error handling patterns
- Output formatting
- Tool usage guidelines

### [AGENT_EXAMPLES.md](AGENT_EXAMPLES.md)
Real-world examples:
- Security orchestrator breakdown
- Validation agent structure
- Engineering profiler pattern
- Learn from existing agents

---

## Quick Reference Summary

### Create New Agent (4 Steps)

1. Create `.claude/agents/{name}.md` with YAML frontmatter
2. Define purpose, responsibilities, and flow
3. Test with real scenarios
4. Add to README

### Agent Types

- **Orchestrator**: Coordinates multiple agents
- **Specialist**: Focused domain task
- **Validator**: Verifies and tests

### YAML Frontmatter

- `name`: Required, lowercase-hyphens
- `description`: Required, include keywords and examples
- `model`: Optional, default `inherit`
- `color`: Optional, UI display

### Best Practices

✅ **Single responsibility** - One clear purpose
✅ **Standalone** - No external dependencies
✅ **Clear instructions** - Step-by-step
✅ **Structured output** - Predictable format
✅ **Error handling** - Graceful failures
✅ **Examples** - Real usage scenarios

### Troubleshoot

**Agent not found:**
```bash
ls -la .claude/agents/{agent-name}.md
```

**Path errors:**
```bash
grep "~/\|/root/\|/Users/" .claude/agents/{agent-name}.md
# Replace with $CLAUDE_PROJECT_DIR or relative paths
```

**Test agent:**
Ask Claude: "Use the {agent-name} to [task]"

---

## Related Files

**Agent Files:**
- `.claude/agents/*.md` - All agent definitions
- `.claude/agents/README.md` - Agent catalog

**Skills:**
- `.claude/skills/*/SKILL.md` - Related skills

**Configuration:**
- `.claude/config.json` - Claude Code configuration

---

**Skill Status**: COMPLETE ✅
**Next**: Create more agents, refine patterns based on usage

