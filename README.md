# AI QA Engineer — Agentic AI Quality Engineering Platform

> **Business requirements in. Executable UAT out. Evidence-backed quality decisions all the way to CI/CD.**

AI QA Engineer is a build-first reference implementation for combining **Java, Spring Boot, GenAI, Playwright, RAG and multi-agent orchestration** into a production-oriented AI Quality Engineering platform.

The project is intentionally designed to demonstrate the architecture and engineering practices expected from a **Java → GenAI / Agentic AI Architect**: deterministic services execute actions, AI agents reason and plan, policy controls autonomy, every important action produces evidence, and CI/CD consumes a measurable quality decision.

---

## ⭐ The product vision

```text
                 BUSINESS REQUIREMENT
                          │
                          ▼
                ┌──────────────────┐
                │ Requirement Agent │
                └────────┬─────────┘
                         ▼
                  Test Design Agent
                         │
                         ▼
                Automation Generator
                         │
                         ▼
                 Real UAT Execution
                         │
             ┌───────────┴───────────┐
             ▼                       ▼
          Evidence              Failure Analysis
             │                       │
             └───────────┬───────────┘
                         ▼
                  Agent Orchestrator
                         │
                  Policy + Approval
                         │
                         ▼
                   Quality Gate
                         │
                 APPROVED / BLOCKED
                         │
                         ▼
                       CI/CD
                         │
                         ▼
                    AWS Deployment
```

### The core principle

**AI decides what should happen; deterministic software decides how it is executed.**

That separation makes the platform safer, testable, observable and explainable.

---

## 🏆 Why this project is different

This is not only an AI test-case generator and not only a Playwright wrapper.

The platform combines five layers:

1. **Quality engineering** — requirements, test design, automation, execution and evidence.
2. **Agentic engineering** — persisted `AgentRun` / `AgentStep` state and an orchestrator for specialized agents.
3. **Governance** — deterministic policies, sensitive-action controls and human approval.
4. **Autonomous quality** — a measurable quality gate that can approve or block a deployment.
5. **Cloud delivery** — GitHub Actions, Docker, AWS EC2 and HTTPS UAT.

The result is a practical reference architecture for **controlled AI autonomy in software delivery**.

---

## 🚀 Current release status

| Milestone | Capability | Status |
|---|---|---|
| V1 | Requirement Analysis Agent | ✅ |
| V2 | Test Design Agent | ✅ |
| V3 | Automation Generation | ✅ |
| V4 | Real Playwright UAT Execution | ✅ |
| V5 | AI Failure Analysis | ✅ |
| V6 | Safe Self-Healing foundation | ✅ |
| V7 | RAG + Enterprise Knowledge | ✅ |
| V8 | Multi-Agent + Tool Orchestration | ✅ |
| V9 | Autonomous CI/CD + AWS Cloud | ✅ **Deployed baseline** |
| V10 | Production Agent Governance | ✅ |
| V11 | Real UAT → Quality Gate → CI/CD | ✅ **Current milestone** |
| V12 | Next-generation autonomous QE | 🔜 Planned |

### V11 is now a real deployment flow

The current GitHub Actions deployment path is:

```text
Push to main
   ↓
Build
   ↓
Safe Docker recovery / cleanup
   ↓
Deploy to AWS EC2
   ↓
Application health check
   ↓
Real UAT execution through Playwright
   ↓
Collect PASS / FAIL + evidence
   ↓
V11 Quality Gate
   ↓
APPROVED → deployment pipeline succeeds
BLOCKED  → deployment pipeline fails
```

Current HTTPS UAT environment:

**https://tejas-aiqa.duckdns.org**

---

# 🧠 Architecture

