package com.aiqa.platform;

import com.aiqa.integration.WebhookDelivery;
import com.aiqa.integration.WebhookDeliveryRepository;
import com.aiqa.pipeline.PipelineRun;
import com.aiqa.pipeline.PipelineRunRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Platform-owner operational readiness/SLO snapshot. */
@RestController
@RequestMapping("/api/platform/readiness")
public class OperationalReadinessController {
    private final PipelineRunRepository runs;
    private final WebhookDeliveryRepository deliveries;

    public OperationalReadinessController(PipelineRunRepository runs, WebhookDeliveryRepository deliveries) {
        this.runs = runs;
        this.deliveries = deliveries;
    }

    @GetMapping
    public ReadinessView readiness() {
        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
        List<PipelineRun> recentRuns = runs.findAllByOrderByCreatedAtDesc().stream()
                .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().isAfter(since))
                .toList();
        long running = recentRuns.stream().filter(r -> "RUNNING".equalsIgnoreCase(r.getStatus()) || "QUEUED".equalsIgnoreCase(r.getStatus())).count();
        long completed = recentRuns.stream().filter(r -> "COMPLETED".equalsIgnoreCase(r.getStatus())).count();
        long failed = recentRuns.stream().filter(r -> "FAILED".equalsIgnoreCase(r.getStatus())).count();
        long terminal = completed + failed;
        double uatSuccessRate = terminal == 0 ? 100.0 : round(completed * 100.0 / terminal);

        List<WebhookDelivery> recentDeliveries = deliveries.findAll().stream()
                .filter(d -> d.getCreatedAt() != null && d.getCreatedAt().isAfter(since))
                .toList();
        long webhookSuccess = recentDeliveries.stream().filter(WebhookDelivery::isSuccess).count();
        double webhookSuccessRate = recentDeliveries.isEmpty() ? 100.0 : round(webhookSuccess * 100.0 / recentDeliveries.size());

        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long max = runtime.maxMemory();
        double heapUsedPercent = max <= 0 ? 0.0 : round(used * 100.0 / max);
        long uptimeSeconds = ManagementFactory.getRuntimeMXBean().getUptime() / 1000;

        boolean healthy = heapUsedPercent < 90.0 && uatSuccessRate >= 90.0 && webhookSuccessRate >= 90.0;
        String status = healthy ? "READY" : "DEGRADED";
        String reason = healthy ? "Operational indicators are within target" : readinessReason(heapUsedPercent, uatSuccessRate, webhookSuccessRate);

        return new ReadinessView(status, reason, uptimeSeconds, running, completed, failed, uatSuccessRate,
                recentDeliveries.size(), webhookSuccessRate, heapUsedPercent, Instant.now().toString());
    }

    private String readinessReason(double heap, double uat, double webhook) {
        if (heap >= 90.0) return "High JVM heap pressure";
        if (uat < 90.0) return "24h UAT success rate below 90%";
        if (webhook < 90.0) return "24h integration delivery success below 90%";
        return "Operational indicator needs attention";
    }

    private double round(double value) { return Math.round(value * 10.0) / 10.0; }

    public record ReadinessView(String status, String reason, long uptimeSeconds, long runningUat,
                                long completed24h, long failed24h, double uatSuccessRate24h,
                                long webhookDeliveries24h, double webhookSuccessRate24h,
                                double heapUsedPercent, String generatedAt) {}
}
