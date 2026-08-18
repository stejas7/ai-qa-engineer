# AURAVIS 2.0.0 — Autonomous AI UAT Engineer

> **From business requirement to release confidence — autonomously.**

Auravis is an Autonomous AI UAT Engineer designed to take end-to-end ownership of software quality engineering activities that traditionally require dedicated QA execution roles.

Its long-term objective is to enable engineering organizations to operate with **minimal dependency on manual QA execution** by autonomously understanding business requirements, retrieving project knowledge, designing test scenarios, generating automation, executing UAT, diagnosing failures, safely self-healing eligible automation issues, performing regression validation, and producing evidence-backed quality decisions.

> **Auravis acts as an autonomous QA engineer inside the software delivery lifecycle — continuously understanding, testing, validating, diagnosing and protecting product quality with minimal human intervention.**

The goal is not simply test automation. Auravis is being engineered toward **autonomous ownership of the QA lifecycle**, while retaining deterministic controls, evidence, auditability and explicit approval boundaries for sensitive actions.

---

## Product Mission

The user provides:

1. a complete business requirement / BRD / PRD / user story, and
2. a UAT environment URL.

Auravis then owns the QA workflow.

```text
Business Requirement / BRD / PRD
              +
          UAT URL
              │
              ▼
      ┌────────────────────┐
      │      AURAVIS       │
      │ Autonomous AI UAT  │
      └─────────┬──────────┘
                │
                ▼
       Knowledge Retrieval
                │
                ▼
    Requirement Intelligence
                │
                ▼
      Intelligent Test Design
                │
                ▼
      Automation Generation
                │
                ▼
          UAT Execution
                │
         ┌──────┴──────┐
         ▼             ▼
       PASS           FAIL
                         │
                         ▼
                  Failure Diagnosis
                         │
                         ▼
                   Safe Self-Healing
                         │
                         ▼
                   Regression Re-run
                         │
                         ▼
                  Final QA Decision
                         │
                         ▼
                 Evidence + Report
```

---

## Auravis 2.0 Architecture

```text
┌─────────────────────────────────────────────────────────────────────────┐
│                           AURAVIS 2.0.0                                 │
├─────────────────────────────────────────────────────────────────────────┤
│ EXPERIENCE                                                              │
│ Requirement upload • UAT URL • Mission status • Evidence • QA decision  │
├─────────────────────────────────────────────────────────────────────────┤
│ MISSION CONTROL                                                         │
│ Mission state • Autonomous orchestration • Retry / recovery              │
├─────────────────────────────────────────────────────────────────────────┤
│ AI INTELLIGENCE                                                         │
│ Requirement analysis • RAG • Test generation • Failure reasoning        │
├─────────────────────────────────────────────────────────────────────────┤
│ DETERMINISTIC EXECUTION                                                 │
│ Java services • Playwright • Policy boundaries • Evidence capture       │
├─────────────────────────────────────────────────────────────────────────┤
│ KNOWLEDGE                                                               │
│ PostgreSQL • pgvector-ready knowledge • requirements • mission history  │
├─────────────────────────────────────────────────────────────────────────┤
│ DELIVERY                                                                │
│ Maven • Docker • GHCR • GitHub Actions • AWS EC2 • HTTPS                │
└─────────────────────────────────────────────────────────────────────────┘
```

### Engineering principle

> **AI understands, plans and diagnoses. Java controls state and policy. Playwright executes. Evidence proves what happened.**

Auravis does not give an LLM unrestricted shell, filesystem, database or deployment access. Real-world actions stay behind deterministic application services and controlled tool boundaries.

---

## Autonomous UAT Flow

```text
Requirement
    ↓
Knowledge / RAG
    ↓
Requirement Intelligence
    ↓
Intelligent Test Generation
    ↓
Automation Generation
    ↓
UAT Execution
    ↓
Failure Diagnosis
    ↓
Safe Self-Healing
    ↓
Regression Re-run
    ↓
Final QA Decision
```

Target operating model:

- no manual test-case writing
- no manual automation coding
- no manual execution
- no manual result analysis
- minimal dependency on dedicated QA execution roles

