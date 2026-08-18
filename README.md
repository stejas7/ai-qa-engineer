# AURAVIS 2.0.0 — Autonomous AI UAT Engineer Backend

> **From business requirement to release confidence — autonomously.**

This repository contains the **Java / Spring Boot backend** for Auravis. The user interface lives in the separate React repository: `stejas7/ai-qa-frontend`.

Auravis is a learning and AI engineering portfolio project that combines requirement intelligence, RAG, agentic orchestration, deterministic browser automation, controlled self-healing, persistence, evidence and cloud delivery around one end-to-end UAT problem.

## Architecture

```text
React / TypeScript frontend
        |
        | HTTPS / JSON
        v
Nginx on AWS EC2
        |
        | /api/*
        v
Spring Boot backend
        |
        +--> PostgreSQL 16
        +--> RAG / knowledge
        +--> M5 agent orchestration
        +--> Playwright execution
        +--> M6 controlled self-healing
        +--> Evidence / analytics
```

The backend is intentionally API-first. Legacy Spring-served product pages and HTML-injection filters have been removed after the React migration.

## Product Mission

```text
Business Requirement + UAT Target
              |
              v
       Knowledge Retrieval
              |
              v
    Requirement Intelligence
              |
              v
      Intelligent Test Design
              |
              v
      Agentic Orchestration
              |
              v
        Playwright Execution
              |
       +------+------+
       |             |
      PASS          FAIL
                      |
                      v
               Failure Diagnosis
                      |
                +-----+-----+
                |           |
       Recoverable issue  Business/assertion failure
                |           |
                v           v
        Controlled Healing  Never auto-heal
                |
                v
            One Retry
                |
                v
           QA Decision
```

## Engineering Principle

> **AI understands, plans and diagnoses. Java controls state and policy. Playwright executes. Evidence proves what happened.**

## Roadmap

| Milestone | Capability | Status |
|---|---|---|
| M1 | Autonomous Mission | ✅ Implemented |
| M2 | Knowledge / RAG Foundation | ✅ Implemented foundation |
| M3 | Intelligent Test Generation | ✅ Implemented |
| M4 | Advanced Automation & Multi-App Support | ✅ Implemented |
| M5 | Agentic Orchestration | ✅ Complete |
| M6 | Self-Healing & Smart Recovery | ✅ Complete |
| M7 | Regression & Learning Intelligence | 🔨 In progress |
| M8 | Defect Management & Autonomous CI/CD Quality Gate | Planned |

**Roadmap progress: 6 / 8 milestones complete (75%).**

## M5 — Agentic Orchestration

M5 coordinates the full QA engineering flow through persisted `AgentRun` and ordered `AgentStep` records:

```text
REQUIREMENT_ANALYSIS
  -> TEST_DESIGN
  -> AUTOMATION_GENERATION
  -> UAT_EXECUTION
  -> FAILURE_DIAGNOSIS (when needed)
  -> QUALITY_DECISION
```

The orchestration layer records state and decisions, while deterministic Java services perform the actual browser execution and quality evaluation.

## M6 — Controlled Self-Healing

M6 is integrated into the real execution path rather than existing only as a standalone API.

Safety rules:

- classify every failed automation action before healing
- locator, timeout, navigation and transient browser failures are recoverable candidates
- assertion, business, unsupported-action and unknown failures are not auto-healed
- confidence must be at least `0.90`
- at most one controlled retry is permitted
- healing decisions are persisted for audit
- before/after execution evidence is captured where possible
- fallback actions remain deterministic and bounded

## Backend Capabilities

### Requirement intelligence
- business intent analysis
- acceptance criteria extraction
- OpenAI-compatible integration with deterministic fallback
- TXT, Markdown, DOCX and PDF parsing

### Knowledge / RAG
- persisted project knowledge
- PostgreSQL-backed retrieval
- pgvector-ready architecture
- mission-time grounding

### Test design and automation
- functional, business-rule, negative, boundary and risk scenarios
- requirement traceability
- Excel and JSON export
- deterministic Playwright execution
- application target registry
- persisted execution history and evidence metadata

