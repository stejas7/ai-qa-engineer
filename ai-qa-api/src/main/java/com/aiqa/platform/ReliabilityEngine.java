package com.aiqa.platform;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** M43-M46 runtime for tenant quotas, checkpoints, observability snapshots and SLO guardrails. */
@Service
public class ReliabilityEngine {
    private final Map<String, AtomicInteger> activeByTenant = new ConcurrentHashMap<>();
    private final Map<String, Checkpoint> checkpoints = new ConcurrentHashMap<>();
    private final Map<String, MetricWindow> metrics = new ConcurrentHashMap<>();

    public QuotaDecision acquire(String tenantId, int maxConcurrent) {
        require(tenantId, "tenantId");
        int max = Math.max(1, Math.min(maxConcurrent, 100));
        int current = activeByTenant.computeIfAbsent(tenantId, k -> new AtomicInteger()).incrementAndGet();
        if (current > max) { activeByTenant.get(tenantId).decrementAndGet(); return new QuotaDecision(false, current - 1, max, "BACKPRESSURE"); }
        return new QuotaDecision(true, current, max, "ACQUIRED");
    }

    public QuotaDecision release(String tenantId, int maxConcurrent) {
        AtomicInteger value = activeByTenant.computeIfAbsent(require(tenantId, "tenantId"), k -> new AtomicInteger());
        int current = Math.max(0, value.decrementAndGet()); value.set(current);
        return new QuotaDecision(true, current, Math.max(1, maxConcurrent), "RELEASED");
    }

    public Checkpoint checkpoint(String tenantId, String missionId, String stage, String payloadRef) {
        require(tenantId,"tenantId"); require(missionId,"missionId"); require(stage,"stage");
        Checkpoint cp = new Checkpoint(UUID.randomUUID().toString(), tenantId, missionId, stage, payloadRef == null ? "" : payloadRef, Instant.now().toString());
        checkpoints.put(tenantId + ":" + missionId, cp); return cp;
    }

    public Checkpoint resume(String tenantId, String missionId) {
        Checkpoint cp = checkpoints.get(require(tenantId,"tenantId") + ":" + require(missionId,"missionId"));
        if (cp == null) throw new IllegalArgumentException("No checkpoint found");
        return cp;
    }

    public MetricWindow observe(String tenantId, long latencyMs, boolean error) {
        require(tenantId,"tenantId");
        MetricWindow current = metrics.getOrDefault(tenantId, new MetricWindow(0,0,0,0));
        long count = current.count()+1;
        long errors = current.errors() + (error ? 1 : 0);
        long totalLatency = current.totalLatencyMs() + Math.max(0, latencyMs);
        MetricWindow next = new MetricWindow(count, errors, totalLatency, count == 0 ? 0 : totalLatency / count);
        metrics.put(tenantId, next); return next;
    }

    public SloDecision slo(String tenantId, double maxErrorRate, long maxAvgLatencyMs) {
        MetricWindow m = metrics.getOrDefault(require(tenantId,"tenantId"), new MetricWindow(0,0,0,0));
        double errorRate = m.count() == 0 ? 0 : (double)m.errors()/m.count();
        boolean healthy = errorRate <= maxErrorRate && m.averageLatencyMs() <= maxAvgLatencyMs;
        return new SloDecision(healthy, errorRate, m.averageLatencyMs(), healthy ? "NORMAL" : "DEGRADE_AUTONOMY");
    }

    private String require(String v,String f){if(v==null||v.isBlank()) throw new IllegalArgumentException(f+" is required"); return v.trim();}
    public record QuotaDecision(boolean allowed,int active,int limit,String state){}
    public record Checkpoint(String id,String tenantId,String missionId,String stage,String payloadRef,String createdAt){}
    public record MetricWindow(long count,long errors,long totalLatencyMs,long averageLatencyMs){}
    public record SloDecision(boolean healthy,double errorRate,long averageLatencyMs,String policy){}
}
