# AI QA Engineer 1.0.0 — Demo Guide

## Demo objective

Show one complete business journey:

```text
Requirement
  ↓
AI test design
  ↓
Automation
  ↓
Change impact
  ↓
Agent execution
  ↓
Playwright UAT
  ↓
Evidence
  ↓
Quality Gate
  ↓
CI/CD decision
```

## Recommended live scenario

> A registered user should be able to log in with valid credentials and reach the dashboard.

## Demo sequence

1. Present the business requirement.
2. Show the generated test intent and traceability.
3. Show the automation artifact.
4. Show the Git/PR impact assessment and recommended regression scope.
5. Start the agent run.
6. Show ordered `AgentStep` execution.
7. Execute the UAT journey through Playwright.
8. Show PASS/FAIL and captured evidence.
9. Show the governance decision for any sensitive action.
10. Show the Quality Gate decision.
11. Show the CI/CD result.
12. Open the deployed HTTPS environment.

## The failure-path demo

For a stronger architecture demonstration, use a controlled test change that causes a UAT failure.

Show:

```text
UAT failure
   ↓
Evidence
   ↓
Failure analysis
   ↓
Risk / recommendation
   ↓
Quality Gate
   ↓
BLOCKED
```

Do not claim autonomous repair unless the repair has actually been validated by a subsequent test run.

## Live environment

`https://tejas-aiqa.duckdns.org`

Health:

`https://tejas-aiqa.duckdns.org/actuator/health`

## Architecture statement

> AI plans and reasons. Deterministic tools execute. Governance controls autonomy. Evidence explains what happened. The Quality Gate decides whether delivery can continue.

## Demo rule

Prefer one complete, explainable workflow over a tour of every endpoint. The objective is to demonstrate an integrated Agentic AI Quality Engineering platform, not a collection of unrelated APIs.