### Agentic orchestration
- persisted AgentRun / AgentStep activity
- policy and governance services
- requirement → design → automation → execution → diagnosis → quality decision flow
- React Agent Activity observability

### Self-healing and failure intelligence
- deterministic failure classification
- conservative healing authorization policy
- one bounded retry
- persisted healing history and metrics
- React Self-Healing observability page
- failure diagnosis and deterministic quality gate

### Product analytics
- anonymous React page-view tracking
- PostgreSQL persistence
- unique visitor and daily traffic summaries
- top-page and recent anonymous visit trace APIs
- raw IP addresses are not stored

## Backend API Reference

The live React product contains the human-readable API catalog at:

```text
https://auravis-uat.duckdns.org/api-reference
```

Primary endpoints include:

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/pipeline/upload` | Upload requirement file and start a mission |
| GET | `/api/pipeline/runs` | Persisted mission history |
| GET | `/api/pipeline/stats` | Mission processing metrics |
| GET | `/api/pipeline/runs/{id}` | Mission detail and structured result |
| GET | `/api/pipeline/runs/{id}/test-cases.json` | JSON test/result export |
| GET | `/api/pipeline/runs/{id}/test-cases.xlsx` | Excel test export |
| POST | `/api/agents/pipeline` | M5 end-to-end agent orchestration |
| GET | `/api/agent-activity/summary` | M5 orchestration summary |
| GET | `/api/agent-activity/runs?limit=20` | Recent AgentRun history |
| GET | `/api/agent-activity/runs/{runId}/steps` | Ordered AgentStep trace |
| POST | `/api/execution/run` | Playwright execution with M6 healing integration |
| GET | `/api/execution/history` | Execution audit history |
| GET | `/api/execution/stats` | PASS/FAIL execution metrics |
| GET | `/api/execution/evidence/{file}` | Screenshot evidence |
| POST | `/api/healing/evaluate` | M6 healing policy evaluation |
| GET | `/api/healing/history` | Persisted healing decisions |
| GET | `/api/healing/stats` | M6 healing metrics and policy status |
| POST | `/api/failure-analysis/analyze` | Failure diagnosis |
| POST | `/api/quality-gate/evaluate` | Deterministic release decision |
| GET | `/api/applications?activeOnly=true` | Active UAT application targets |
| POST | `/api/applications` | Register UAT target |
| POST | `/api/analytics/visit` | Anonymous page-view event |
| GET | `/api/analytics/stats` | Visitor/page-view summary |
| GET | `/api/analytics/recent` | Recent anonymous traffic trace |
| GET | `/actuator/health` | Deployment/application health |

Additional capability groups exist under `/api/knowledge/*` and `/api/rag/*`.

## Tests

Maven verification includes tests for core quality-gate, agent-policy, impact-analysis, test-design and M6 healing classification/policy behavior. More end-to-end browser tests remain part of ongoing engineering hardening rather than milestone marketing status.

## Demo UAT Fixture

`ai-qa-api/src/main/resources/static/uat/index.html` is intentionally retained as a deterministic login target for Playwright/UAT demonstrations. It is a test fixture, not the Auravis product UI.

## Technology Stack

Java 17+ • Spring Boot 3.5.x • Maven • Spring Data JPA • PostgreSQL 16 • pgvector-ready persistence • Playwright for Java • OpenAI-compatible AI integration • Apache POI • PDFBox • Docker / Docker Compose • GitHub Actions • GHCR • AWS EC2 • Nginx • HTTPS

Frontend: React • TypeScript • Vite • React Router • TanStack Query.

## Run Backend Locally

```bash
git clone https://github.com/stejas7/ai-qa-engineer.git
cd ai-qa-engineer
docker compose up -d postgres
mvn clean verify
mvn spring-boot:run -pl ai-qa-api
```

## Deployment

```text
Commit to main
  -> Maven verify
  -> Docker image
  -> GHCR
  -> AWS EC2
  -> local health check
  -> public health/API smoke check
  -> deployment success
```

Live environment: `https://auravis-uat.duckdns.org`
