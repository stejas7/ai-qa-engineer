# V4 + V5

V4 adds a runnable UAT execution agent and a deliberately buggy demo UAT app.
V5 adds failure analysis with a deterministic fallback and optional OpenAI analysis.

## Quick start in Codespaces

```bash
git checkout main
git pull origin main
docker compose up -d postgres
mvn clean verify
mvn -q exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium"
mvn spring-boot:run -pl ai-qa-api
```

Open the forwarded port 8080 and use `/v4.html` or `/v5.html`.

Demo UAT is available under `/uat/`.

### V4 API
`POST /api/execution/run`

### V5 API
`POST /api/failure-analysis/analyze`

V5 can use `OPENAI_API_KEY`; without it, the service classifies common failures deterministically so the demo remains runnable.
