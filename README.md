# AI UAT Engineer — Autonomous UAT Learning Project

> **From business requirement to release confidence — powered by a virtual workforce of 100 specialized AI agents.**

This repository contains the **Java / Spring Boot backend** for **AI UAT Engineer**. The React UI lives in the separate repository `stejas7/ai-qa-frontend`.

AI UAT Engineer is a learning and AI-engineering portfolio project exploring how Java, Spring AI, RAG, agentic orchestration, deterministic browser automation, security, evidence, CI/CD and cloud deployment can work together around one end-to-end UAT problem.

> **Learning-project note:** this product is still evolving. The 100-agent workforce is an architectural capability catalog: agents are not all executed at once. The orchestrator activates only the smallest useful tenant-safe subset for each mission.

## 100-Agent AI Workforce

The platform now exposes a canonical catalog of **100 specialized AI UAT workers** covering orchestration, requirements, test design, automation, execution, diagnostics, quality, knowledge/RAG, integrations, governance and platform intelligence.

```text
Requirement
   ↓
Chief UAT Orchestrator
   ↓
Dynamic mission-team selection
   ↓
Relevant specialists only (typically a bounded subset)
   ↓
Analysis → Test Design → Automation → Execution
   ↓
Diagnostics / Healing → Evidence → Quality / Governance
   ↓
Human-controlled Release Decision
```

Key rule: **100 agents available does not mean 100 agents run concurrently.** Mission composition is capability-driven and bounded; the backend currently caps an individual selected mission team at 20 agents.

Workforce endpoint groups:

```text
GET /api/agent-workforce/catalog
GET /api/agent-workforce/plan
POST /api/intelligence/risk-score
```

### Workforce organization

| Agent range | Team |
|---|---|
| 1–5 | Orchestration |
| 6–10 | Requirements |
| 11–15 | Test Design |
| 16–20 | Automation |
| 21–25 | Execution |
| 26–30 | Diagnostics / Healing |
| 31–35 | Quality / Performance / Security |
| 36–40 | Knowledge / RAG / Traceability |
| 41–45 | Integrations |
| 46–50 | Governance / Release / Self-UAT |
| 51–55 | Advanced Requirements Intelligence |
| 56–60 | Advanced Test Design |
| 61–65 | Advanced Automation |
| 66–70 | Advanced Execution |
| 71–75 | Advanced Diagnostics |
| 76–80 | Advanced Quality Intelligence |
| 81–85 | Advanced Knowledge / Learning |
| 86–90 | Advanced Integrations |
| 91–95 | Advanced Governance / Security |
| 96–100 | Platform Intelligence / Workforce Direction |

## Product Flow

```text
Register Company
      ↓
Create UAT Team
      ↓
Register Product / Environment
      ↓
Configure Secure Runtime Access
      ↓
Upload Requirement
      ↓
Requirement Analysis + RAG Context
      ↓
Risk / Change / Regression Intelligence
      ↓
Dynamic AI Workforce Selection
      ↓
Test Design
      ↓
Automation Generation
      ↓
Playwright Execution
      ↓
Durable Evidence + Traceability
      ↓
Release Governance
      ↓
READY / BLOCKED / Human Approval
```

## Architecture

```text
React / TypeScript
      |
      | HTTPS / JSON
      v
Nginx on AWS EC2
      |
      | /api/*
      v
Spring Boot 3.5
      |
      +--> Spring Security / tenant authorization
      +--> 100-Agent Workforce Catalog / mission planner
      +--> Spring AI / OpenAI
      +--> RAG / knowledge retrieval
      +--> Deterministic Java policy
      +--> Playwright execution
      +--> PostgreSQL persistence
      +--> Evidence / traceability / reporting
      +--> Release governance / platform diagnostics
```

## Engineering Principle

> **AI reasons. Java controls state and policy. Playwright executes. Evidence supports the release decision. Humans retain release authority.**

## Current Technology Stack

Java 21 • Spring Boot 3.5.x • Spring Security • Spring AI 1.1.8 • OpenAI API • PostgreSQL 16 • pgvector-ready RAG • Playwright for Java • JUnit 5 • Maven • Docker / Docker Compose • GitHub Actions • GHCR • AWS EC2 • Nginx • HTTPS

Frontend: React • TypeScript • Vite • React Router • TanStack Query.

## Roadmap Status

