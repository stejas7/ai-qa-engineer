package com.aiqa.execution;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/** Persisted audit record for one Auravis browser execution. */
@Entity
@Table(name = "auravis_execution_records")
public class ExecutionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String testId;

    @Column(nullable = false, length = 2048)
    private String targetUrl;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private long durationMs;

    @Column(length = 2048)
    private String screenshot;

    @Column(length = 4000)
    private String message;

    @Column(nullable = false, updatable = false)
    private Instant executedAt = Instant.now();

    protected ExecutionRecord() {}

    public ExecutionRecord(String testId, String targetUrl, String status, long durationMs,
                           String screenshot, String message) {
        this.testId = testId;
        this.targetUrl = targetUrl;
        this.status = status;
        this.durationMs = durationMs;
        this.screenshot = screenshot;
        this.message = message;
    }

    public UUID getId() { return id; }
    public String getTestId() { return testId; }
    public String getTargetUrl() { return targetUrl; }
    public String getStatus() { return status; }
    public long getDurationMs() { return durationMs; }
    public String getScreenshot() { return screenshot; }
    public String getMessage() { return message; }
    public Instant getExecutedAt() { return executedAt; }
}
