# AURAVIS 2.0.0 — Autonomous AI UAT Engineer Backend

> **From business requirement to release confidence — autonomously.**

This repository contains the **Java / Spring Boot backend** for Auravis. The user interface has moved to the separate React repository: `stejas7/ai-qa-frontend`.

Auravis is a learning and AI engineering portfolio project that combines requirement intelligence, RAG, agentic orchestration, deterministic browser automation, persistence, evidence and cloud delivery around one end-to-end UAT problem.

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
        +--> Agent orchestration
        +--> Playwright execution
        +--> Evidence / analytics
```

The backend is intentionally API-first. Legacy Spring-served product pages and HTML-injection filters have been removed after the React migration.

## Product Mission

The user provides a business requirement / BRD / PRD / user story and a UAT target. Auravis aims to own the downstream QA workflow:

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
       Recoverable issue  Genuine defect
                |           |
                v           v
        Safe Self-Healing  Defect Management
                |           |
                +-----+-----+
                      |
                      v
              Final QA Decision
                      |
                      v
               Evidence + Report
```

## Engineering Principle

> **AI understands, plans and diagnoses. Java controls state and policy. Playwright executes. Evidence proves what happened.**

Auravis does not give an LLM unrestricted shell, filesystem, database or deployment access. Real-world actions stay behind deterministic application services and controlled tool boundaries.

## Roadmap

| Milestone | Capability | Status |
|---|---|---|
| M1 | Autonomous Mission | Implemented |
| M2 | Knowledge / RAG Foundation | Implemented foundation |
| M3 | Intelligent Test Generation | Implemented |
| M4 | Advanced Automation & Multi-App Support | Implemented |
| M5 | Agentic Orchestration | In progress — end-to-end orchestration wired |
| M6 | Self-Healing & Smart Recovery | In progress — controlled execution healing wired |
| M7 | Regression & Learning Intelligence | Planned |
| M8 | Defect Management & Autonomous CI/CD Quality Gate | Planned |

## Backend Capabilities

### Requirement intelligence
- business intent analysis
- acceptance criteria extraction
- OpenAI-compatible integration with deterministic fallback
- requirement document parsing for TXT, Markdown, DOCX and PDF

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
- common agent contract
- persisted AgentRun / AgentStep activity
- policy and governance services
- orchestration APIs and observability
- requirement → design → automation → execution → diagnosis → quality decision flow

### Failure and quality intelligence
- failure classification and diagnosis
- controlled retry / healing foundations
- conservative self-healing policy
- quality gate decision services

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

All product-facing API traffic is proxied by Nginx to the Spring Boot backend. Primary endpoints include:

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/pipeline/upload` | Upload requirement file and start a mission |
| GET | `/api/pipeline/runs` | Persisted mission history |
| GET | `/api/pipeline/stats` | Mission processing metrics |
| GET | `/api/pipeline/runs/{id}` | Mission detail and structured result |
| GET | `/api/pipeline/runs/{id}/test-cases.json` | JSON test/result export |
| GET | `/api/pipeline/runs/{id}/test-cases.xlsx` | Excel test export |
| POST | `/api/execution/run` | Deterministic Playwright execution |
| GET | `/api/execution/history` | Execution audit history |
| GET | `/api/execution/stats` | PASS/FAIL execution metrics |
| GET | `/api/execution/evidence/{file}` | Screenshot evidence |
| GET | `/api/applications?activeOnly=true` | Active UAT application targets |
| POST | `/api/applications` | Register UAT target |
| PATCH | `/api/applications/{id}/active?value=true` | Activate/deactivate target |
| GET | `/api/agent-activity/summary` | M5 orchestration summary |
| GET | `/api/agent-activity/runs?limit=20` | Recent AgentRun history |
| GET | `/api/agent-activity/runs/{runId}/steps` | Ordered AgentStep trace |
| POST | `/api/healing/evaluate` | M6 healing policy evaluation |
| GET | `/api/healing/history` | Persisted healing decisions |
| GET | `/api/healing/stats` | M6 healing metrics |
| POST | `/api/failure-analysis/analyze` | Failure diagnosis |
| POST | `/api/quality-gate/evaluate` | Deterministic release decision |
| POST | `/api/analytics/visit` | Anonymous page-view event |
| GET | `/api/analytics/stats` | Visitor/page-view summary |
| GET | `/api/analytics/recent` | Recent anonymous traffic trace |
| GET | `/actuator/health` | Deployment/application health |

Additional capability groups exist under `/api/knowledge/*` and `/api/rag/*` for project knowledge and retrieval-augmented reasoning.

## Demo UAT Fixture

`ai-qa-api/src/main/resources/static/uat/index.html` is intentionally retained as a small deterministic login target for local Playwright/UAT demonstrations. It is a test fixture, not the Auravis product UI.

## Technology Stack

Java 17+ • Spring Boot 3.5.x • Maven • Spring Data JPA • PostgreSQL 16 • pgvector-ready persistence • Playwright for Java • OpenAI-compatible AI integration • Apache POI • PDFBox • Docker / Docker Compose • GitHub Actions • GHCR • AWS EC2 • Nginx • HTTPS

Frontend technology lives in `stejas7/ai-qa-frontend`: React • TypeScript • Vite • React Router • TanStack Query.

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

The React frontend has its own CI/CD workflow and is deployed independently to the same EC2/Nginx environment.

## Repository Boundary

This backend repository should contain only backend services, persistence, APIs, automation/runtime code, tests, deployment configuration, architecture documentation and intentional test fixtures. Product UI code belongs in `ai-qa-frontend`.
