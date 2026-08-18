# M8 Reliable Autonomous UAT Operations

M8 hardens Auravis deployment and runtime verification so autonomous changes fail safely, produce useful evidence, and avoid blind recovery attempts.

## Operating rule

Each autonomous maintenance run must inspect the latest repository and CI/CD state first, then make at most one bounded low-risk M8 improvement. If a failure repeats or the required recovery is risky or ambiguous, stop changing code and report the blocker instead of guessing.

M8 must not modify production data, secrets, IAM/security groups, billing/resources, or destructive Docker volumes.

## Deployment verification sequence

The production deployment workflow currently verifies the following sequence:

1. EC2 SSH reachability and minimum free-disk preflight.
2. GHCR authentication and immutable image pull by commit SHA.
3. Docker Compose container startup.
4. Local `/actuator/health` readiness.
5. Local `/api/ai/runtime` Spring AI runtime contract.
6. Public `/actuator/health` readiness.
7. Public pipeline/runtime contract checks.

A deployment is not considered successful until the public checks complete.

## M8 failure taxonomy

| Failure class | Meaning | Default recovery posture |
|---|---|---|
| `LOW_DISK` | EC2 root filesystem is below the deployment safety threshold. | Run the safe EC2 disk-cleanup workflow; preserve Docker volumes. |
| `SSH_FAILURE` | EC2 TCP/22 or SSH authentication/reachability failed. | Diagnose connectivity; do not change IAM/security groups automatically. |
| `DOCKER_PULL_FAILURE` | GHCR login, image tag, package permission, or image pull failed. | Verify image publication/tag/auth before retrying. |
| `DOCKER_START_FAILURE` | Image pulled but Docker Compose/container startup failed. | Inspect compose status and backend logs; do not blindly retry. |
| `APP_HEALTH_FAILURE` | Container started but local Spring Boot health did not become `UP`. | Capture logs and attempt rollback to the previous image. |
| `RAG_RUNTIME_FAILURE` | Spring AI/runtime/RAG contract failed. | Verify runtime configuration and dependencies; rollback is attempted for local runtime failure. |
| `PUBLIC_ENDPOINT_FAILURE` | Local runtime is healthy but the public endpoint is unavailable/unhealthy. | Check reverse proxy/DNS/TLS/routing without changing infrastructure automatically. |
| `UNKNOWN` | Failure does not match a deterministic class. | Stop and inspect the failing Actions step/logs before changing code. |

## Rollback safety

Before replacing the running container, the deployment records the previous `ai-qa-api` image. Local application-health and AI-runtime verification failures attempt to restore that previous image with Docker Compose.

Important: an attempted rollback is not equivalent to a verified recovery. A future bounded M8 improvement should verify that the restored image returns `/actuator/health` = `UP` and report rollback outcome explicitly. Until that is implemented, failed deployments must be treated as requiring operator review even when the log says rollback was attempted.

## Evidence and observability

For Docker startup and application-health failures, preserve the most useful non-secret evidence in the Actions log:

- `docker compose ps`
- recent `ai-qa-api` logs
- deterministic M8 failure class
- local/public health result
- Spring AI runtime contract result

Do not print credentials, API keys, tokens, or secret environment values.

## CI/CD decision record

GitHub commit statuses may be empty even when repository Actions are configured. An empty combined-status response must therefore be reported as **status unavailable**, not interpreted as success or failure. Do not make speculative fixes solely because no status record is returned.

When actual Actions failure logs are available, diagnose the observed failing step first and make only one targeted safe fix in that run.

## Current M8 priorities

1. Reliable build-to-image-to-deploy sequencing.
2. Deterministic failure classification.
3. Safe recovery and rollback verification.
4. Local and public health/runtime/RAG verification.
5. Actionable logs and deployment summaries.
6. JUnit coverage for deterministic M8 policy/classification code.
7. Documentation of lessons that prevent repeated speculative fixes.
