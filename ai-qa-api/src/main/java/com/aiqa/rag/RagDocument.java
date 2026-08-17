package com.aiqa.rag;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "rag_documents")
public class RagDocument {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, columnDefinition = "text")
    private String content;
    @Column(nullable = false)
    private String source;
    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected RagDocument() {}
    public RagDocument(String content, String source) { this.content = content; this.source = source; }
    public Long getId() { return id; }
    public String getContent() { return content; }
    public String getSource() { return source; }
    public Instant getCreatedAt() { return createdAt; }
}
