# AI QA Engineer 1.0.0 — Agentic AI Quality Engineering Platform

> **Business requirements in. Executable UAT out. Evidence-backed quality decisions all the way to CI/CD.**

AI QA Engineer is an **Agentic AI Quality Engineering Platform** that connects business requirements, intelligent test engineering, deterministic browser execution, agent orchestration, governance, evidence, quality decisions and cloud delivery in one continuous engineering workflow.

The platform is designed to demonstrate the architecture expected from a **Java → GenAI / Agentic AI Architect**:

> **AI plans and reasons. Deterministic tools execute. Governance controls autonomy. Evidence explains what happened. The Quality Gate decides whether delivery can continue.**

---

## 🚀 Version 1.0.0 Stable

This release consolidates the platform capabilities developed through the project's engineering milestones into **one product architecture and one stable product identity**.

The historical V1–V12 milestones remain part of the Git engineering history; they are no longer separate product versions. **AI QA Engineer 1.0.0 is the product.**

### The complete product flow

```text
                         BUSINESS REQUIREMENT
                                  │
                                  ▼
                     ┌────────────────────────┐
                     │ Intelligent Test       │
                     │ Engineering            │
                     │                        │
                     │ Requirement → Test     │
                     │ Test → Automation      │
                     └───────────┬────────────┘
                                 │
                                 ▼
                     ┌────────────────────────┐
                     │ Agentic Orchestration   │
                     │                        │
                     │ AgentRun / AgentStep   │
                     │ Specialized Agents     │
                     └───────────┬────────────┘
                                 │
                                 ▼
                     ┌────────────────────────┐
                     │ Quality Intelligence    │
                     │                        │
                     │ Impact Analysis        │
                     │ UAT Selection          │
                     │ Failure Analysis       │
                     └───────────┬────────────┘
                                 │
                                 ▼
                     ┌────────────────────────┐
                     │ Deterministic Execution │
                     │                        │
                     │ Playwright / API       │
                     │ Evidence / Results     │
                     └───────────┬────────────┘
                                 │
                                 ▼
                     ┌────────────────────────┐
                     │ Governed Autonomy       │
                     │                        │
                     │ Policy • Approval      │
                     │ Audit • Tool Controls  │
                     └───────────┬────────────┘
                                 │
                                 ▼
                     ┌────────────────────────┐
                     │ Autonomous Quality Gate │
                     │                        │
                     │ APPROVED / BLOCKED     │
                     └───────────┬────────────┘
                                 │
                                 ▼
                     ┌────────────────────────┐
                     │ Continuous Delivery     │
                     │                        │
                     │ GitHub Actions         │
                     │ Docker → AWS → HTTPS   │
                     └────────────────────────┘
```

---

# ⭐ Why AI QA Engineer?

This is not simply an AI test-case generator and not simply a Playwright wrapper.

It combines six product capabilities:

### 1. Intelligent Test Engineering

- Business requirement understanding
- Functional, negative and boundary test design
- Requirement/test traceability
- Automation generation
- UAT scenario generation

### 2. Agentic Execution

- Persisted `AgentRun` and `AgentStep` state
- Agent orchestration
- Specialized agent pipelines
- Controlled tool execution
- Real browser UAT through Playwright
- Evidence capture

### 3. Quality Intelligence

- Git/PR change-impact analysis
- Risk classification
- Regression recommendations
- Failure analysis
- Requirement coverage
- Execution evidence

### 4. Governed Autonomy

- Deterministic policy engine
- `ALLOW`, `APPROVAL_REQUIRED`, `DENY`
- Human approval workflow
- Auditability
- Protection around sensitive and destructive actions

### 5. Autonomous Quality Gate

Real UAT results become a measurable release decision:

```text
UAT execution
     ↓
PASS / FAIL + evidence
     ↓
Coverage + automation + pass-rate evaluation
     ↓
QUALITY GATE
     ↓
APPROVED / BLOCKED
```

### 6. Continuous Delivery

```text
Git push
   ↓
GitHub Actions
   ↓
Docker recovery / cleanup
   ↓
AWS EC2 deployment
   ↓
Application health
   ↓
Real HTTPS UAT
   ↓
Quality Gate
   ↓
Release decision
```

---

# 🧠 Architecture

