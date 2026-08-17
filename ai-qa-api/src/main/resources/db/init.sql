CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS rag_embeddings (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES rag_documents(id) ON DELETE CASCADE,
    embedding vector(1536) NOT NULL
);

CREATE INDEX IF NOT EXISTS rag_embeddings_embedding_idx
ON rag_embeddings USING hnsw (embedding vector_cosine_ops);
