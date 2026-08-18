# Auravis 3.0 Roadmap

| Milestone | Capability | Status |
|---|---|---|
| M1 | Autonomous Mission | ✅ Complete |
| M2 | Knowledge / RAG Foundation | ✅ Complete foundation |
| M3 | Intelligent Test Generation | ✅ Complete |
| M4 | Advanced Automation & Multi-App Support | ✅ Complete |
| M5 | Agentic Orchestration | ✅ Complete |
| M6 | Self-Healing & Smart Recovery | ✅ Complete |
| M7 | Spring AI Intelligence + Regression | 🔨 In progress |
| M8 | Defect Management & Autonomous CI/CD Quality Gate | Planned |

## Auravis 3.0 direction

Auravis 3.0 makes Spring AI the standard intelligence layer while keeping state-changing actions behind deterministic Java services.

Spring AI is already active for requirement intelligence and failure diagnosis through `ChatClient`.

## M7 implementation sequence

1. Central Spring AI runtime and health metadata — implemented
2. Controlled read-only QA tool calling — implementation started
3. Spring AI RAG integration over the existing knowledge foundation
4. pgvector-backed semantic retrieval
5. Historical regression selection and failure similarity
6. Flaky-test intelligence
7. TEJAS integration with the same Spring AI runtime and controlled tools
8. UI, API reference, tests and deployment verification

## M5 completed flow

Requirement Analysis → Test Design → Automation Generation → UAT Execution → Failure Diagnosis → Quality Decision, with persisted AgentRun/AgentStep observability.

## M6 completed safety model

Failure classification → protected-category check → ≥90% confidence gate → deterministic fallback repair → one bounded retry → persisted healing audit/evidence.

Assertion and business failures are never auto-healed.

## M7 engineering boundary

The model may request information through Spring AI tools, but Java remains responsible for authorization, persistence, execution and policy. Initial M7 tools are intentionally read-only. Mutation tools will only be added when their governance rules are explicit and testable.
