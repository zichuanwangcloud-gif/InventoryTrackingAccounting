# Agent Templates

Ready-to-use templates for creating new agents. Copy and customize for your needs.

## Table of Contents

- [Simple Specialist Template](#simple-specialist-template)
- [Orchestrator Template](#orchestrator-template)
- [Validator Template](#validator-template)
- [Multi-Step Processor Template](#multi-step-processor-template)

---

## Simple Specialist Template

Use for focused, single-responsibility agents.

```markdown
---
name: my-specialist-agent
description: |
  Brief description of what this agent does.
  Include trigger keywords: keyword1, keyword2, keyword3

  <example>
  Context: When user needs to do X
  user: "example user request"
  assistant: "I'll use my-specialist-agent to handle this"
  </example>
model: inherit
color: blue
---

# My Specialist Agent

## Purpose

Clear statement of what this agent does and when to use it.

## Core Responsibilities

- Responsibility 1: What it does
- Responsibility 2: Another task
- Responsibility 3: Final responsibility

## Input Format

What the agent expects:
- Input type 1: Description
- Input type 2: Description
- Optional parameters: Description

## Execution Flow

### Step 1: Initialization
1. Validate inputs
2. Create work directory if needed
3. Initialize state

### Step 2: Main Processing
1. Load required data
2. Process according to logic
3. Generate intermediate results

### Step 3: Output Generation
1. Format results
2. Create output files
3. Return summary

## Output Format

**Files Created:**
- `output/report.md` - Main report
- `output/data.json` - Structured data

**Return Value:**
- Summary message
- File locations
- Status indicators

## Tools Available

- `read_file` - Read source files
- `codebase_search` - Search codebase
- `write` - Create output files
- `run_terminal_cmd` - Execute commands

## Error Handling

- **Input validation errors**: Return clear error message
- **File not found**: Check paths, suggest alternatives
- **Processing errors**: Log error, return partial results if possible

## Example Usage

```
User: "Use my-specialist-agent to analyze the codebase"
Agent: [Executes analysis, returns structured report]
```

---

## Orchestrator Template

Use for agents that coordinate multiple other agents.

```markdown
---
name: my-orchestrator-agent
description: |
  Coordinates multiple agents for complex workflows.
  Keywords: orchestrate, coordinate, workflow, multi-agent

  <example>
  Context: Complex multi-phase task
  user: "orchestrate the full analysis workflow"
  assistant: "I'll use my-orchestrator-agent to coordinate the process"
  </example>
model: inherit
color: purple
---

# My Orchestrator Agent

## Purpose

Coordinates multiple agents to complete complex, multi-phase tasks.

## Core Responsibilities

- Task sequencing and coordination
- Agent selection and routing
- Shared state management (blackboard)
- Error recovery and retry logic
- Progress tracking

## System Architecture

```
┌─────────────────────────────────────────┐
│      My Orchestrator Agent                │
├─────────────────────────────────────────┤
│                                          │
│  Phase 1 → Agent A                      │
│  Phase 2 → Agent B                      │
│  Phase 3 → Agent C                      │
│                                          │
│  ┌──────────────────────────────────┐   │
│  │      Blackboard (Shared State)    │   │
│  └──────────────────────────────────┘   │
│                                          │
└─────────────────────────────────────────┘
```

## Workflow

### Phase 1: Initialization
1. Create work directory: `.work/{task-id}/`
2. Initialize blackboard: `blackboard.json`
3. Validate inputs and prerequisites

**Blackboard Structure:**
```json
{
  "meta": {
    "taskId": "",
    "startedAt": "",
    "status": "initializing",
    "currentPhase": 0
  },
  "phases": {
    "phase1": { "status": "pending", "agent": "agent-a" },
    "phase2": { "status": "pending", "agent": "agent-b" },
    "phase3": { "status": "pending", "agent": "agent-c" }
  },
  "results": {}
}
```

### Phase 2: Agent Execution
For each phase:
1. Check prerequisites
2. Invoke appropriate agent
3. Wait for completion
4. Update blackboard
5. Handle errors

### Phase 3: Finalization
1. Aggregate results from all phases
2. Generate final report
3. Cleanup temporary files
4. Return summary

## Agent Routing

| Phase | Agent | Input | Output |
|-------|-------|-------|--------|
| Phase 1 | agent-a | Initial data | Intermediate result 1 |
| Phase 2 | agent-b | Result 1 | Intermediate result 2 |
| Phase 3 | agent-c | Result 2 | Final result |

## Error Handling

- **Agent failure**: Retry with fallback agent
- **Phase failure**: Skip dependent phases, report partial results
- **Blackboard corruption**: Reinitialize from last known good state

## Output Format

**Final Report:**
- `final-report.md` - Complete workflow summary
- `blackboard.json` - Final state
- `phase-results/` - Individual phase outputs

---

## Validator Template

Use for agents that verify, test, or validate results.

```markdown
---
name: my-validator-agent
description: |
  Validates and verifies results with evidence collection.
  Keywords: validate, verify, test, check, evidence

  <example>
  Context: Need to verify findings
  user: "validate these results"
  assistant: "I'll use my-validator-agent to verify with evidence"
  </example>
model: inherit
color: orange
---

# My Validator Agent

## Purpose

Validates findings, collects evidence, and generates verification reports.

## Core Responsibilities

- Input validation and triage
- Evidence collection
- Verification testing
- Confidence scoring
- Report generation

## Verification Process

### Step 1: Triage
1. Cluster similar findings
2. Remove duplicates
3. Prioritize by severity
4. Group related items

### Step 2: Static Verification
1. Code pattern analysis
2. Data flow tracking
3. Reachability analysis
4. Pattern matching

### Step 3: Dynamic Verification
1. PoC construction
2. Test execution
3. Result validation
4. Evidence capture

### Step 4: Evidence Collection
1. Code snippets
2. Data flow diagrams
3. Execution logs
4. Test results

## Input Format

**Finding Format:**
```json
{
  "id": "finding-001",
  "type": "vulnerability-type",
  "location": "file:line",
  "description": "Finding description",
  "severity": "high|medium|low"
}
```

## Output Format

**Verified Results:**
- `verified-results.json` - Validated findings
- `evidence-chains/` - Evidence for each finding
- `verification-report.md` - Summary report

**Evidence Chain Structure:**
```json
{
  "findingId": "finding-001",
  "status": "verified|false-positive|inconclusive",
  "confidence": 0.95,
  "evidence": {
    "code": "code-snippet",
    "dataflow": "flow-diagram",
    "poc": "poc-code",
    "execution": "test-results"
  }
}
```

## Tools Available

- `read_file` - Read source code
- `codebase_search` - Find related code
- `grep` - Pattern matching
- `run_terminal_cmd` - Execute tests
- `write` - Create evidence files

---

## Multi-Step Processor Template

Use for agents that process data through multiple stages.

```markdown
---
name: my-processor-agent
description: |
  Processes data through multiple transformation stages.
  Keywords: process, transform, analyze, pipeline

  <example>
  Context: Need to process data
  user: "process this data through the pipeline"
  assistant: "I'll use my-processor-agent to transform the data"
  </example>
model: inherit
color: green
---

# My Processor Agent

## Purpose

Processes input data through multiple transformation stages.

## Processing Pipeline

```
Input → Stage 1 → Stage 2 → Stage 3 → Output
```

### Stage 1: Data Loading
1. Read input files
2. Parse formats
3. Validate structure
4. Normalize data

### Stage 2: Transformation
1. Apply transformations
2. Filter data
3. Enrich with metadata
4. Generate intermediate results

### Stage 3: Output Generation
1. Format results
2. Create output files
3. Generate summary
4. Cleanup temporary data

## Configuration

**Processing Options:**
- `strictMode`: Enable strict validation
- `parallelProcessing`: Process in parallel
- `cacheResults`: Cache intermediate results

## Output Format

**Results:**
- `output/processed-data.json` - Final processed data
- `output/stage-results/` - Intermediate stage outputs
- `output/summary.md` - Processing summary

## Error Handling

- **Invalid input**: Return validation errors
- **Processing failure**: Save partial results, report failure point
- **Output errors**: Retry with alternative format

---

## Customization Guide

### 1. Replace Placeholders

- `my-specialist-agent` → Your agent name
- `Purpose` → Your agent's purpose
- `Responsibilities` → Your agent's tasks

### 2. Add Domain-Specific Sections

- Security agents: Add threat modeling, vulnerability analysis
- Code analysis: Add AST parsing, pattern matching
- Documentation: Add template generation, formatting

### 3. Customize Output Format

- JSON for structured data
- Markdown for reports
- HTML for web views
- Multiple formats for different use cases

### 4. Add Tool-Specific Instructions

- File operations: Specify paths, formats
- API calls: Include endpoints, authentication
- Database: Include schemas, queries

---

## Quick Start

1. **Choose template** - Match your agent type
2. **Copy template** - Create new `.md` file
3. **Customize** - Replace placeholders
4. **Test** - Run with real scenarios
5. **Iterate** - Refine based on usage

---

**Next Steps:**
- See [BEST_PRACTICES.md](BEST_PRACTICES.md) for design guidelines
- See [AGENT_EXAMPLES.md](AGENT_EXAMPLES.md) for real-world examples