```text
┌─────────────────────────────────────────────────────────────────┐
│                         AI QA ENGINEER 1.0.0                    │
├─────────────────────────────────────────────────────────────────┤
│ PRODUCT EXPERIENCE                                              │
│ Requirements • Tests • Automation • Agent Runs • Quality        │
├─────────────────────────────────────────────────────────────────┤
│ INTELLIGENT ENGINEERING                                         │
│ Requirement Analysis • Test Design • Automation • RAG           │
├─────────────────────────────────────────────────────────────────┤
│ AGENTIC ORCHESTRATION                                           │
│ AgentOrchestrator • AgentRun • AgentStep • AgentPipeline        │
├─────────────────────────────────────────────────────────────────┤
│ QUALITY INTELLIGENCE                                            │
│ Git/PR Impact • Regression Selection • Failure Analysis          │
├─────────────────────────────────────────────────────────────────┤
│ DETERMINISTIC TOOLS                                             │
│ Playwright • API Execution • Persistence • Evidence              │
├─────────────────────────────────────────────────────────────────┤
│ GOVERNANCE                                                      │
│ Policy Engine • Approval • Audit • Tool Boundaries               │
├─────────────────────────────────────────────────────────────────┤
│ QUALITY DECISION                                                │
│ UAT Evidence → Quality Gate → APPROVED / BLOCKED                │
├─────────────────────────────────────────────────────────────────┤
│ DELIVERY                                                        │
│ GitHub Actions → Docker → AWS EC2 → Nginx → HTTPS               │
└─────────────────────────────────────────────────────────────────┘
```

## Architectural principles

- **AI/deterministic separation:** models reason and plan; deterministic services execute.
- **Orchestrator-first:** important agent workflows are persisted and inspectable.
- **Tool isolation:** external actions go through controlled service contracts.
- **Structured outputs:** APIs return machine-readable results instead of relying on agent prose.
- **Evidence-first:** UAT produces evidence that can be inspected after execution.
- **Safe autonomy:** sensitive actions can require human approval.
- **Provider-neutral:** AI providers can evolve without redesigning core execution.
- **Quality-gated delivery:** deployment depends on validation, not merely compilation.
- **Cloud-ready:** Docker and GitHub Actions make deployment reproducible.

---

# 🔄 Golden Demo Journey

The recommended 1.0.0 demonstration is one complete business journey rather than a collection of disconnected APIs.

### Example requirement

> **A registered user should be able to log in with valid credentials and reach the dashboard.**

### Platform execution

```text
1. Requirement received
        ↓
2. AI understands business intent
        ↓
3. UAT scenarios generated
        ↓
4. Automation generated
        ↓
5. Git/PR impact assessed
        ↓
6. Relevant UAT selected
        ↓
7. Agent orchestrates execution
        ↓
8. Playwright executes the user journey
        ↓
9. Evidence captured
        ↓
10. Result analyzed
        ↓
11. Governance evaluates sensitive actions
        ↓
12. Quality Gate evaluates release readiness
        ↓
13. APPROVED / BLOCKED
        ↓
14. CI/CD continues or stops
```

The key product question is no longer simply:

> **Did the build succeed?**

It is:

> **Is this change sufficiently validated against business intent to proceed?**

---

# 🛡️ Governed Agentic AI

The platform deliberately avoids unrestricted AI autonomy.

### Policy decisions

`POST /api/governance/policy/evaluate`

Example:

```json
{
  "action": "DEPLOY",
  "tool": "CI_CD",
  "environment": "PRODUCTION"
}
```

Possible outcomes:

- `ALLOW`
- `APPROVAL_REQUIRED`
- `DENY`

### Approval workflow

```text
Agent proposes action
        ↓
Policy Engine
   ┌────┼─────────┐
   ▼    ▼         ▼
 ALLOW APPROVAL  DENY
        │
        ▼
 Human decision
        │
        ▼
     Continue
```

Approval APIs:

- `POST /api/governance/approvals`
- `GET /api/governance/approvals/pending`
- `GET /api/governance/runs/{runId}/approvals`
- `POST /api/governance/approvals/{approvalId}/decision`

---

# 🔎 Change Impact Intelligence

The platform can analyze Git/PR changes and produce an explainable impact assessment before regression execution.

```text
Git diff
   ↓
Impact Analysis
   ↓
Risk score
   ↓
Impacted areas
   ↓
Recommended suites
   ↓
Targeted regression / full regression
```

This creates the foundation for intelligent regression rather than blindly running the entire test estate after every change.

---

# 🧪 Real UAT Execution

`POST /api/execution/run`

The execution service uses Playwright to perform supported business steps against a real application, verify expected outcomes and capture evidence.

Typical execution lifecycle:

```text
Create test
   ↓
Launch browser
   ↓
Navigate
   ↓
Execute business steps
   ↓
Verify expected result
   ↓
Capture evidence
   ↓
PASS / FAIL
```

---

# 🚦 Quality Gate

`POST /api/quality-gate/evaluate`

Example input:

```json
{
  "totalTests": 10,
  "passedTests": 10,
  "failedTests": 0,
  "automatedTests": 10,
  "requirements": 4,
  "coveredRequirements": 4
}
```

The gate evaluates:

- pass rate
- automation rate
- requirement coverage
- failed tests
- final release decision

```text
                         UAT RESULTS
                              │
                              ▼
                    ┌──────────────────┐
                    │   QUALITY GATE   │
                    └────────┬─────────┘
                             │
                   ┌─────────┴─────────┐
                   ▼                   ▼
               APPROVED             BLOCKED
                   │                   │
                   ▼                   ▼
                CI/CD             Stop release
```

---

