package com.aiqa.pipeline;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Persisted state of one "upload a requirement file, get a full QA result" run.
 *
 * <p>The heavy pipeline (requirement understanding, test design, automation generation, real
 * UAT execution and quality gate evaluation) runs in the background; the dashboard polls this
 * entity by id to show progress and, once {@code COMPLETED}, the full result.</p>
 */
@Entity
@Table(name = "pipeline_runs")
public class PipelineRun {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false)
    private String fileName;

    /** QUEUED -> RUNNING -> COMPLETED | FAILED */
    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant completedAt;

    @Column(length = 2000)
    private String currentStage;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String resultJson;

    @Column(length = 2000)
    private String errorMessage;

    protected PipelineRun() {}

    public PipelineRun(String company, String fileName) {
        this.company = company;
        this.fileName = fileName;
        this.status = "QUEUED";
        this.currentStage = "Queued";
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getCompany() { return company; }
    public String getFileName() { return fileName; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
    public String getCurrentStage() { return currentStage; }
    public String getResultJson() { return resultJson; }
    public String getErrorMessage() { return errorMessage; }

    public void markRunning(String stage) { this.status = "RUNNING"; this.currentStage = stage; }
    public void updateStage(String stage) { this.currentStage = stage; }

    public void complete(String resultJson) {
        this.status = "COMPLETED";
        this.currentStage = "Completed";
        this.resultJson = resultJson;
        this.completedAt = Instant.now();
    }

    public void fail(String errorMessage) {
        this.status = "FAILED";
        this.currentStage = "Failed";
        this.errorMessage = errorMessage;
        this.completedAt = Instant.now();
    }
}
