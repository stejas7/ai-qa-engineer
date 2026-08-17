# V8 — Multi-Agent Orchestration

## Goal

V8 evolves the platform from independent AI capabilities into a controlled multi-agent system. Specialized agents collaborate through an orchestrator and a permissioned Tool Gateway.

## Architecture

```text
                    Web UI / API
                         |
                         v
                Agent Orchestrator
                         |
          +--------------+--------------+
          |              |              |
          v              v              v
   Requirement      Test Design     Knowledge/RAG
      Agent            Agent            Agent
          |              |              |
          +--------------+--------------+
                         |
                         v
                  Automation Agent
                         |
                         v
                   Execution Agent
                         |
                         v
                Failure Analysis Agent
                         |
                         v
                 Self-Healing Agent
                         |
                         v
                   Reporting Agent
                         |
                         v
                   CI/CD Agent
                         |
                         v
                  Controlled Tools
          +----------+---+---+----------+
          |          |       |          |
       Browser      Git      DB       CI/CD
```

## Agent lifecycle

```text
CREATED → PLANNING → RUNNING → WAITING_APPROVAL → COMPLETED
                                      |
                                      +→ FAILED / CANCELLED
```

## Core concepts

### AgentRun

Represents one orchestrated agent execution and contains project, status, timestamps, and concise decision summaries.

### AgentStep

Represents a deterministic step performed by an agent. Steps reference tool calls and results rather than private model chain-of-thought.

### ToolCall / ToolResult

Every external action is routed through the Tool Gateway, validated against policy, executed with a timeout, and audited.

## Controlled Tool Gateway

Tools planned for V8:

- BrowserTool — approved Playwright operations
- GitTool — approved repository operations
- DatabaseTool — scoped database operations
- FileTool — restricted project files
- CI/CDTool — approved pipeline operations
- KnowledgeTool — project-scoped retrieval

The LLM never receives unrestricted shell or filesystem access.

## Governance

- Tool allowlists
- Project isolation
- Timeouts
- Input validation
- Audit events
- Human approval for risky actions
- No secret exposure
- No hidden chain-of-thought persistence

## V8 outcome

The platform can coordinate specialized agents while retaining deterministic boundaries between AI planning and real-world execution.

V9 builds on this foundation with autonomous CI/CD and cloud execution. V10 adds production governance, RBAC, multi-project support, resilience, and enterprise controls.
