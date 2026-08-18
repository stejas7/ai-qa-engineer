package com.aiqa.knowledge;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Project knowledge stored for retrieval during autonomous Scorpion missions. */
@Entity
@Table(name = "knowledge_documents")
public class KnowledgeDocument {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(length = 500)
    private String sourceReference;

    @Column(nullable = false)
    private Instant createdAt;

    protected KnowledgeDocument() {}

    public KnowledgeDocument(String title, String type, String content, String sourceReference) {
        this.title = title;
        this.type = type;
        this.content = content;
        this.sourceReference = sourceReference;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getType() { return type; }
    public String getContent() { return content; }
    public String getSourceReference() { return sourceReference; }
    public Instant getCreatedAt() { return createdAt; }
}
