# AURAVIS 2.0.0 — Autonomous AI UAT Engineer

> **From business requirement to release confidence — autonomously.**

Auravis is an Autonomous AI UAT Engineer designed to take end-to-end ownership of software quality engineering activities that traditionally require dedicated QA execution roles.

Its long-term objective is to enable engineering organizations to operate with **minimal dependency on manual QA execution** by autonomously understanding business requirements, retrieving project knowledge, designing test scenarios, generating automation, executing UAT, diagnosing failures, safely self-healing eligible automation issues, performing regression validation, and producing evidence-backed quality decisions.

> **Auravis acts as an autonomous QA engineer inside the software delivery lifecycle — continuously understanding, testing, validating, diagnosing and protecting product quality with minimal human intervention.**

This is a learning and AI portfolio project. Capabilities are being implemented incrementally, with deterministic controls and explicit evidence rather than pretending unfinished roadmap items are production-ready.

---

## Product Mission

The user provides:

1. a complete business requirement / BRD / PRD / user story, and
2. a UAT environment or a registered Auravis application target.

Auravis then owns the QA workflow.

```text
Business Requirement / BRD / PRD
              +
          UAT Target
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
     Executable Test Planning
                │
                ▼
        Playwright Execution
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
│ Requirement upload • Application targets • Mission status • Evidence    │
├─────────────────────────────────────────────────────────────────────────┤
│ MISSION CONTROL                                                         │
│ Mission state • Autonomous orchestration • Retry / recovery              │
├─────────────────────────────────────────────────────────────────────────┤
│ AI INTELLIGENCE                                                         │
│ Requirement analysis • RAG • Test generation • Failure reasoning        │
├─────────────────────────────────────────────────────────────────────────┤
│ DETERMINISTIC EXECUTION                                                 │
│ Java services • Playwright • Allowed actions • Assertions • Evidence    │
├─────────────────────────────────────────────────────────────────────────┤
│ PERSISTENCE                                                             │
│ PostgreSQL • Knowledge • Applications • Missions • Execution history    │
├─────────────────────────────────────────────────────────────────────────┤
│ DELIVERY                                                                │
│ Maven • Docker • GHCR • GitHub Actions • AWS EC2 • Nginx • HTTPS        │
└─────────────────────────────────────────────────────────────────────────┘
```

### Engineering principle

> **AI understands, plans and diagnoses. Java controls state and policy. Playwright executes. Evidence proves what happened.**

Auravis does not give an LLM unrestricted shell, filesystem, database or deployment access. Real-world actions stay behind deterministic application services and controlled tool boundaries.

---

## Auravis 2.0 Roadmap

| Milestone | Capability | Status | Outcome |
|---|---|---|---|
| **M1** | Autonomous Mission | ✅ Implemented | Requirement → tests → automation → UAT → diagnosis |
| **M2** | Knowledge / RAG Foundation | ✅ Implemented foundation | Project knowledge grounds QA reasoning |
| **M3** | Intelligent Test Generation | ✅ Implemented | Business-rule, negative, boundary, risk and traceability coverage + Excel/JSON export |
| **M4** | Advanced Automation & Multi-App Support | ✅ Implemented | Registered UAT targets, richer deterministic browser actions, persisted execution history and evidence |
| **M5** | Agentic Orchestration | 🔨 Next | Specialized planning/execution/diagnosis agents coordinated inside one mission |
| **M6** | Self-Healing & Smart Recovery | Planned | Safe locator healing, retries and evidence-based repair |
| **M7** | Regression & Learning Intelligence | Planned | Historical mission learning and targeted regression |
| **M8** | Autonomous CI/CD Quality Gate | Planned | Auravis validates releases and returns a delivery recommendation |

**Roadmap progress: 4 / 8 milestones complete (50%).**

---

## M4 — Advanced Automation

M4 closes the gap between generated test cases and auditable UAT execution.

### Multi-application targets

Auravis can persist multiple UAT applications/environments instead of relying on one hard-coded URL.

```text
POST /api/applications
GET  /api/applications
```

Each target stores application name, base URL, environment, authentication type and active state. Credentials are intentionally not stored in the application-target record.

### Deterministic Playwright execution

The execution engine supports controlled browser operations such as:

- navigation
- labelled field entry
- button clicks
- select/dropdown actions
- checkbox actions
- text verification
- expected-result assertions

Unsupported actions fail explicitly rather than being guessed.

### Evidence and audit history

Each browser run persists:

- test case ID
- target URL
- PASS / FAIL
- duration
- diagnostic message
- screenshot evidence path
- execution timestamp

```text
GET /api/execution/history
GET /api/execution/stats
GET /api/execution/evidence/{file}
```

---

## Current Capabilities

### Requirement intelligence
- business intent analysis
- acceptance criteria extraction
- OpenAI-compatible integration with deterministic fallback

### Knowledge / RAG foundation
- project knowledge persistence
- retrieval service
- PostgreSQL + pgvector-ready storage
- mission-time grounding before QA reasoning

### Intelligent test design
- functional scenarios
- business-rule scenarios
- negative scenarios
- boundary scenarios
- risk-based scenarios
- requirement traceability
- Excel and JSON download

### Automation & execution
- Java + Playwright automation generation
- deterministic browser action vocabulary
- controlled assertions
- PASS / FAIL results
- screenshots and evidence
- persisted execution audit history
- multi-application UAT target registry

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
- `/execution-center.html` — M4 application registry, execution metrics and evidence history

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
http://localhost:8080/dashboard.html
http://localhost:8080/execution-center.html
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
https://auravis-uat.duckdns.org
```

Auravis:

```text
https://auravis-uat.duckdns.org/auravis.html
```

Execution Center:

```text
https://auravis-uat.duckdns.org/execution-center.html
```

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
Intelligent Test Design
  ↓
Deterministic Playwright Execution
  ↓
Evidence + Audit History
  ↓
Agentic Orchestration (M5)
  ↓
Self-Healing
  ↓
CI/CD + AWS
```

Every capability should move Auravis closer to **autonomous, explainable and safe UAT quality ownership from a business requirement**.
