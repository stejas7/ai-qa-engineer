# M25-M27 Operations & Integrations

## M25 — Tenant integrations
- Tenant-owned HTTPS webhook endpoints.
- Explicit event subscriptions: `UAT_COMPLETED`, `UAT_FAILED`, `RELEASE_READY`, `RELEASE_BLOCKED`.
- Company Admin / QA Manager mutation rights; tenant users may inspect configured integrations.
- Private/localhost endpoints are rejected.

## M26 — Webhook delivery
- Bounded HTTP delivery with connect/request timeouts.
- Test delivery endpoint per integration.
- Delivery metadata persisted without storing remote response bodies or secrets.
- Tenant-scoped delivery history.

## M27 — Operational readiness
- Platform-owner readiness endpoint at `/api/platform/readiness`.
- 24-hour UAT success rate, running workload, webhook success rate, JVM heap pressure and uptime.
- Simple `READY` / `DEGRADED` operational signal with a reason.
