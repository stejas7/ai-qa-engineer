# AI QA Engineer — Agentic Java + GenAI Platform

A build-first reference implementation for exploring how **Java, Spring Boot, GenAI and AI Agents** can be combined into an enterprise automation platform.

## Vision

The goal is to evolve from a requirement-aware assistant into a production-oriented **agentic engineering platform** that can understand business requirements, plan work, use tools, execute actions, analyze outcomes and safely improve its own automation.

## Efficient Architecture

```text
                         ┌──────────────────────────────┐
                         │        User / Business       │
                         │        Requirement           │
                         └──────────────┬───────────────┘
                                        │
                                        ▼
                         ┌──────────────────────────────┐
                         │      Agent Orchestrator      │
                         │  planning • routing • state  │
                         └──────────────┬───────────────┘
                                        │
              ┌─────────────────────────┼─────────────────────────┐
              ▼                         ▼                         ▼
     ┌─────────────────┐      ┌─────────────────┐       ┌─────────────────┐
     │ Requirement     │      │ Test Design     │       │ Automation      │
     │ Agent           │      │ Agent           │       │ Agent           │
     └────────┬────────┘      └────────┬────────┘       └────────┬────────┘
              │                        │                         │
              └────────────────────────┼─────────────────────────┘
                                       ▼
                         ┌──────────────────────────────┐
                         │        Tool Gateway          │
                         │ Playwright • Git • CI/CD     │
                         │ DB • Jira • APIs • Cloud     │
                         └──────────────┬───────────────┘
                                        ▼
                         ┌──────────────────────────────┐
                         │       Execution Engine       │
                         │       UAT / CI / Cloud       │
                         └──────────────┬───────────────┘
                                        │
                          ┌─────────────┴─────────────┐
                          ▼                           ▼
                 ┌─────────────────┐        ┌─────────────────┐
                 │ Evidence Store  │        │ Failure /       │
                 │ logs • traces   │        │ Analysis Agent  │
                 │ screenshots     │        └────────┬────────┘
                 └─────────────────┘                 │
                                                     ▼
                                          ┌─────────────────────┐
                                          │ Safe Self-Healing   │
                                          │ + Human Approval    │
                                          └─────────────────────┘

  PostgreSQL / pgvector → state • metadata • knowledge • embeddings
  OpenAI / LLM            → reasoning • generation • classification
  Spring Boot             → APIs • orchestration • security
  Docker                  → reproducible runtime
  GitHub Actions          → CI/CD and autonomous regression
```

### Architecture principles

- **Orchestrator-first:** agents are specialized workers; orchestration owns workflow state and routing.
- **Tool isolation:** agents access Git, browser, database, CI/CD and external systems through controlled tools rather than unrestricted access.
- **Structured outputs:** use typed request/response models and machine-readable agent results instead of free-form text between services.
- **Deterministic execution:** LLMs plan and reason; execution tools perform the actual actions.
- **Evidence-first:** every execution produces logs, screenshots, traces and structured results.
- **Safe self-healing:** generated fixes are validated and can require human approval before changing production automation.
- **Provider-neutral design:** LLM integration should remain replaceable so the platform is not tightly coupled to one model provider.

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
| **V8** | Multi-Agent + Tool Orchestration | Introduce specialized agents and controlled tools for Git, Jira, APIs, databases, CI/CD and environments. |
| **V9** | Autonomous CI/CD + Cloud | Trigger pipelines, provision UAT, execute regression suites, publish evidence and integrate cloud deployment. |
| **V10** | Production-Ready Agentic Engineering Platform | Secure, observable, policy-controlled multi-agent platform with human-in-the-loop governance and enterprise integrations. |

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
- Supports natural-language steps for the demo login flow

### V5 — Failure Analysis Agent
V5 analyzes failed V4 executions.

- Dashboard: `/v5.html`
- API: `POST /api/failure-analysis/analyze`
- Deterministic fallback works without an AI key
- Optional OpenAI Responses API analysis when `OPENAI_API_KEY` is configured
- Classifies failures as application/requirement, automation/application, environment/performance, test data, or unknown
- Recommends retry or investigation

## Technology Stack

- **Java 21**
- **Spring Boot 3.5.3**
- **Maven**
- **PostgreSQL 16**
- **Playwright for Java 1.52.0**
- **OpenAI Responses API**
- **Docker / Docker Compose**
- **GitHub Actions**
- Planned: **pgvector, RAG, MCP/tool integrations, cloud deployment and observability**

## Run in GitHub Codespaces

```bash
git checkout main
git pull origin main
docker compose up -d postgres
mvn clean verify
mvn -pl ai-qa-api exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
mvn spring-boot:run -pl ai-qa-api
```

Keep the Spring Boot terminal running. Open the **forwarded Codespaces port 8080**, not `localhost:8080` in your local browser.

Then open:

- `/v4.html` — UAT execution
- `/v5.html` — execution + failure analysis
- `/uat/` — demo UAT application
- `/actuator/health` — application health

### Passing V4/V5 demo

Use:

- URL: `/uat/`
- Email: `test@example.com`
- Password: `Password123`
- Expected: `Verify Welcome Test User`

### Failure-analysis demo

Use the same steps but change the expected result to:

`Verify Welcome Tejas`

The UAT application returns `Welcome Test User`, so the test fails and V5 analyzes the failure.

## APIs

### V3
`POST /api/automation/generate`

### V4
`POST /api/execution/run`

### V5
`POST /api/failure-analysis/analyze`

## Project Direction

This repository is intentionally being developed incrementally. V1–V5 establish the core requirement → design → automation → execution → analysis loop. V6–V10 focus on **safe autonomy, enterprise knowledge, tool orchestration, CI/CD, cloud deployment, security, observability and production readiness**.

The long-term objective is not simply an AI chatbot. It is a **controlled agentic engineering system** where AI handles planning and reasoning while deterministic tools execute actions with auditable evidence and human governance.
