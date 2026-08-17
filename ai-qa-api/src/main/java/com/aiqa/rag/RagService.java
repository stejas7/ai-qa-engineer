package com.aiqa.rag;

import jakarta.annotation.PostConstruct;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.util.*;

@Service
public class RagService {
    private final JdbcTemplate jdbc;
    private final RestClient openai;
    private final String apiKey;

    public RagService(JdbcTemplate jdbc, Environment env) {
        this.jdbc = jdbc;
        this.apiKey = env.getProperty("openai.api-key", "");
        this.openai = RestClient.builder().baseUrl("https://api.openai.com/v1").build();
    }

    @PostConstruct
    void ensureSchema() {
        jdbc.execute("CREATE EXTENSION IF NOT EXISTS vector");
        jdbc.execute("CREATE TABLE IF NOT EXISTS rag_embeddings (id BIGSERIAL PRIMARY KEY, document_id BIGINT NOT NULL REFERENCES rag_documents(id) ON DELETE CASCADE, embedding vector(1536) NOT NULL)");
        jdbc.execute("CREATE INDEX IF NOT EXISTS rag_embeddings_embedding_idx ON rag_embeddings USING hnsw (embedding vector_cosine_ops)");
    }

    public Map<String,Object> ingest(String content, String source) {
        requireKey();
        float[] vector = embed(content);
        Long id = jdbc.queryForObject("insert into rag_documents(content,source) values (?,?) returning id", Long.class, content, source);
        jdbc.update("insert into rag_embeddings(document_id,embedding) values (?, ?::vector)", id, vectorLiteral(vector));
        return Map.of("id", id, "source", source, "status", "indexed");
    }

    public List<Map<String,Object>> search(String query, int limit) {
        requireKey();
        float[] vector = embed(query);
        return jdbc.query("select d.id,d.content,d.source,1-(e.embedding <=> ?::vector) as score from rag_embeddings e join rag_documents d on d.id=e.document_id order by e.embedding <=> ?::vector limit ?", (rs,n)->Map.of("id",rs.getLong("id"),"content",rs.getString("content"),"source",rs.getString("source"),"score",rs.getDouble("score")), vectorLiteral(vector), vectorLiteral(vector), Math.max(1, Math.min(limit, 20)));
    }

    private void requireKey(){ if(apiKey.isBlank()) throw new IllegalStateException("OPENAI_API_KEY is not configured"); }

    private float[] embed(String text) {
        Map<String,Object> body = Map.of("model", "text-embedding-3-small", "input", text);
        Map response = openai.post().uri("/embeddings").header("Authorization", "Bearer " + apiKey).body(body).retrieve().body(Map.class);
        List data = (List) response.get("data");
        Map first = (Map) data.get(0);
        List numbers = (List) first.get("embedding");
        float[] result = new float[numbers.size()];
        for (int i=0;i<numbers.size();i++) result[i]=((Number)numbers.get(i)).floatValue();
        return result;
    }

    private String vectorLiteral(float[] v) {
        StringBuilder b=new StringBuilder("[");
        for(int i=0;i<v.length;i++){ if(i>0)b.append(','); b.append(v[i]); }
        return b.append(']').toString();
    }
}
