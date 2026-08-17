# Architecture Guide

## Purpose

AI QA Engineer is a controlled agentic Quality Engineering platform. Its architecture separates **reasoning** from **execution** so that AI can plan work without receiving unrestricted access to production systems.

## System layers

```text
Business / API
      │
      ▼
Agent Orchestration
      │
      ├── Requirement Agent
      ├── Test Design Agent
      ├── Automation Agent
      ├── Failure Analysis Agent
      └── Quality Gate
      │
      ▼
Controlled Tools
      │
      ├── Playwright
      ├── Persistence
      ├── Reporting
      └── CI/CD
      │
      ▼
Governance
      │
      ├── Policy decision
      └── Human approval
      │
      ▼
Delivery
      │
      └── GitHub Actions → Docker → AWS EC2 → HTTPS UAT
```

## AgentRun and AgentStep

`AgentRun` represents the lifecycle of an autonomous workflow. `AgentStep` represents one ordered operation inside that workflow.

This gives every agent execution:

- a stable identifier
- lifecycle status
- ordered steps
- inputs and outputs
- an auditable execution trail

## Deterministic execution boundary

LLMs are not used as a substitute for deterministic execution.

For example:

```text
AI: "The test should click Login."
                 │
                 ▼
ExecutionService
                 │
                 ▼
Playwright: getByRole(BUTTON, "Login").click()
```

If an action is unsupported, execution fails explicitly. The platform does not invent browser behavior.

## Governance boundary

Every future external action should pass through the policy boundary:

```text
Agent request
    ↓
Policy Engine
    ├── ALLOW
    ├── APPROVAL_REQUIRED
    └── DENY
          │
          ▼
     Tool execution
```

Production deployment, shell/SSH, secrets and database administration are treated as sensitive capabilities.

## Quality Gate

The Quality Gate converts deterministic execution facts into a release decision:

```text
UAT execution
    ↓
PASS / FAIL metrics
    ↓
Requirement coverage
    ↓
QualityGateService
    ↓
APPROVED / BLOCKED
```

The gate does not decide based on an LLM opinion. AI analysis can provide context and recommendations, while the final release policy remains deterministic.

## CI/CD integration

The deployment workflow is intentionally ordered so that a deployed application must survive UAT before the pipeline is considered successful:

```text
Build
  ↓
Deploy
  ↓
Health check
  ↓
Real UAT execution
  ↓
Quality Gate
  ↓
CI/CD result
```

This turns UAT from a reporting activity into an actual release control.

## V12 architectural direction

The next major step is to replace the initial fixed UAT smoke suite with persisted, requirement-derived test execution:

```text
Requirement
   ↓
Agent test design
   ↓
Persisted UAT suite
   ↓
Playwright / API execution
   ↓
Evidence
   ↓
AI failure classification
   ↓
Safe repair candidate
   ↓
Validation + policy
   ↓
Quality Gate
```

The governance boundary remains in place as autonomy increases.
