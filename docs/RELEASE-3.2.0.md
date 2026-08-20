# AI UAT Engineer 3.2.0 Stabilization Release

3.2.0 is a stabilization release line. New feature work must not bypass a failed gate.

## Release gates

1. Deployment GREEN
   - backend container starts successfully
   - PostgreSQL remains healthy
   - `/actuator/health` returns UP
   - `/api/ai/runtime` is reachable and reports the expected Spring AI runtime
   - local/public UAT smoke verification passes
   - rollback remains available and non-destructive

2. M19 E2E GREEN
   - authenticated tenant selects a registered product environment
   - runtime credential is resolved server-side only
   - Playwright executes the registered target
   - evidence survives deployment boundaries
   - release result is generated without exposing credentials

3. Platform Admin login GREEN
   - safe bootstrap creates the first PLATFORM_ADMIN only from environment configuration
   - normal sign-in establishes the session
   - Platform Owner Console is available only to PLATFORM_ADMIN
   - no tenant secrets/password hashes are exposed

4. SSO GREEN
   - Google SSO works for an existing invited/registered user by verified email
   - GitHub SSO works for an existing invited/registered user by verified email
   - SSO never auto-creates a customer tenant
   - provider secrets stay outside Git

5. M20.4-M20.8
   - cross-tenant UAT operations monitoring
   - failure/evidence reporting
   - performance overview
   - platform audit trail
   - read-only report drill-down

6. Self-validation
   - AI UAT Engineer performs UAT against its own supported flows
   - bounded load testing validates agreed SLOs
   - tenant/IDOR/authorization checks pass
   - evidence and release recommendation are reviewed

7. 4.0 Stable
   - only after all previous gates are green

## Current blocker

The latest known deployment failure was Spring bean construction for `RuntimeCredentialResolver` after a second test constructor was introduced. Commit `8dc6d602e284e641641db0bb9396a7bf491d40d3` explicitly selects the production constructor. Deployment must be verified green before advancing to the next gate.

## Release rule

If a gate fails, diagnose the exact failure first. Apply at most one bounded low-risk fix for that failure. If the same failure repeats or recovery would require risky/ambiguous infrastructure, secret, IAM, billing, or destructive data actions, stop and report the blocker instead of guessing.
