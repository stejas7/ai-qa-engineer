# 🦂 Scorpion 2.0.0 — Autonomous UAT QA Agent

> **Business requirement in. Autonomous UAT QA out.**

Scorpion is a Java + Spring Boot agentic QA platform that takes an end-to-end business requirement and a UAT environment, then automatically understands the requirement, grounds itself in project knowledge, creates test scenarios, generates Playwright automation, executes UAT, diagnoses failures and produces a business-readable QA decision.

The product is now intentionally centered on **one autonomous Scorpion mission**. Historical V1.x pages and the 1.0.0 product identity are no longer part of the active product experience.

---

## Product goal

The user should provide only:

1. a business requirement / BRD / PRD / user story, and
2. a UAT environment URL.

Then Scorpion owns the QA workflow.

```text
Business Requirement / BRD / PRD
              +
          UAT URL
              │
              ▼
      ┌──────────────────┐
      │    🦂 SCORPION   │
      │ Autonomous UAT QA│
      └────────┬─────────┘
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

# Scorpion 2.0 architecture

```text
┌─────────────────────────────────────────────────────────────────────────┐
│                         SCORPION 2.0.0                                  │
├─────────────────────────────────────────────────────────────────────────┤
│ EXPERIENCE                                                              │
│ Requirement upload / paste • UAT URL • Mission status • Final decision  │
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

### Architecture principle

> **AI understands, plans and diagnoses. Java controls state and policy. Playwright executes. Evidence proves what happened.**

Scorpion does not give an LLM unrestricted shell, filesystem, database or deployment access. Real-world actions stay behind deterministic application services and controlled tool boundaries.

---

# Current autonomous mission flow

```text
ScorpionMission
      │
      ├── Retrieve relevant project knowledge
      │
      ├── Analyze complete business requirement
      │
      ├── Generate functional / negative / boundary scenarios
      │
      ├── Generate Playwright Java automation
      │
      ├── Execute against UAT
      │
      ├── Capture execution result + evidence
      │
      ├── Diagnose failures automatically
      │
      └── Produce final QA decision
```

The user does **not** manually navigate separate requirement, test-design, automation, execution and failure-analysis pages. Those capabilities remain reusable Java services inside the Scorpion mission.

---

# Scorpion 2.0 roadmap

The roadmap is capability-based rather than a collection of separate product pages.

| Milestone | Capability | Status | Product outcome |
|---|---|---|---|
| **M1** | Autonomous QA Mission | ✅ Implemented | One requirement → test design → automation → execution → diagnosis |
| **M2** | Knowledge / RAG | 🟡 In progress | Scorpion retrieves project knowledge before making QA decisions |
| **M3** | Intelligent Test Generation | 🔜 Next | Better business-flow, negative, boundary, risk and traceability coverage |
| **M4** | Agentic Mission Orchestration | 🔜 Planned | Specialized capabilities coordinated automatically by mission state |
| **M5** | Safe Self-Healing | 🔜 Planned | Diagnose automation failures, propose safe repairs and re-run |
| **M6** | Learning / Regression Intelligence | 🔜 Planned | Use historical missions and failures to improve future QA selection |
| **M7** | Autonomous CI/CD Quality Gate | 🔜 Planned | Scorpion runs after deployment and produces a delivery recommendation |
| **M8** | Enterprise Governance | 🔜 Planned | RBAC, policy, audit, multi-project isolation and provider controls |

### Target product journey

```text
Upload Requirement
      ↓
Scorpion Mission
      ↓
Ground with Knowledge
      ↓
Generate Test Intelligence
      ↓
Generate Automation
      ↓
Execute UAT
      ↓
Diagnose Failure
      ↓
Self-Heal when safe
      ↓
Regression Re-run
      ↓
Final QA Report
      ↓
CI/CD Quality Decision
```

---

# Implemented capabilities

### Autonomous mission

- `ScorpionMission` persistence
- one-click mission API
- requirement file upload (`.txt`, `.md`, `.docx`, `.pdf`)
- pasted requirement input
- UAT URL input
- automatic stage progression
- mission status and final decision

### Requirement intelligence

- business intent analysis
- acceptance/test scenario extraction
- deterministic fallback when an external LLM key is not configured

### Knowledge foundation

- knowledge document persistence
- project knowledge retrieval service
- knowledge REST API
- PostgreSQL configured with pgvector image and vector extension
- Scorpion mission retrieves relevant knowledge before requirement analysis

> Current retrieval is deliberately deterministic/lexical. The architecture is ready for a real embedding provider and vector retriever without changing mission orchestration.

### Intelligent test design