| Milestone | Capability | Status |
|---|---|---|
| M1–M20 | Core UAT, RAG, automation, execution, multi-tenant foundation | ✅ Implemented / stabilized progressively |
| M21 | Super Admin, Company Admin evolution, external API/OAuth foundation, password recovery | ✅ Implemented |
| M22–M24 | Authorization hardening, security audit, tenant governance | ✅ Implemented |
| M25–M27 | Integrations, webhook delivery, operational readiness | ✅ Implemented |
| M28–M30 | Release approval, platform analytics, enterprise readiness | ✅ Implemented |
| M31–M34 | 100-agent workforce foundation, risk/change/flaky/regression intelligence | ✅ Foundation merged |
| M35–M38 | Enterprise integrations expansion | 🔨 Next workforce phase |
| M39–M42 | Governance & compliance expansion | 🔨 Planned / branch prepared |
| M43–M46 | Scale, queue, recovery, observability | ⏳ Planned |
| M47–M50 | Multi-agent release planning, prediction, learning, self-UAT | ⏳ Planned |

## Authentication & Tenant Model

- first registered company user becomes `COMPANY_ADMIN`
- multiple Company Admins are supported with last-active-admin safeguards
- `QA_MANAGER`, `TESTER` and `VIEWER` retain bounded tenant roles
- `SUPER_ADMIN` is supported with legacy `PLATFORM_ADMIN` compatibility
- tenant identity and registered target URL are resolved server-side
- cross-company UAT execution is denied
- workforce selection must remain tenant-safe
- runtime credentials are referenced and resolved only at execution time
- raw credential values, API secrets and password reset tokens are never returned in reporting payloads

### SSO / OAuth2

Google and GitHub OAuth2 login are supported when the corresponding client credentials are configured. SSO is deliberately **existing-user only**: a successful identity-provider login must map to an already registered, active AI UAT Engineer user and never creates a company or tenant implicitly.

```text
Browser → /oauth2/authorization/{provider}
        → Google / GitHub authentication
        → verified email resolution
        → existing active application-user lookup
        → role + tenant session mapping
        → /account
```

GitHub accounts that do not expose an email in the default user-info response use the provider's verified-email endpoint as a fallback. The application records security-safe SSO diagnostics for provider, success/failure reason, mapped user ID, company ID and role. Access tokens, client secrets, passwords and raw OAuth credentials are never written to these SSO logs.

Useful checks:

```text
GET /api/auth/sso/providers
GET /oauth2/authorization/google
GET /oauth2/authorization/github
```

## Core Capabilities

- Requirement intelligence and change-impact analysis
- Risk-based prioritization and smart regression selection
- RAG-grounded product knowledge
- 100-agent virtual UAT workforce catalog
- Dynamic bounded mission-team selection
- Functional/negative/boundary/data test design
- UI/API automation generation and Playwright execution
- Failure classification, flaky-test intelligence and bounded healing
- Evidence, traceability and release-quality decisions
- Multi-tenant company/product/user/security model
- Existing-user Google/GitHub SSO with verified-email mapping and safe audit logging
- Forgot-password single-use reset-token flow
- Super Admin diagnostics and platform analytics
- Tenant governance limits and durable metadata-only security audit
- Tenant HTTPS integrations and webhook delivery history
- Release approval workflow and enterprise-readiness gates

## Selected API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/auth/register` | Register company and first Company Admin |
| POST | `/api/auth/login` | Sign in |
| GET | `/api/auth/me` | Current authenticated user |
| GET | `/api/auth/sso/providers` | Discover configured SSO providers |
| GET | `/oauth2/authorization/{provider}` | Start Google/GitHub OAuth2 login |
| POST | `/api/auth/password/forgot` | Request password reset |
| POST | `/api/auth/password/reset` | Complete password reset |
| GET | `/api/company/products` | Tenant product environments |
| POST | `/api/company/uat/upload` | Tenant-safe UAT launch |
| GET | `/api/pipeline/runs` | Persisted UAT runs |
| GET | `/api/execution/evidence/{file}` | Execution evidence |
| GET | `/api/agent-workforce/catalog` | 100-agent workforce catalog |
| GET | `/api/agent-workforce/plan` | Select bounded mission workforce |
| POST | `/api/intelligence/risk-score` | Deterministic risk scoring |
| GET | `/api/platform/diagnostics` | Platform health/usage snapshot |
| GET | `/api/platform/enterprise-readiness` | Enterprise readiness gate |
| GET | `/actuator/health` | Application health |

## Deployment

```text
Commit to main
  → backend/frontend validation
  → immutable deployment artifacts
  → AWS EC2
  → health + login + contract checks
  → deployment success
```

Feature work can move rapidly, but production deployment remains controlled from `main`. The public base URL is deployment configuration and is intentionally not part of the product brand.
