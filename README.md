# AI UAT Engineer — Autonomous UAT Learning Project

> **From business requirement to release confidence — with AI-assisted UAT automation.**

This repository contains the **Java / Spring Boot backend** for **AI UAT Engineer**. The React UI lives in the separate repository `stejas7/ai-qa-frontend`.

AI UAT Engineer is a learning and AI-engineering portfolio project exploring how Java, Spring AI, RAG, agentic orchestration, deterministic browser automation, security, evidence, CI/CD and cloud deployment can work together around one end-to-end UAT problem.

> **Learning-project note:** this product is still evolving. There are many experiments, open questions and limitations, and it may not yet be suitable for direct use against real products. The goal is to keep learning, hardening the architecture and validating how far this approach can go.

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
Test Design
      ↓
Automation Generation
      ↓
Playwright Execution
      ↓
Durable Evidence + Traceability
      ↓
READY / BLOCKED Release Recommendation
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
      +--> Spring AI / OpenAI
      +--> RAG / knowledge retrieval
      +--> Agent orchestration
      +--> Deterministic Java policy
      +--> Playwright execution
      +--> PostgreSQL persistence
      +--> Evidence / traceability / reporting
```

## Engineering Principle

> **AI reasons. Java controls state and policy. Playwright executes. Evidence supports the release decision.**

## Current Technology Stack

Java 21 • Spring Boot 3.5.x • Spring Security • Spring AI 1.1.8 • OpenAI API • PostgreSQL 16 • pgvector-ready RAG • Playwright for Java • JUnit 5 • Maven • Docker / Docker Compose • GitHub Actions • GHCR • AWS EC2 • Nginx • HTTPS

Frontend: React • TypeScript • Vite • React Router • TanStack Query.

## Roadmap Status

| Milestone | Capability | Status |
|---|---|---|
| M1–M7 | Core autonomous UAT foundation | ✅ Complete |
| M8 | Reliable Autonomous UAT Operations | ✅ Implemented / hardening continues |
| M9 | RAG / AI orchestration evolution | ✅ Complete |
| M10 | Autonomous workflow improvements | ✅ Complete |
| M11 | Performance & load-testing foundation | ✅ Complete |
| M12 | Automation-script management | ✅ Complete |
| M13 | Test management & traceability | ✅ Complete |
| M14–M16 | Company, product, users & security foundation | ✅ Complete |
| M17 | Secure credential profiles | ✅ Core complete |
| M18 | Tenant authorization & product scope | ✅ Core complete |
| M19 | One-click authenticated autonomous UAT | 🔨 Stabilization / E2E validation |
| M20.1–M20.3 | Platform Owner + company/product/user reporting | ✅ Complete |
| M20.4–M20.8 | Platform operations monitoring & audit/report drill-down | ⏳ Planned after M19 is green |

## v3.2.0 Stabilization Gates

```text
Deployment GREEN
      ↓
M19 E2E GREEN
      ↓
Platform Admin Login GREEN
      ↓
Google / GitHub SSO GREEN
      ↓
M20.4–M20.8
      ↓
Self-UAT + Load + Security Testing
      ↓
4.0 Stable
```

No unrelated feature expansion should jump ahead of a failed stabilization gate.

## Authentication & Tenant Model

- the first registered company user becomes `COMPANY_ADMIN`
- `COMPANY_ADMIN` can manage users and product environments
- `QA_MANAGER` can manage/run UAT within the company
- `TESTER` can execute authorized UAT
- `VIEWER` has results/evidence access only
- `PLATFORM_ADMIN` is reserved for read-only cross-company platform oversight
- tenant identity and registered target URL are resolved server-side
- cross-company UAT execution is denied
- runtime credentials are referenced and resolved only at execution time
- raw credential values are not persisted in result JSON or exposed through reporting APIs

## Authenticated M19 Flow

```text
Authenticated User
      ↓
Authorized Company
      ↓
Registered Product Target
      ↓
Runtime Credential Readiness
      ↓
Credential Resolution (in memory only)
      ↓
Requirement → Tests → Automation
      ↓
Playwright Login / Execution
      ↓
Evidence
      ↓
Quality Gate
      ↓
