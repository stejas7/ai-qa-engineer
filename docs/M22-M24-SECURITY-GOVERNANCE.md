# M22-M24 Security & Governance

## M22 — Authorization hardening

- Mature capability groups no longer fall through to public access.
- Mutation rights are separated from read rights.
- Platform-owner routes accept `SUPER_ADMIN` and legacy `PLATFORM_ADMIN`.
- `/api/auth/capabilities` exposes advisory UI permissions while server-side rules remain authoritative.

## M23 — Durable security audit

- Authenticated API mutations receive an `X-Correlation-Id`.
- Metadata-only audit events are persisted with actor, method, path, response status and timestamp.
- Request bodies, passwords, tokens and credential values are never captured by the audit filter.
- Platform owners can read recent events at `/api/platform/security-audit`.

## M24 — Tenant governance

- Per-company limits are persisted for users, products and concurrent UAT runs.
- Default limits: 50 users, 20 products, 5 concurrent UAT runs.
- User and product creation enforce configured limits.
- Platform owners can inspect/update limits under `/api/platform/governance/companies/{companyId}`.

These controls are intentionally additive and remain compatible with the M21 tenant/admin model.