---

## Auravis 2.0 Roadmap

| Milestone | Capability | Status | Outcome |
|---|---|---|---|
| **M1** | Autonomous Mission | Implemented | Requirement → tests → automation → UAT → diagnosis |
| **M2** | Knowledge / RAG Foundation | Implemented foundation | Project knowledge grounds QA reasoning |
| **M3** | Intelligent Test Generation | Next | Business-flow, risk, negative, boundary and traceability coverage |
| **M4** | Advanced Automation & Multi-App Support | Planned | Richer browser/API/mobile-ready execution model |
| **M5** | Agentic Orchestration | Planned | Specialized agents coordinated inside one mission |
| **M6** | Self-Healing & Smart Recovery | Planned | Safe locator healing, retries and evidence-based repair |
| **M7** | Regression & Learning Intelligence | Planned | Historical mission learning and targeted regression |
| **M8** | Autonomous CI/CD Quality Gate | Planned | Auravis validates releases and returns a delivery recommendation |

---

## Current Capabilities

### Requirement intelligence
- business intent analysis
- acceptance criteria extraction
- deterministic fallback when an external LLM key is unavailable

### Knowledge / RAG foundation
- project knowledge persistence
- knowledge retrieval service
- PostgreSQL + pgvector-ready storage
- mission-time grounding before QA reasoning

### Intelligent test design
- functional scenarios
- negative scenarios
- boundary scenarios
- requirement traceability

### Automation & execution
- Java + Playwright automation generation
- controlled browser execution
- PASS / FAIL result
- screenshots and evidence where available

### Failure intelligence
- failure classification
- severity
- probable cause
- recommendation
- retry signal

### Delivery
- Maven verification
- Docker image build
- GHCR publishing
- AWS EC2 deployment
- local/public health checks
- rollback on unhealthy deployment

---

## Primary UI

- `/` — Auravis product overview, architecture and roadmap
- `/auravis.html` — start an autonomous UAT mission
- `/dashboard.html` — live mission dashboard and mission history

The old `/scorpion.html` route remains only as a compatibility redirect to `/auravis.html`.

---

## Technology Stack

- Java 17+
- Spring Boot 3.5.x
- Maven
- Spring Data JPA
- PostgreSQL 16
- pgvector
- Playwright for Java
- OpenAI-compatible AI integration with deterministic fallback
- Docker / Docker Compose
- GitHub Actions
- GHCR
- AWS EC2
- Nginx + HTTPS

---

## Run Locally

```bash
git clone https://github.com/stejas7/ai-qa-engineer.git
cd ai-qa-engineer

docker compose up -d postgres
mvn clean verify
mvn spring-boot:run -pl ai-qa-api
```

Open:

```text
http://localhost:8080/auravis.html
```

Health:

```bash
curl http://localhost:8080/actuator/health
```

---

## Deployment

```text
Commit to main
      ↓
Maven verify
      ↓
Build exact Docker image
      ↓
Publish to GHCR
      ↓
AWS EC2 deployment
      ↓
Health checks
      ↓
Auravis UI smoke test
      ↓
DEPLOYMENT SUCCESS
```

Live environment:

```text
https://tejas-aiqa.duckdns.org
```

Auravis:

```text
https://tejas-aiqa.duckdns.org/auravis.html
```

---

## Branding Migration Note

The product identity is now **Auravis — Autonomous UAT Engineer**. Some internal Java package/class/API identifiers may still use the historical `scorpion` name temporarily for backward compatibility and migration safety. Product-facing UI, documentation and new development should use **Auravis**.

---

## Learning Value

Auravis demonstrates an end-to-end Java → GenAI / Agentic AI engineering path:

```text
Java
  ↓
Spring Boot
  ↓
Structured AI Integration
  ↓
RAG / Knowledge
  ↓
Agentic Orchestration
  ↓
Deterministic Tools
  ↓
Playwright UAT
  ↓
Evidence + Diagnosis
  ↓
Self-Healing
  ↓
CI/CD + AWS
```

Every capability should move Auravis closer to **autonomous, explainable and safe UAT quality ownership from a business requirement**.
