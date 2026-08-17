# AI QA Engineer — V5

A build-first demo of an AI-driven QA/UAT automation platform.

## Current flow

Business Requirement → AI Requirement Analysis → Test Design → Playwright Automation → UAT Execution → Evidence → AI Failure Analysis.

## V1 — Requirement Agent
Business Requirement → AI Requirement Analysis → structured test scenarios → PostgreSQL → dashboard.

## V2 — Test Design Agent
Business Requirement → AI Requirement Analysis → detailed executable test cases.

## V3 — Playwright Automation Agent
Test case → automation request → generated Java + Playwright test skeleton.

## V4 — UAT Execution Agent
V4 adds a deliberately simple demo UAT login application and a real Playwright execution endpoint.

- Demo UAT: `/uat/`
- Execution dashboard: `/v4.html`
- API: `POST /api/execution/run`
- Captures execution duration and screenshot evidence
- Supports natural-language steps for the demo login flow

## V5 — Failure Analysis Agent
V5 automatically sends failed V4 executions to a failure-analysis agent.

- Dashboard: `/v5.html`
- API: `POST /api/failure-analysis/analyze`
- Deterministic fallback works without an AI key
- Optional OpenAI Responses API analysis when `OPENAI_API_KEY` is configured
- Classifies failures as application/requirement, automation/application, environment/performance, test data, or unknown
- Recommends retry or investigation

## Stack
- Java 21
- Spring Boot 3.5.3
- Maven
- PostgreSQL 16
- Playwright for Java 1.52.0
- OpenAI Responses API
- Docker / Docker Compose
- GitHub Actions

## Run in GitHub Codespaces

```bash
git checkout main
git pull origin main
docker compose up -d postgres
mvn clean verify
mvn -q exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
mvn spring-boot:run -pl ai-qa-api
```

Keep the Spring Boot terminal running. Open the forwarded Codespaces port 8080, not `localhost:8080` in your local browser.

Then open:
- `/v4.html` for a passing execution
- `/v5.html` for execution + failure analysis
- `/uat/` for the demo UAT app
- `/actuator/health` for health

### Passing V4/V5 demo

Use:
- URL: `/uat/`
- Email: `test@example.com`
- Password: `Password123`
- Expected: `Verify Welcome Test User`

### Failure-analysis demo

Use the same steps but change Expected result to:
`Verify Welcome Tejas`

The UAT app returns `Welcome Test User`, so the test fails and V5 analyzes the failure.

## APIs

### V3
`POST /api/automation/generate`

### V4
`POST /api/execution/run`

### V5
`POST /api/failure-analysis/analyze`

## Roadmap
V6 → Safe Self-Healing
V7 → RAG with pgvector
V8 → Jira/Git/CI tools
V9 → Full CI/CD and cloud deployment
