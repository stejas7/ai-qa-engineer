# AI UAT Engineer — M21 to M30 Evolution Roadmap

This roadmap continues the product from current UAT stabilization into platform administration, secure external access, operational maturity and final marketing/demo readiness.

## M21 — Platform Administration & External Access
- Super Admin compatibility with legacy Platform Admin
- multi-Company-Admin lifecycle and last-admin safety
- tenant-safe API clients and scoped access tokens
- authorization hardening
- forgot/reset password flow
- platform diagnostics endpoint and UI
- NOVA product knowledge refresh

## M22 — Audit & Governance
- durable security/admin audit events
- privileged-action traceability
- tenant-safe audit views
- retention and redaction rules

## M23 — Quotas & Usage Controls
- company/product usage limits
- API client quotas and throttling
- UAT execution limits
- usage reporting for Super Admin and Company Admin

## M24 — Integration Hub
- outbound webhooks
- CI/CD integration contracts
- extensible provider adapters
- integration health and retry visibility

## M25 — Scheduling & Automation
- scheduled UAT runs
- recurring regression plans
- controlled re-run policies
- schedule history and ownership

## M26 — Reliability & Operations
- richer health signals
- background-job visibility
- failure/retry policies
- operational dashboards and alert-ready metrics

## M27 — Security Hardening
- explicit permission matrix
- API token scope enforcement
- stronger tenant-isolation tests
- secret lifecycle and revocation
- authorization regression suite

## M28 — Release Governance
- approval checkpoints
- release evidence packages
- policy-based READY/BLOCKED rules
- reviewer traceability

## M29 — Product Intelligence
- product-aware NOVA guidance
- release-risk summaries
- historical UAT comparison
- explainable recommendations grounded in persisted evidence

## M30 — Marketing Studio & Product Explainer
A UI-driven marketing workflow that turns the current deployed AI UAT Engineer capabilities into a polished product explanation video.

Flow:

```text
Choose Audience
      ↓
Generate Product Script
      ↓
Select / Arrange Scenes
      ↓
Generate Professional Female Voice-over
      ↓
Add Captions + Product Screens / Animations
      ↓
Preview Video
      ↓
Review / Regenerate Scene
      ↓
Export MP4
```

### M30 requirements
- script generated from the current product capability catalog, not stale hard-coded copy
- scene editor in the UI
- product screenshots / animated UI sequences
- professional female narration voice option
- captions and branding
- aspect-ratio presets for website, demo and LinkedIn
- preview before export
- MP4 export/download
- no feature may be claimed in the video unless marked implemented in the product capability registry

## Delivery Rule
Each milestone is implemented and validated before the next one starts. UAT regression and deployment stability take priority over feature expansion.