```text
┌──────────────────────────────────────────────────────────────┐
│                       AI QA ENGINEER                         │
├──────────────────────────────────────────────────────────────┤
│ Presentation / API                                           │
│  REST Controllers • Dashboards • Actuator                    │
├──────────────────────────────────────────────────────────────┤
│ Agentic Layer                                                │
│  AgentOrchestrator • AgentRun • AgentStep • AgentPipeline   │
├──────────────────────────────────────────────────────────────┤
│ Specialized AI Capabilities                                  │
│  Requirement • Test Design • Automation • RAG • Analysis    │
├──────────────────────────────────────────────────────────────┤
│ Deterministic Tool Layer                                     │
│  Playwright • Persistence • Reporting • CI/CD                │
├──────────────────────────────────────────────────────────────┤
│ Governance                                                   │
│  Policy Engine • Approval • Audit                            │
├──────────────────────────────────────────────────────────────┤
│ Quality Decision                                             │
│  UAT Evidence → Quality Gate → APPROVED / BLOCKED           │
├──────────────────────────────────────────────────────────────┤
│ Delivery                                                     │
│  GitHub Actions → Docker → AWS EC2 → Nginx → HTTPS          │
└──────────────────────────────────────────────────────────────┘
```

## Architecture principles

- **Orchestrator-first:** specialized agents participate in a persisted workflow.
- **Tool isolation:** external systems are accessed through controlled contracts.
- **Structured outputs:** APIs return typed, machine-readable data rather than agent prose.
- **Deterministic execution:** LLMs reason and plan; deterministic tools perform actions.
- **Evidence-first:** execution produces auditable results and screenshots.
- **Safe autonomy:** sensitive or destructive actions can require approval.
- **Provider-neutral:** the AI provider can evolve without redesigning the platform.
- **Cloud-ready:** deployment is reproducible through Docker and GitHub Actions.
- **Quality-gated delivery:** deployment is a consequence of measurable quality, not merely a successful build.

---

# 🤖 Agentic workflow

A persisted agent run follows the lifecycle:

```text
CREATED → RUNNING → COMPLETED
                 ↘ FAILED
```

Each run contains ordered steps such as:

```text
REQUIREMENT_ANALYSIS
        ↓
TEST_DESIGN
        ↓
AUTOMATION_GENERATION
        ↓
EXECUTION
        ↓
FAILURE_ANALYSIS
        ↓
QUALITY_GATE
```

This makes an AI workflow inspectable rather than a black box.

---

# 🛡️ V10 — Production Agent Governance

V10 places a deterministic safety boundary between agent reasoning and external actions.

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

Possible decisions:

- `ALLOW` — permitted by deterministic policy.
- `APPROVAL_REQUIRED` — human approval is required.
- `DENY` — no policy permits the action.

Sensitive tools such as shell/SSH/secrets and destructive production actions are not silently trusted.

### Human approval APIs

- `POST /api/governance/approvals`
- `GET /api/governance/approvals/pending`
- `GET /api/governance/runs/{runId}/approvals`
- `POST /api/governance/approvals/{approvalId}/decision`

Approval requests are persisted and can be associated with an `AgentRun`.

---

# ✅ V11 — Real UAT + Autonomous Quality Gate

V11 turns UAT results into a deployment decision.

### Execution

`POST /api/execution/run`

The execution service launches Playwright, navigates to the target URL, performs supported business steps, verifies expected results and captures evidence.

### Quality gate

`POST /api/quality-gate/evaluate`

Example:

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

The gate calculates:

- pass rate
- automation rate
- requirement coverage
- final deployment decision

A clean execution with complete requirement coverage returns `APPROVED`; failures or incomplete coverage return `BLOCKED`.

### Why the gate matters

A conventional pipeline answers:

> **Did the build succeed?**

AI QA Engineer answers:

> **Is the delivered change sufficiently validated against business requirements to proceed?**

That is the central V11 product capability.

---

# 📚 Version history

| Version | Capability | Engineering outcome |
|---|---|---|
| V1 | Requirement Analysis | Convert business requirements into structured scenarios. |
| V2 | Test Design | Generate functional, negative, boundary and traceability tests. |
| V3 | Automation Generation | Generate Java + Playwright automation skeletons. |
| V4 | UAT Execution | Execute real browser tests and capture evidence. |
| V5 | Failure Analysis | Classify failures and recommend next actions. |
| V6 | Safe Self-Healing | Generate candidate automation fixes with validation/approval. |
| V7 | RAG | Ground agent reasoning in enterprise QA knowledge. |
| V8 | Multi-Agent Orchestration | Persist agent runs, steps and controlled tool interactions. |
| V9 | Cloud CI/CD | Build, deploy and validate on AWS EC2 through GitHub Actions. |
| V10 | Governance | Policy, approval and audit boundary for agent actions. |
| V11 | Quality Gate | Real UAT results control the CI/CD decision. |

