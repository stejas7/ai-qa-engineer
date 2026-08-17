# AI QA Engineer — Agentic Java + GenAI Platform

A build-first reference implementation for exploring how **Java, Spring Boot, GenAI and AI Agents** can be combined into an enterprise automation platform.

## Vision

The goal is to evolve from a requirement-aware assistant into a production-oriented **agentic engineering platform** that can understand business requirements, plan work, use tools, execute actions, analyze outcomes and safely improve its own automation.

## Efficient Architecture

```text
User / Business Requirement
          ↓
   Agent Orchestrator
          ↓
   AgentRun / AgentStep
          ↓
Requirement → Test Design → Automation → Execution → Analysis
          ↓
       Tool Gateway
   Browser • Git • APIs • DB • CI/CD • Cloud
          ↓
   Evidence + Audit State
```

### Architecture principles

- **Orchestrator-first:** agents are specialized workers; orchestration owns workflow state and routing.
- **Tool isolation:** agents access external systems through controlled tool contracts rather than unrestricted access.
- **Structured outputs:** typed request/response models and machine-readable agent results.
- **Deterministic execution:** LLMs plan/reason; deterministic tools perform actions.
- **Evidence-first:** execution produces auditable steps and results.
- **Safe autonomy:** future destructive actions can require approval before execution.
- **Provider-neutral:** LLM integration remains replaceable.

## Current Flow

```text
Business Requirement
        ↓
AI Requirement Analysis
        ↓
AI Test Design
        ↓
Playwright Automation Generation
        ↓
UAT Execution
        ↓
Evidence
        ↓
AI Failure Analysis
```

## Versioned Roadmap — V1 → V10

| Version | Capability | Outcome |
|---|---|---|
| **V1** | Requirement Analysis Agent | Convert business requirements into structured scenarios and persist them. |
| **V2** | Test Design Agent | Generate detailed functional, negative, boundary and traceability test cases. |
| **V3** | Automation Generation Agent | Convert test cases into Java + Playwright automation. |
| **V4** | Autonomous UAT Execution | Execute generated tests against a demo UAT application and collect evidence. |
| **V5** | AI Failure Analysis | Analyze failures, classify probable root cause and recommend next action. |
| **V6** | Safe Self-Healing Agent | Detect broken locators/automation, generate candidate fixes, validate them and support approval-based retry. |
| **V7** | RAG + Enterprise Knowledge | Add pgvector/RAG for requirements, standards, historical defects, test knowledge and reusable patterns. |
| **V8** | Multi-Agent + Tool Orchestration | Coordinate specialized agents through persisted AgentRuns/AgentSteps and controlled tool contracts. |
| **V9** | Autonomous CI/CD + Cloud | Trigger pipelines, execute regression suites, publish evidence and integrate cloud deployment. |
| **V10** | Production-Ready Agentic Engineering Platform | Secure, observable, policy-controlled multi-agent platform with human-in-the-loop governance. |

## Current Implementation

### V1 — Requirement Agent
Business Requirement → AI Requirement Analysis → structured test scenarios → PostgreSQL → dashboard.

### V2 — Test Design Agent
Business Requirement → AI Requirement Analysis → detailed executable test cases.

### V3 — Playwright Automation Agent
Test case → automation request → generated Java + Playwright test skeleton.

### V4 — UAT Execution Agent
V4 adds a deliberately simple demo UAT login application and real Playwright execution.

- Demo UAT: `/uat/`
- Execution dashboard: `/v4.html`
- API: `POST /api/execution/run`
- Captures execution duration and screenshot evidence

### V5 — Failure Analysis Agent
V5 analyzes failed V4 executions.

- Dashboard: `/v5.html`
- API: `POST /api/failure-analysis/analyze`
- Deterministic fallback works without an AI key
- Optional OpenAI Responses API analysis when `OPENAI_API_KEY` is configured
- Classifies probable failure root cause and recommends retry or investigation

### V8 — Multi-Agent Orchestration
V8 turns the earlier agent UI into a real persisted orchestration flow.

- Dashboard: `/v8.html`
- `AgentRun` persists lifecycle state: CREATED → RUNNING → COMPLETED/FAILED
- `AgentStep` persists ordered execution steps and outputs
- `AgentOrchestrator` owns run/step lifecycle and state
- Controlled `AgentTool` / `AgentToolResult` contract introduced for tool isolation
- Real pipeline: **Requirement Agent → Test Design Agent → Automation Agent**
- API: `POST /api/agents/pipeline`
- Existing V1–V3 services are invoked by the orchestrator rather than duplicated
- Pipeline result exposes run ID, scenario count, test-case count and generated automation artifacts

## Technology Stack

- **Java 21**
- **Spring Boot 3.5.3**
- **Maven**
- **PostgreSQL 16**
- **Playwright for Java 1.52.0**
- **OpenAI Responses API**
- **Docker / Docker Compose**
- **GitHub Actions**
- Planned: **pgvector, RAG, MCP/tool integrations, cloud observability and V9 autonomous execution**

## Run in GitHub Codespaces

```bash
git checkout main
git pull origin main
docker compose up -d postgres
mvn clean verify
mvn -pl ai-qa-api exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
mvn spring-boot:run -pl ai-qa-api
```

Then open `/v8.html` for the V8 Agent Control Center.

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

## Project Direction

This repository is intentionally developed incrementally. V1–V5 establish the requirement → design → automation → execution → analysis loop. V8 adds real multi-agent orchestration and controlled tool contracts. V9 will extend this into autonomous CI/CD and cloud execution.

The long-term objective is not simply an AI chatbot. It is a **controlled agentic engineering system** where AI handles planning and reasoning while deterministic tools execute actions with auditable evidence and human governance.