- functional scenarios
- negative scenarios
- boundary scenarios
- traceability coverage

### Automation and UAT

- Java + Playwright automation generation
- deterministic browser execution
- PASS / FAIL result
- screenshots / execution evidence where available

### Failure intelligence

- automatic failure classification
- severity / probable cause
- recommendation
- retry signal

### Delivery

- Maven verification
- Docker image based on Playwright runtime
- GHCR image publishing
- AWS EC2 deployment
- health checks and rollback
- public Scorpion UI smoke check

---

# Technology stack

- **Java 17+** (project currently compiles against Java 17 for runtime compatibility)
- Spring Boot 3.5.x
- Maven
- Spring Data JPA
- PostgreSQL 16
- pgvector
- Playwright for Java
- OpenAI-compatible AI integration with deterministic fallback
- Docker / Docker Compose
- GitHub Actions
- GitHub Container Registry (GHCR)
- AWS EC2
- Nginx + HTTPS

---

# Primary UI

### Scorpion mission

```text
/scorpion.html
```

This is the main user experience: upload or paste a requirement, provide UAT URL, and start autonomous QA.

### Pipeline dashboard

```text
/dashboard.html
```

Used to observe uploaded requirement pipeline runs, stage progression, generated tests, automation, execution results and quality information.

### Overview

```text
/
```

Shows the Scorpion 2.0 product architecture and roadmap.

---

# Core Scorpion API

### Start from pasted requirement

```http
POST /api/scorpion/missions
```

```json
{
  "title": "Customer checkout UAT",
  "requirement": "A customer can search a product, add it to cart, pay and receive an order confirmation.",
  "uatUrl": "https://uat.example.com"
}
```

### Start from uploaded requirement

```http
POST /api/scorpion/missions/upload
Content-Type: multipart/form-data
```

Fields:

- `file`
- `title`
- `uatUrl`

### Mission history

```http
GET /api/scorpion/missions
GET /api/scorpion/missions/{id}
```

### Knowledge

```http
POST /api/knowledge/documents
GET  /api/knowledge/documents
POST /api/knowledge/search
```

---

# Run locally

```bash
git clone https://github.com/stejas7/ai-qa-engineer.git
cd ai-qa-engineer

docker compose up -d postgres
mvn clean verify
mvn spring-boot:run -pl ai-qa-api
```

Health:

```bash
curl http://localhost:8080/actuator/health
```

Then open:

```text
http://localhost:8080/scorpion.html
```

For the full Docker runtime:

```bash
docker compose up -d
```

---

# Deployment

```text
Commit to main
      ↓
AI QA Engineer Build
      ↓
Maven verify
      ↓
Build exact Docker image
      ↓
Publish image to GHCR
      ↓
AWS deployment workflow
      ↓
EC2 pulls tested image
      ↓
Docker Compose restart
      ↓
Local health check
      ↓
Public health check
      ↓
/scorpion.html smoke check
      ↓
DEPLOYMENT SUCCESS
```

The deployment workflow is intentionally separate from future Scorpion QA/quality-gate workflows so an application deployment is not incorrectly marked failed by an unrelated QA stage.

---

# What was removed from the active 2.0 product

The old static version pages were removed from `main`:

- `v2.html`
- `v3.html`
- `v4.html`
- `v5.html`
- `v8.html`
- `v9.html`

The underlying Java services for requirement analysis, test design, automation, execution and failure analysis are **not legacy waste**; they are reused as internal Scorpion capabilities and therefore remain intentionally.

Old 1.0 branding and old V1–V12 UI navigation should no longer appear in the active Scorpion 2.0 experience.

---

# Learning value

Scorpion is designed to demonstrate an end-to-end Java → GenAI / Agentic AI engineering journey:

```text
Java
  ↓
Spring Boot
  ↓
Structured AI integration
  ↓
RAG / Knowledge
  ↓
Agentic orchestration
  ↓
Deterministic tools
  ↓
Playwright UAT
  ↓
Evidence + diagnosis
  ↓
Self-healing
  ↓
CI/CD + AWS
```

The purpose is not to add AI features for their own sake. Every capability must help Scorpion move closer to **autonomous, explainable and safe UAT QA from a business requirement**.

---

## Repository & demo

Repository: https://github.com/stejas7/ai-qa-engineer  
Live environment: https://tejas-aiqa.duckdns.org  
Scorpion: https://tejas-aiqa.duckdns.org/scorpion.html

---

## License / portfolio note

This is a learning and portfolio-oriented reference implementation. Production adoption would require organization-specific identity, secrets management, security controls, compliance, resource isolation and operational hardening.
