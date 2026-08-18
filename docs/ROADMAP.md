# Auravis 2.0 Roadmap

| Milestone | Capability | Status |
|---|---|---|
| M1 | Autonomous Mission | ✅ Complete |
| M2 | Knowledge / RAG Foundation | ✅ Complete foundation |
| M3 | Intelligent Test Generation | ✅ Complete |
| M4 | Advanced Automation & Multi-App Support | ✅ Complete |
| M5 | Agentic Orchestration | ✅ Complete |
| M6 | Self-Healing & Smart Recovery | ✅ Complete |
| M7 | Regression & Learning Intelligence | 🔨 In progress |
| M8 | Defect Management & Autonomous CI/CD Quality Gate | Planned |

## M5 completed flow

Requirement Analysis → Test Design → Automation Generation → UAT Execution → Failure Diagnosis → Quality Decision, with persisted AgentRun/AgentStep observability.

## M6 completed safety model

Failure classification → protected-category check → ≥90% confidence gate → deterministic fallback repair → one bounded retry → persisted healing audit/evidence.

Assertion and business failures are never auto-healed.

## Next: M7

Use historical missions, executions, failures and validated healing outcomes to prioritize smarter regression coverage without allowing historical data to silently rewrite business expectations.
