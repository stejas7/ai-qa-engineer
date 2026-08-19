package com.aiqa.performance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Persisted M11 performance evidence for release review. */
@Entity
@Table(name = "ai_uat_load_test_runs")
public class LoadTestRun {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 2048)
    private String targetUrl;
    @Column(nullable = false) private int requests;
    @Column(nullable = false) private int concurrency;
    @Column(nullable = false) private long durationMs;
    @Column(nullable = false) private long p50Ms;
    @Column(nullable = false) private long p95Ms;
    @Column(nullable = false) private long p99Ms;
    @Column(nullable = false) private double throughputPerSecond;
    @Column(nullable = false) private int failures;
    @Column(nullable = false) private double errorRatePercent;
    @Column(nullable = false) private long maxP95Ms;
    @Column(nullable = false) private double maxErrorRatePercent;
    @Column(nullable = false) private boolean sloPassed;
    @Column(nullable = false) private String releaseRisk;
    @Column(nullable = false, length = 1000) private String summary;
    @Column(nullable = false, updatable = false) private Instant createdAt = Instant.now();

    protected LoadTestRun() {}

    public LoadTestRun(LoadTestService.LoadTestResult result) {
        this.targetUrl = result.targetUrl();
        this.requests = result.requests();
        this.concurrency = result.concurrency();
        this.durationMs = result.durationMs();
        this.p50Ms = result.p50Ms();
        this.p95Ms = result.p95Ms();
        this.p99Ms = result.p99Ms();
        this.throughputPerSecond = result.throughputPerSecond();
        this.failures = result.failures();
        this.errorRatePercent = result.errorRatePercent();
        this.maxP95Ms = result.maxP95Ms();
        this.maxErrorRatePercent = result.maxErrorRatePercent();
        this.sloPassed = result.sloPassed();
        this.releaseRisk = result.releaseRisk();
        this.summary = result.summary();
    }

    public UUID getId() { return id; }
    public String getTargetUrl() { return targetUrl; }
    public int getRequests() { return requests; }
    public int getConcurrency() { return concurrency; }
    public long getDurationMs() { return durationMs; }
    public long getP50Ms() { return p50Ms; }
    public long getP95Ms() { return p95Ms; }
    public long getP99Ms() { return p99Ms; }
    public double getThroughputPerSecond() { return throughputPerSecond; }
    public int getFailures() { return failures; }
    public double getErrorRatePercent() { return errorRatePercent; }
    public long getMaxP95Ms() { return maxP95Ms; }
    public double getMaxErrorRatePercent() { return maxErrorRatePercent; }
    public boolean isSloPassed() { return sloPassed; }
    public String getReleaseRisk() { return releaseRisk; }
    public String getSummary() { return summary; }
    public Instant getCreatedAt() { return createdAt; }
}
