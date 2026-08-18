# Scorpion 2.0 — V6 RAG / Knowledge Layer

V6 grounds autonomous QA decisions in project knowledge rather than relying only on the submitted requirement.

## Knowledge sources

- BRD / PRD / user stories
- acceptance criteria
- API specifications
- architecture documentation
- existing test cases
- defects and resolutions
- previous Scorpion mission results
- runbooks

## Retrieval flow

```text
Document -> Chunk -> Metadata -> Embedding -> Vector Store
                                           |
Mission -> Query --------------------------+
                                           v
                                      Retriever
                                           |
                                           v
                                     Context Builder
                                           |
                                           v
                                  Scorpion Agent Stage
```

## Rules

- Retrieval is project-scoped.
- AI output must retain source provenance.
- Provider-specific embedding code stays behind an abstraction.
- Demo/local mode must remain usable without paid AI credentials.
- Secrets and private chain-of-thought are never persisted as knowledge.

## Exit criteria

A mission can retrieve relevant project knowledge before requirement analysis, test generation and failure diagnosis.
