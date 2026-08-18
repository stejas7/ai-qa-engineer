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
| M5 | Agentic Orchestration | In progress |
| M6 | Self-Healing & Smart Recovery | Planned |
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

### Failure and quality intelligence
- failure classification and diagnosis
- controlled retry / healing foundations
- quality gate decision services

### Product analytics
- anonymous React page-view tracking
- PostgreSQL persistence
- unique visitor and daily traffic summaries
- top-page and recent anonymous visit trace APIs
- raw IP addresses are not stored

## API Surface

Main API groups include:

```text
/api/pipeline/*
/api/applications/*
/api/execution/*
/api/agent-activity/*
/api/analytics/*
/api/knowledge/*
/api/rag/*
/api/quality-gate/*
```

Spring Boot health endpoint:

```text
/actuator/health
```

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
