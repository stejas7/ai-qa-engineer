# AI QA Engineer — Agentic Java + GenAI Platform

A build-first reference implementation for combining **Java, Spring Boot, GenAI and AI Agents** into an enterprise automation platform.

## Vision

Evolve from a requirement-aware assistant into a production-oriented **agentic engineering platform** that understands requirements, plans work, uses controlled tools, executes actions, analyzes outcomes and safely improves automation.

```text
Business Requirement
        ↓
Agent Orchestrator → AgentRun / AgentStep
        ↓
Requirement → Test Design → Automation → Execution → Analysis
        ↓
Policy Engine → Human Approval → Tool Execution
        ↓
Evidence + Audit
        ↓
Quality Gate → CI/CD Decision
```

## Architecture principles

- Orchestrator-first: specialized agents, centralized workflow state.
- Tool isolation: external systems are accessed through controlled contracts.
- Structured outputs: typed request/response models and machine-readable results.
- Deterministic execution: LLMs plan/reason; deterministic tools perform actions.
- Evidence-first: execution produces auditable steps and results.
- Safe autonomy: sensitive/destructive actions can require human approval.
- Provider-neutral: LLM integration remains replaceable.

## Versioned Roadmap — V1 → V11

| Version | Capability | Outcome |
|---|---|---|
| V1 | Requirement Analysis Agent | Structured scenarios from business requirements. |
| V2 | Test Design Agent | Functional, negative, boundary and traceability tests. |
| V3 | Automation Generation Agent | Java + Playwright automation generation. |
| V4 | Autonomous UAT Execution | Real Playwright execution and evidence. |
| V5 | AI Failure Analysis | Root-cause classification and next-action recommendation. |
| V6 | Safe Self-Healing Agent | Candidate locator/automation fixes with validation and approval. |
| V7 | RAG + Enterprise Knowledge | pgvector/RAG for requirements, standards and historical QA knowledge. |
| V8 | Multi-Agent + Tool Orchestration | Persisted AgentRuns/AgentSteps and controlled tool contracts. |
| V9 | Autonomous CI/CD + Cloud | CI/CD, regression, evidence and cloud deployment. **Deployed baseline.** |
| V10 | Production Agent Governance | Policy evaluation, sensitive-action approval and governance APIs. |
| V11 | Autonomous Quality Gate | APPROVED/BLOCKED decision from UAT results and requirement coverage. |

## V9 — Deployed Baseline

V9 is the deployment foundation for V10/V11. GitHub Actions deploys to AWS EC2 using Docker Compose, performs safe Docker recovery/cleanup, starts the services and validates `/actuator/health` before declaring deployment successful.

Current HTTPS UAT environment:

`https://tejas-aiqa.duckdns.org`

## V10 — Production Agent Governance

V10 introduces a deterministic policy boundary between agent reasoning and external actions.

### Policy API

`POST /api/governance/policy/evaluate`

Example:

```json
{"action":"DEPLOY","tool":"CI_CD","environment":"PRODUCTION"}
```

Decisions:

- `ALLOW` — deterministic action permitted.
- `APPROVAL_REQUIRED` — sensitive action needs human approval.
- `DENY` — no policy permits the action.

UAT Playwright execution and failure analysis are allowed by default. Production deployment and sensitive tools such as shell/SSH/secrets require approval.

### Human approval APIs

- `POST /api/governance/approvals`
- `GET /api/governance/approvals/pending`
- `GET /api/governance/runs/{runId}/approvals`
- `POST /api/governance/approvals/{approvalId}/decision`

Approval requests are persisted in `agent_approvals` and can be linked to an `AgentRun`.

## V11 — Autonomous Quality Gate

V11 introduces a deterministic quality gate that converts UAT evidence into a deployment decision.

`POST /api/quality-gate/evaluate`

Example:

```json
{"totalTests":10,"passedTests":10,"failedTests":0,"automatedTests":10,"requirements":4,"coveredRequirements":4}
```

A clean run with full requirement coverage returns `APPROVED`. Any failed test or incomplete requirement coverage returns `BLOCKED`.

The next V11 increments will connect this gate to persisted execution results, AI failure classification, self-healing validation and the V9 CI/CD workflow.

## Current Implementation

### V1
Business Requirement → AI Requirement Analysis → structured scenarios → PostgreSQL → dashboard.

### V2
Business Requirement → AI Requirement Analysis → executable test cases.

### V3
Test case → automation request → generated Java + Playwright test skeleton.

### V4
Demo UAT login application and real Playwright execution.

- Demo UAT: `/uat/`
- Execution dashboard: `/v4.html`
- API: `POST /api/execution/run`

### V5
Failure analysis dashboard/API with deterministic fallback and optional OpenAI Responses API analysis.

### V8
Persisted multi-agent orchestration:

- Dashboard: `/v8.html`
- `AgentRun`: CREATED → RUNNING → COMPLETED/FAILED
- `AgentStep`: ordered execution steps and outputs
- `AgentOrchestrator`: run/step lifecycle
- `AgentTool` / `AgentToolResult`: controlled tool contract
- Pipeline: Requirement → Test Design → Automation
- API: `POST /api/agents/pipeline`

## Technology Stack

- Java 21
- Spring Boot 3.5.3
- Maven
- PostgreSQL 16
- Playwright for Java 1.52.0
- OpenAI Responses API
- Docker / Docker Compose
- GitHub Actions

## APIs

### V3
`POST /api/automation/generate`

### V4
`POST /api/execution/run`

### V5
`POST /api/failure-analysis/analyze`

### V8
`POST /api/agents/pipeline`

`GET /api/agents/runs`

`GET /api/agents/runs/{id}`

`GET /api/agents/runs/{id}/steps`

### V10
`POST /api/governance/policy/evaluate`

`POST /api/governance/approvals`

`GET /api/governance/approvals/pending`

`GET /api/governance/runs/{runId}/approvals`

`POST /api/governance/approvals/{approvalId}/decision`

### V11
`POST /api/quality-gate/evaluate`

## Project Direction

V9 is the deployed AWS/CI/CD foundation. V10 adds governance and safe autonomy. V11 adds the quality gate that becomes the decision point for autonomous CI/CD. The long-term objective is a **controlled agentic engineering system** where AI handles planning/reasoning while deterministic tools execute actions with auditable evidence and human governance.
