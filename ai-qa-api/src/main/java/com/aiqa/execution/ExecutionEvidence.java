package com.aiqa.execution;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.time.Instant;

/** Persisted browser evidence so links survive application/container redeploys. */
@Entity
@Table(name = "execution_evidence")
public class ExecutionEvidence {
    @Id
    @Column(length = 255)
    private String fileName;

    @Lob
    @Column(nullable = false)
    private byte[] content;

    @Column(nullable = false, length = 100)
    private String mediaType;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected ExecutionEvidence() {}

    public ExecutionEvidence(String fileName, byte[] content, String mediaType) {
        this.fileName = fileName;
        this.content = content;
        this.mediaType = mediaType;
    }

    public String getFileName() { return fileName; }
    public byte[] getContent() { return content; }
    public String getMediaType() { return mediaType; }
    public Instant getCreatedAt() { return createdAt; }
}
