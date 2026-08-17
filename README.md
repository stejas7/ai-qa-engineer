# AI QA Engineer — V1

A build-first demo of an AI-driven QA/UAT automation platform.

## V1
Business Requirement → AI Requirement Analysis → Structured Test Scenarios → PostgreSQL → Dashboard.

## Stack
- Java 21
- Spring Boot 3.5.3
- Maven
- PostgreSQL 16
- Docker / Docker Compose
- OpenAI Responses API
- GitHub Actions

## Run
1. `docker compose up -d postgres`
2. `mvn spring-boot:run -pl ai-qa-api`
3. Open `http://localhost:8080`
4. Health: `http://localhost:8080/actuator/health`

Without `OPENAI_API_KEY`, the application uses deterministic demo AI output, so it can be tested without an API key.

With AI, set `OPENAI_API_KEY` and optionally `OPENAI_MODEL`.

## First demo
POST `/api/requirements/analyze` with a requirement and acceptance criteria. The API returns a structured summary, business rules, questions and test scenarios.

## Roadmap
Requirement Agent → Test Design Agent → Playwright Automation Agent → UAT Execution → Failure Analysis → Safe Self-Healing → RAG → Jira/Git/CI tools → Cloud deployment.
