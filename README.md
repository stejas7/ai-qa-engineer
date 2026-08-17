# AI QA Engineer — V2

A build-first demo of an AI-driven QA/UAT automation platform.

## V1
Business Requirement → AI Requirement Analysis → structured test scenarios → PostgreSQL → dashboard.

## V2 — Test Design Agent
Business Requirement → AI Requirement Analysis → detailed executable test cases.

V2 adds:
- Functional, negative and boundary test design
- Preconditions
- Test steps
- Test data guidance
- Expected results
- Automation-candidate flag
- Acceptance-criteria traceability case
- Dedicated V2 browser dashboard

## Stack
- Java 21
- Spring Boot 3.5.3
- Maven
- PostgreSQL 16
- Docker / Docker Compose
- OpenAI Responses API
- GitHub Actions

## Run
```bash
docker compose up -d postgres
mvn clean verify
mvn spring-boot:run -pl ai-qa-api
```

Open `http://localhost:8080/v2.html` for the V2 dashboard.
Health: `http://localhost:8080/actuator/health`

Without `OPENAI_API_KEY`, the application uses deterministic demo output so the workflow can be tested without an API key. With AI, set `OPENAI_API_KEY` and optionally `OPENAI_MODEL`.

## V2 API
POST `/api/test-design/generate`

Example:
```json
{
  "title": "Password reset",
  "description": "A registered customer can reset their password using their registered email address.",
  "acceptanceCriteria": [
    "Registered email receives a reset link",
    "Invalid email displays an error",
    "Reset link expires after 15 minutes"
  ]
}
```

## Next versions
V3 → Playwright Automation Agent
V4 → Dummy UAT Application + Execution Agent
V5 → Failure Analysis Agent
V6 → Safe Self-Healing
V7 → RAG with pgvector
V8 → Jira/Git/CI tools
V9 → Full CI/CD and cloud deployment
