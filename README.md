# AI QA Engineer — V3

A build-first demo of an AI-driven QA/UAT automation platform.

## V1
Business Requirement → AI Requirement Analysis → structured test scenarios → PostgreSQL → dashboard.

## V2 — Test Design Agent
Business Requirement → AI Requirement Analysis → detailed executable test cases.

## V3 — Playwright Automation Agent
Test case → automation request → generated Java + Playwright test skeleton.

V3 adds:
- Playwright Java dependency
- Automation generation API: `POST /api/automation/generate`
- Generated JUnit 5 + Playwright test class
- UAT base URL input
- Step-to-code mapping placeholders for the next AI locator phase
- Dedicated V3 browser dashboard

## Stack
- Java 21
- Spring Boot 3.5.3
- Maven
- PostgreSQL 16
- Playwright for Java 1.52.0
- OpenAI Responses API
- Docker / Docker Compose
- GitHub Actions

## Run
```bash
git checkout v3-playwright-automation-agent
docker compose up -d postgres
mvn clean verify
mvn spring-boot:run -pl ai-qa-api
```

Open `http://localhost:8080/v3.html` for the V3 dashboard.
Health: `http://localhost:8080/actuator/health`

## V3 API
POST `/api/automation/generate`

Example:
```json
{
  "testId": "TC-001",
  "title": "Password reset happy path",
  "url": "http://localhost:8081",
  "steps": [
    "Open the application",
    "Enter registered email",
    "Click reset password",
    "Verify reset confirmation"
  ],
  "expectedResult": "A reset confirmation is displayed."
}
```

The generated source intentionally marks locator/action mapping as TODO in V3. V3.1 will use AI to turn natural-language steps into Playwright locators and actions. V4 will add a dummy UAT application and real execution.

## Roadmap
V4 → Dummy UAT Application + Execution Agent
V5 → Failure Analysis Agent
V6 → Safe Self-Healing
V7 → RAG with pgvector
V8 → Jira/Git/CI tools
V9 → Full CI/CD and cloud deployment
