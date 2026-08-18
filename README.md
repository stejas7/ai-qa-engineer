# 🦂 Scorpion 2.0.0 — Autonomous AI QA Engineer

> **From business requirement to release confidence, autonomously.**

## Purpose & Vision

**Scorpion is an Autonomous AI QA Engineer designed to take end-to-end ownership of software quality engineering activities traditionally performed through dedicated QA execution roles.**

Its long-term objective is to enable engineering organizations to operate with **minimal dependency on dedicated manual QA execution** by autonomously understanding business requirements, designing test scenarios, generating automation, executing UAT, diagnosing failures, safely self-healing eligible automation issues, performing regression validation, and producing evidence-backed quality decisions.

> **Scorpion acts as an autonomous QA engineer within the software delivery lifecycle — continuously understanding, testing, validating, diagnosing, and protecting product quality with minimal human intervention.**

The goal is not merely test automation. Scorpion is being engineered toward **autonomous ownership of the QA lifecycle**, while retaining deterministic controls, evidence, auditability, and human approval boundaries for sensitive actions.

## Product Goal

The user provides a complete business requirement (BRD, PRD, user story, or specification) and the UAT environment. Scorpion owns the QA workflow from that point onward.

```text
Business Requirement / BRD / PRD
              +
          UAT URL
              │
              ▼
      ┌────────────────────┐
      │    🦂 SCORPION     │
      │ Autonomous AI QA   │
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

## Scorpion 2.0 Architecture

```text
┌─────────────────────────────────────────────────────────────────────────┐
│                         SCORPION 2.0.0                                  │
├─────────────────────────────────────────────────────────────────────────┤
│ EXPERIENCE                                                              │
│ Requirement upload • UAT URL • Mission status • Evidence • QA decision  │
├─────────────────────────────────────────────────────────────────────────┤
│ MISSION CONTROL                                                         │
│ ScorpionMission • Mission state • Autonomous orchestration              │
├─────────────────────────────────────────────────────────────────────────┤
│ AI INTELLIGENCE                                                         │
│ Requirement analysis • RAG • Test generation • Failure reasoning        │
├─────────────────────────────────────────────────────────────────────────┤
│ DETERMINISTIC EXECUTION                                                 │
│ Java services • Playwright • Policy boundaries • Evidence capture       │
├─────────────────────────────────────────────────────────────────────────┤
│ KNOWLEDGE                                                               │
│ PostgreSQL • pgvector-ready knowledge • requirements • history          │
├─────────────────────────────────────────────────────────────────────────┤
│ DELIVERY                                                                │
│ Maven • Docker • GHCR • GitHub Actions • AWS EC2 • HTTPS                │
└─────────────────────────────────────────────────────────────────────────┘
```

### Engineering Principle

> **AI understands, plans and diagnoses. Java controls state and policy. Playwright executes. Evidence proves what happened.**

Scorpion does not give an LLM unrestricted infrastructure access. Real-world actions remain behind deterministic application services and controlled tool boundaries.

## Scorpion 2.0 Roadmap

| Milestone | Capability | Status | Product outcome |
|---|---|---|---|
| **M1** | Autonomous QA Mission | ✅ Implemented | Requirement → tests → automation → UAT → diagnosis |
| **M2** | Knowledge / RAG | 🟡 In progress | Ground QA reasoning in project knowledge |
| **M3** | Intelligent Test Generation | 🔜 Next | Business-flow, negative, boundary, risk and traceability coverage |
| **M4** | Agentic Mission Orchestration | 🔜 Planned | Specialized AI capabilities coordinated automatically |
| **M5** | Safe Self-Healing | 🔜 Planned | Diagnose eligible automation failures, repair and re-run |
| **M6** | Learning & Regression Intelligence | 🔜 Planned | Learn from mission history to improve regression selection |
| **M7** | Autonomous CI/CD Quality Gate | 🔜 Planned | Execute Scorpion after deployment and produce release recommendation |
| **M8** | Enterprise Governance | 🔜 Planned | RBAC, audit, policy, isolation and provider controls |

## Current Capabilities

- Business requirement upload and pasted requirement input
- Autonomous mission state and stage progression
- Requirement intelligence and acceptance-scenario extraction
- Project knowledge persistence and retrieval
- Functional, negative and boundary test generation
- Java + Playwright automation generation
- UAT execution and evidence capture
- Automatic failure classification and diagnosis
- Business-readable QA decision
- Maven, Docker, GHCR, GitHub Actions and AWS EC2 delivery

## Technology Stack

- Java 17+
- Spring Boot 3.5.x
- Maven
- Spring Data JPA
- PostgreSQL 16
- pgvector-ready knowledge layer
- Playwright for Java
- OpenAI-compatible AI abstraction with deterministic fallback
- Docker / Docker Compose
- GitHub Actions + GHCR
- AWS EC2 + HTTPS

## Primary UI

- `/` — Scorpion 2.0 overview, architecture and roadmap
- `/scorpion.html` — start an autonomous QA mission
- `/dashboard.html` — mission execution dashboard, evidence and QA decisions

## Run Locally

```bash
git clone https://github.com/stejas7/ai-qa-engineer.git
cd ai-qa-engineer
docker compose up -d postgres
mvn clean verify
mvn spring-boot:run -pl ai-qa-api
```

Then open `http://localhost:8080/scorpion.html`.

## Delivery Flow

```text
Commit to main
      ↓
Maven verification
      ↓
Docker image
      ↓
GHCR
      ↓
AWS EC2 deployment
      ↓
Health checks
      ↓
Scorpion UI smoke test
      ↓
Deployment success
```

## Product Direction

Historical V1.x UI pages are removed from the active product. Existing Java requirement-analysis, test-design, automation, execution, and failure-analysis services remain because they are reusable internal capabilities of the Scorpion mission.

Every future feature must move Scorpion closer to **autonomous, explainable, evidence-backed and safe ownership of the software QA lifecycle** rather than simply adding more screens or disconnected AI features.

## Portfolio Note

Scorpion is a learning and portfolio-oriented reference implementation demonstrating an end-to-end Java → GenAI → Agentic AI engineering journey. Production adoption requires organization-specific identity, secrets management, security controls, compliance, isolation, governance and operational hardening.
