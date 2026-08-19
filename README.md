# AURAVIS 3.0.0-SNAPSHOT — Autonomous AI UAT Engineer Backend

> **From business requirement to release confidence — autonomously.**

This repository contains the Java / Spring Boot backend for Auravis. The React UI lives in `stejas7/ai-qa-frontend`.

## Roadmap

| Milestone | Capability | Status |
|---|---|---|
| M1-M6 | Core autonomous UAT, RAG, orchestration, execution and controlled healing | ✅ Implemented |
| M7 | Spring AI Runtime + Regression & Learning Intelligence | 🔨 In progress |
| M8 | Reliable Autonomous UAT Operations: CI/CD diagnosis, health/runtime checks, rollback and recovery | 🔨 Active |
| M9 | Company & Product Workspace: company registration, product ownership and isolation | 🔨 Active |
| M10 | UAT Session Lifecycle: create and track company/product scoped UAT sessions | 🔨 Started |
| 3.1.0 | Stable Release | 🎯 Target: 21 August 2026 |

## M8 — Reliable Autonomous UAT Operations

M8 keeps deployments diagnosable and recoverable through deterministic failure classification, EC2 preflight checks, immutable images, local health verification, Spring AI runtime verification, public smoke checks and verified rollback behavior.

## M9 — Company & Product Workspace

M9 introduces the enterprise workspace boundary:

```text
Company -> Products / UAT Targets
```

Companies can be registered and activated/deactivated. Products can optionally belong to a company, can be queried by company, and cannot be registered under an inactive company.

## M10 — UAT Session Lifecycle

M10 starts the execution workspace layer:

```text
Company -> Product -> UAT Session -> Agent Execution -> Evidence / Decision
```

The first implementation phase keeps session lifecycle deterministic and auditable. Authentication/authorization enforcement is intentionally handled as a separate controlled security step rather than being mixed into the session foundation.

## Engineering Principle

> **Spring AI understands and reasons. Java controls state and policy. Playwright executes. Evidence proves what happened.**

## Deployment

```text
Commit to main
  -> Maven verify
  -> Docker image
  -> GHCR
  -> AWS EC2
  -> /actuator/health
  -> /api/ai/runtime
  -> public API smoke checks
  -> deployment success
```

Live environment: `https://auravis-uat.duckdns.org`
