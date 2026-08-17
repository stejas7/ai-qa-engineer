# V7 — Enterprise Knowledge & RAG

## Goal

V7 adds a persistent enterprise knowledge layer so AI agents can retrieve relevant requirements, test history, defects, documentation, runbooks, and execution evidence before making decisions.

## Architecture

```text
Documents / Requirements / Tests / Defects / Evidence
                         |
                         v
                  Ingestion Pipeline
                         |
                    Chunk + Metadata
                         |
                         v
                    Embeddings
                         |
                         v
              PostgreSQL + pgvector
                         |
                         v
                    Retriever
                         |
              +----------+----------+
              |                     |
        Semantic Search       Metadata Filters
              |                     |
              +----------+----------+
                         |
                         v
                  Context Builder
                         |
                         v
                  Agent / LLM
                         |
                         v
              Answer + Source References
```

## V7 capabilities

- Knowledge document ingestion
- Chunking and metadata
- Vector-search abstraction
- Project-scoped knowledge
- Source references in AI responses
- Retrieval before agent decisions
- Historical execution/failure knowledge
- Provider abstraction for embeddings
- Mock mode so Codespaces can run without external AI credentials

## Knowledge types

| Type | Examples |
|---|---|
| Requirement | Business requirements, acceptance criteria |
| Test | Test cases, automation metadata |
| Defect | Bugs, failure classifications, resolutions |
| Documentation | API docs, architecture docs, runbooks |
| Evidence | Screenshots, traces, execution summaries |
| Decision | Approved healing proposals and architecture decisions |

## Design principles

1. **Project isolation** — retrieval is scoped to the active project unless explicitly configured otherwise.
2. **Evidence first** — generated answers should reference the source documents/chunks used.
3. **No hidden reasoning** — persist concise decisions and evidence, not private chain-of-thought.
4. **Provider abstraction** — LLM and embedding providers must not leak into domain code.
5. **Deterministic fallback** — local/demo mode must work without an API key.
6. **Versioned knowledge** — documents can be updated without destroying historical provenance.

## Planned API surface

```text
POST /api/knowledge/documents
GET  /api/knowledge/documents
POST /api/knowledge/search
GET  /api/knowledge/documents/{id}
```

## V7 → V8

V7 provides the knowledge foundation. V8 consumes it through the Agent Orchestrator and controlled KnowledgeTool, allowing specialized agents to retrieve project context while retaining permissions, auditability, and source provenance.