---

# 🔌 Key APIs

| Area | Endpoint | Purpose |
|---|---|---|
| Automation | `POST /api/automation/generate` | Generate automation artifacts. |
| Execution | `POST /api/execution/run` | Execute Playwright UAT. |
| Failure | `POST /api/failure-analysis/analyze` | Analyze execution failures. |
| Agents | `POST /api/agents/pipeline` | Run requirement → design → automation pipeline. |
| Agents | `GET /api/agents/runs` | List agent runs. |
| Agents | `GET /api/agents/runs/{id}` | Inspect one run. |
| Agents | `GET /api/agents/runs/{id}/steps` | Inspect ordered agent steps. |
| Governance | `POST /api/governance/policy/evaluate` | Evaluate an agent action. |
| Governance | `POST /api/governance/approvals` | Create approval request. |
| Governance | `GET /api/governance/approvals/pending` | List pending approvals. |
| Governance | `POST /api/governance/approvals/{approvalId}/decision` | Approve/reject an action. |
| Quality | `POST /api/quality-gate/evaluate` | Produce APPROVED/BLOCKED quality decision. |

---

# 🧪 Testing strategy

The project uses a layered testing strategy:

```text
Unit tests
   ↓
Spring integration tests
   ↓
Playwright UAT
   ↓
Evidence validation
   ↓
Quality Gate
   ↓
CI/CD deployment decision
```

JUnit/Spring tests validate deterministic business logic. Playwright validates the actual user journey. The quality gate validates the resulting release decision.

---

# 🏗️ Technology stack

- **Java 17+ / Java 21 development target**
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

# 🚀 Run locally

### Prerequisites

- JDK 17+
- Maven 3.9+
- Docker / Docker Compose
- Node/Playwright browser dependencies as required by the local execution environment

### Start the application

```bash
mvn clean test
```

Then start the services with Docker Compose:

```bash
docker compose up -d --build
```

Health check:

```text
http://localhost:8080/actuator/health
```

Expected:

```json
{"status":"UP"}
```

---

# ☁️ AWS deployment

The production-style deployment path is intentionally simple and reproducible:

```text
GitHub main
    ↓
GitHub Actions
    ↓
SSH to EC2
    ↓
Safe Docker cleanup/recovery
    ↓
Docker Compose build/deploy
    ↓
Spring Boot health check
    ↓
Public HTTPS UAT
    ↓
Real Playwright tests
    ↓
Quality Gate
```

Persistent Docker volumes are deliberately protected during cleanup.

---

# 📈 V12 direction

V12 is intentionally **not** a random feature dump. The next milestone should make the platform more autonomous while preserving the V10 governance boundary.

Priority candidates:

1. **Requirement → generated UAT suite** instead of a fixed CI smoke suite.
2. **Persisted execution results** feeding the Quality Gate directly.
3. **AI failure classification** integrated into the gate.
4. **Safe self-healing loop:** failure → classify → repair candidate → validate → approval when required → re-test.
5. **Intelligent test selection** from Git diff / requirement impact.
6. **API + UI UAT** under one orchestration model.
7. **AI application evaluation** for correctness, grounding, hallucination and safety.
8. **MCP/tool gateway integration** for developer and CI agents.

The goal is not to claim autonomous behavior prematurely. Each V12 capability should be measurable, auditable and protected by policy.

---

# 🎯 What this project demonstrates

This project is designed to demonstrate end-to-end engineering capability across:

**Java → Spring Boot → GenAI → RAG → Agents → Playwright → UAT → Governance → Docker → GitHub Actions → AWS → Quality Engineering.**

The strongest architectural statement is:

> **AI plans and reasons. Deterministic tools execute. Governance controls autonomy. Evidence explains what happened. The Quality Gate decides whether delivery can continue.**

---

## Repository

**AI QA Engineer:** https://github.com/stejas7/ai-qa-engineer

**Live UAT:** https://tejas-aiqa.duckdns.org

---

## License

This repository is a learning and portfolio-oriented reference implementation. Adapt it to your organization's security, compliance and operational requirements before production use.
