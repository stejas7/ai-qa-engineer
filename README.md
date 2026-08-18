# AURAVIS 2.0.0 — Autonomous AI UAT Engineer

> **From business requirement to release confidence — autonomously.**

Auravis is an Autonomous AI UAT Engineer designed to take end-to-end ownership of software quality engineering activities that traditionally require dedicated QA execution roles.

Its long-term objective is to enable engineering organizations to operate with **minimal dependency on manual QA execution** by autonomously understanding business requirements, retrieving project knowledge, designing test scenarios, generating automation, executing UAT, diagnosing failures, safely self-healing eligible automation issues, performing regression validation, raising evidence-backed defects in engineering tools, and producing quality decisions.

> **Auravis acts as an autonomous QA engineer inside the software delivery lifecycle — continuously understanding, testing, validating, diagnosing and protecting product quality with minimal human intervention.**

This is a learning and AI portfolio project. Capabilities are being implemented incrementally, with deterministic controls and explicit evidence rather than pretending unfinished roadmap items are production-ready.

---

## Product Mission

The user provides a business requirement / BRD / PRD / user story and a UAT environment or registered Auravis application target. Auravis then owns the QA workflow.

```text
Business Requirement / BRD / PRD
              +
          UAT Target
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
        Playwright Execution
              │
       ┌──────┴──────┐
       ▼             ▼
     PASS           FAIL
                       │
                       ▼
                Failure Diagnosis
                       │
                 ┌─────┴─────┐
                 ▼           ▼
          Automation issue  Genuine defect
                 │           │
                 ▼           ▼
          Safe Self-Healing  Defect Management
                 │           │
                 ▼           ▼
          Regression Re-run  Jira / GitHub Issues /
                             Azure DevOps
                 └─────┬─────┘
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
│ INTEGRATIONS                                                            │
│ Jira • GitHub Issues • Azure DevOps • CI/CD quality gates               │
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
| **M4** | Advanced Automation & Multi-App Support | ✅ Implemented | Registered UAT targets, deterministic browser actions, execution history and evidence |
| **M5** | Agentic Orchestration | 🔨 In progress | Specialized planning/execution/diagnosis agents coordinated inside one mission |
| **M6** | Self-Healing & Smart Recovery | Planned | Safe locator healing, controlled retries and evidence-based repair |
| **M7** | Regression & Learning Intelligence | Planned | Historical mission learning, failure patterns and targeted regression |
| **M8** | Defect Management & Autonomous CI/CD Quality Gate | Planned | Classify genuine defects, create evidence-backed Jira/GitHub/Azure DevOps work items, prevent duplicates, and return an autonomous release recommendation |

**Roadmap progress: 4 / 8 milestones complete (50%).**

### M8 — Defect Management & Tool Integration

Auravis will not create a defect simply because automation failed. M6/M7 intelligence first determines whether the failure is a recoverable automation problem, environment issue, flaky execution, or genuine product defect.

For genuine defects, M8 is planned to support a provider-based integration layer:

```text
DefectManagementProvider
        │
        ├── JiraDefectProvider
        ├── GitHubIssueProvider
        └── AzureDevOpsDefectProvider
```

A generated defect can include requirement traceability, test case, expected vs actual result, severity/priority, environment, screenshot/evidence, diagnostic summary and Auravis mission ID. Duplicate detection should run before creating a new work item.

---

## Current Capabilities

### Requirement intelligence
- business intent analysis
- acceptance criteria extraction
- OpenAI-compatible integration with deterministic fallback

### Knowledge / RAG foundation
- project knowledge persistence and retrieval
- PostgreSQL + pgvector-ready storage
- mission-time grounding before QA reasoning

### Intelligent test design
- functional, business-rule, negative, boundary and risk scenarios
- requirement traceability
- Excel and JSON download

### Automation & execution
- Java + Playwright automation generation
- deterministic browser action vocabulary and assertions
- PASS / FAIL results, screenshots and evidence
- persisted execution audit history
- multi-application UAT target registry

### Failure intelligence
- failure classification
- severity and probable cause
- recommendation and retry signal

### Delivery
- Maven verification
- Docker image build and GHCR publishing
- AWS EC2 deployment
- local/public health checks
- rollback on unhealthy deployment

---

## Primary UI

- `/technology.html` — Engineering Showcase / Technology Behind Auravis
- `/` — Auravis product overview and roadmap
- `/auravis.html` — start an autonomous UAT mission
- `/dashboard.html` — live mission dashboard and mission history
- `/execution-center.html` — application registry, execution metrics and evidence history
- `/real-world-impact.html` — real-world problem and future product impact

---

## Technology Stack

Java 17+ • Spring Boot 3.5.x • Maven • Spring Data JPA • PostgreSQL 16 • pgvector • Playwright for Java • OpenAI-compatible AI integration • Docker / Docker Compose • GitHub Actions • GHCR • AWS EC2 • Nginx + HTTPS

---

## Run Locally

```bash
git clone https://github.com/stejas7/ai-qa-engineer.git
cd ai-qa-engineer
docker compose up -d postgres
mvn clean verify
mvn spring-boot:run -pl ai-qa-api
```

---

## Deployment

```text
Commit to main → Maven verify → Docker image → GHCR → AWS EC2 → Health checks → UI smoke test → DEPLOYMENT SUCCESS
```

Live environment: `https://auravis-uat.duckdns.org`

---

## Learning Value

Auravis demonstrates an end-to-end Java → GenAI / Agentic AI engineering path:

```text
Java → Spring Boot → Structured AI Integration → RAG / Knowledge
→ Intelligent Test Design → Playwright Execution → Evidence / Audit
→ Agentic Orchestration → Self-Healing → Regression Intelligence
→ Defect Management Integrations → Autonomous CI/CD Quality Gate → AWS
```

Every capability should move Auravis closer to **autonomous, explainable and safe UAT quality ownership from a business requirement**.