READY / BLOCKED
```

`USERNAME_PASSWORD` and `API_TOKEN` runtime paths are supported. `OAUTH_CLIENT` intentionally fails closed until an explicit token flow is configured.

## Spring AI Runtime

Spring AI 1.1.8 is used as the Java-native model integration layer. Runtime configuration:

```text
OPENAI_API_KEY=<secret>
OPENAI_MODEL=gpt-4.1-mini
```

When a real model credential is unavailable or a provider call fails, deterministic Java fallback keeps supported flows operational. Deployment verifies both `/actuator/health` and `/api/ai/runtime` before success.

Safe runtime metadata:

```text
GET /api/ai/runtime
```

The endpoint exposes framework/model configuration metadata but never exposes the API key.

## Core Capabilities

### Requirement intelligence
- TXT, Markdown, DOCX and PDF input
- business intent and acceptance-criteria analysis
- AI-assisted scenario generation with deterministic fallback

### Knowledge / RAG
- persisted project knowledge
- PostgreSQL-backed retrieval
- product-context grounding

### Test design & automation
- functional, negative, boundary and risk scenarios
- automation candidate generation
- deterministic Playwright execution
- registered product/environment targets

### Evidence & quality decisions
- persisted execution history
- durable evidence support
- failure diagnosis and bounded healing
- release-quality gate with READY / BLOCKED outcome

### Multi-tenant product foundation
- company registration
- company users and roles
- registered product environments
- secure runtime credential references
- tenant-scoped UAT launch

### Platform Owner reporting
- read-only company directory
- product/environment overview
- user/role overview
- platform-level reporting without password hashes, target secrets or credential values

## Selected API Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/auth/register` | Register company and first Company Admin |
| POST | `/api/auth/login` | Sign in |
| GET | `/api/auth/me` | Current authenticated user |
| GET | `/api/company/products` | Tenant product environments |
| POST | `/api/company/uat/upload` | Tenant-safe M19 UAT launch |
| GET | `/api/pipeline/runs` | Persisted UAT runs |
| GET | `/api/pipeline/runs/{id}` | Structured run result |
| GET | `/api/execution/evidence/{file}` | Execution evidence |
| GET | `/api/ai/runtime` | Spring AI runtime metadata |
| GET | `/api/platform/companies` | Platform Owner company reporting |
| GET | `/api/platform/products` | Platform Owner product reporting |
| GET | `/api/platform/users` | Platform Owner user reporting |
| GET | `/actuator/health` | Application health |

Additional capability groups exist under `/api/knowledge/*`, `/api/rag/*`, `/api/performance/*`, `/api/automation-scripts/*` and `/api/test-management/*`.

## Testing

Maven verification includes JUnit coverage for tenant authorization, credential readiness, pipeline behavior, quality gates, agent policy, healing policy and other backend logic.

M19 stabilization is adding controlled browser-level Playwright coverage for:

```text
runtime credential
  → login page
  → authenticated content
  → execution evidence
  → persisted pipeline result
  → READY / BLOCKED release gate
```

The CI runner installs Chromium specifically for controlled browser E2E verification.

## Demo UAT Fixture

`ai-qa-api/src/main/resources/static/uat/index.html` is retained as a deterministic browser/UAT fixture. It is a test target, not the AI UAT Engineer product UI.

## Run Backend Locally

```bash
git clone https://github.com/stejas7/ai-qa-engineer.git
cd ai-qa-engineer
docker compose up -d postgres
mvn clean verify
mvn spring-boot:run -pl ai-qa-api
```

To enable a real AI model locally:

```bash
export OPENAI_API_KEY=<your-key>
export OPENAI_MODEL=gpt-4.1-mini
```

## Platform Admin Bootstrap

The first Platform Owner account is provisioned from runtime environment variables; values are never committed to Git:

```text
AI_UAT_PLATFORM_ADMIN_EMAIL
AI_UAT_PLATFORM_ADMIN_PASSWORD
```

## Deployment

```text
Commit to main
  → Maven verify
  → immutable Docker image
  → GHCR
  → AWS EC2
  → local health
  → Spring AI runtime check
  → public health / contract checks
  → deployment success
```

The public base URL is deployment configuration and is intentionally not part of the product brand.