# 🔌 Core API Surface

| Capability | Endpoint | Purpose |
|---|---|---|
| Automation | `POST /api/automation/generate` | Generate automation artifacts. |
| Execution | `POST /api/execution/run` | Execute real UAT. |
| Failure Intelligence | `POST /api/failure-analysis/analyze` | Analyze execution failures. |
| Agent Pipeline | `POST /api/agents/pipeline` | Run the agent workflow. |
| Agent Runs | `GET /api/agents/runs` | List agent runs. |
| Agent Run | `GET /api/agents/runs/{id}` | Inspect a run. |
| Agent Steps | `GET /api/agents/runs/{id}/steps` | Inspect ordered steps. |
| Impact Analysis | `POST /api/impact-analysis/analyze` | Analyze Git/PR impact. |
| Policy | `POST /api/governance/policy/evaluate` | Evaluate an agent action. |
| Approvals | `POST /api/governance/approvals` | Create an approval request. |
| Pending Approvals | `GET /api/governance/approvals/pending` | List pending approvals. |
| Approval Decision | `POST /api/governance/approvals/{approvalId}/decision` | Approve/reject an action. |
| Quality Gate | `POST /api/quality-gate/evaluate` | Produce release decision. |

---

# ☁️ Deployment Architecture

```text
                    GitHub main
                         │
                         ▼
                  GitHub Actions
                         │
                         ▼
               Build / Test / Package
                         │
                         ▼
               Docker recovery/cleanup
                         │
                         ▼
                      AWS EC2
                         │
                         ▼
                   Docker Compose
                         │
                         ▼
                    Spring Boot
                         │
                         ▼
                       Nginx
                         │
                         ▼
                  HTTPS / DuckDNS
                         │
                         ▼
                  Real UAT execution
                         │
                         ▼
                    Quality Gate
```

Persistent Docker volumes are protected during cleanup.

### Live demo environment

**HTTPS:** `https://tejas-aiqa.duckdns.org`

Health endpoint:

`https://tejas-aiqa.duckdns.org/actuator/health`

---

# 🧪 Testing Strategy

```text
Unit Tests
    ↓
Spring Integration Tests
    ↓
Agent Workflow Tests
    ↓
Playwright UAT
    ↓
Evidence Validation
    ↓
Quality Gate
    ↓
CI/CD Deployment Decision
```

Deterministic business logic is covered by Java/Spring tests. Real user journeys are validated through Playwright. The Quality Gate validates the release decision produced from execution results.

---

# 🏗️ Technology Stack

- **Java 17+**
- Spring Boot 3.5.3
- Maven
- PostgreSQL 16
- Playwright for Java 1.52.0
- OpenAI Responses API
- Docker / Docker Compose
- GitHub Actions
- AWS EC2
- Nginx
- DuckDNS + HTTPS

---

# 🚀 Local Development

### Prerequisites

- JDK 17+
- Maven 3.9+
- Docker / Docker Compose
- Browser dependencies required by Playwright

### Validate the codebase

```bash
mvn clean test
```

### Start services

```bash
docker compose up -d --build
```

### Health check

```text
http://localhost:8080/actuator/health
```

Expected:

```json
{"status":"UP"}
```

---

# 📦 1.0.0 Release Philosophy

**1.0.0 means product coherence, not feature exhaustion.**

The stable release intentionally favors:

- deterministic execution over uncontrolled autonomy
- explainable decisions over opaque scores
- evidence over claims
- governed actions over unrestricted agents
- reproducible deployment over manual operations
- a complete demo journey over dozens of disconnected features

Future capabilities can extend the platform without changing the 1.0.0 architectural foundation.

---

# 🗺️ Post-1.0 Evolution

The product is intentionally not branded by internal development milestones anymore.

Future releases may add capabilities such as:

- richer requirement-to-UAT generation
- persisted execution intelligence
- deeper AI failure classification
- policy-controlled self-healing
- intelligent regression selection
- API + UI quality journeys
- GenAI application evaluation
- MCP/tool integrations
- advanced quality analytics

These are **future product evolution**, not missing pieces that prevent the 1.0.0 identity from being coherent.

---

# 🎯 What 1.0.0 Demonstrates

**Java → Spring Boot → GenAI → RAG → Agents → Playwright → UAT → Governance → Git/PR Intelligence → Docker → GitHub Actions → AWS → Quality Engineering.**

The platform demonstrates how AI can participate in software quality and delivery without turning production automation into an uncontrolled black box.

> **AI plans and reasons. Deterministic tools execute. Governance controls autonomy. Evidence explains what happened. The Quality Gate decides whether delivery can continue.**

---

# 📌 Repository & Demo

**Repository:** https://github.com/stejas7/ai-qa-engineer  
**Live HTTPS UAT:** https://tejas-aiqa.duckdns.org

---

## License

This repository is a portfolio-oriented reference implementation. Adapt security, compliance, identity, secrets management and operational controls to your organization's requirements before production adoption.
