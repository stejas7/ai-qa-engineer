package com.aiqa.platform;

import com.aiqa.analytics.SiteVisit;
import com.aiqa.analytics.SiteVisitRepository;
import com.aiqa.application.ApplicationTargetRepository;
import com.aiqa.company.CompanyRepository;
import com.aiqa.pipeline.PipelineRun;
import com.aiqa.pipeline.PipelineRunRepository;
import com.aiqa.security.AppUserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Single admin-only operational snapshot for the AI UAT Engineer platform. */
@RestController
@RequestMapping("/api/platform")
public class PlatformDiagnosticsController {
    private final CompanyRepository companies;
    private final ApplicationTargetRepository products;
    private final AppUserRepository users;
    private final PipelineRunRepository runs;
    private final SiteVisitRepository visits;

    public PlatformDiagnosticsController(CompanyRepository companies,
                                         ApplicationTargetRepository products,
                                         AppUserRepository users,
                                         PipelineRunRepository runs,
                                         SiteVisitRepository visits) {
        this.companies = companies;
        this.products = products;
        this.users = users;
        this.runs = runs;
        this.visits = visits;
    }

    @GetMapping("/diagnostics")
    public DiagnosticsView diagnostics() {
        Runtime runtime = Runtime.getRuntime();
        long max = runtime.maxMemory();
        long allocated = runtime.totalMemory();
        long freeInsideAllocated = runtime.freeMemory();
        long used = allocated - freeInsideAllocated;
        long remaining = Math.max(0, max - used);

        List<PipelineRun> allRuns = runs.findAllByOrderByCreatedAtDesc();
        long running = allRuns.stream().filter(r -> "RUNNING".equalsIgnoreCase(r.getStatus()) || "QUEUED".equalsIgnoreCase(r.getStatus())).count();
        long failed = allRuns.stream().filter(r -> "FAILED".equalsIgnoreCase(r.getStatus())).count();
        Instant last24h = Instant.now().minus(24, ChronoUnit.HOURS);
        long failedLast24h = allRuns.stream().filter(r -> "FAILED".equalsIgnoreCase(r.getStatus()) && r.getCreatedAt().isAfter(last24h)).count();

        List<SiteVisit> allVisits = visits.findAll();
        long uniqueVisitors = allVisits.stream().map(SiteVisit::getVisitorId).distinct().count();
        long visitsLast24h = allVisits.stream().filter(v -> v.getVisitedAt().isAfter(last24h)).count();
        long uniqueVisitorsLast24h = allVisits.stream().filter(v -> v.getVisitedAt().isAfter(last24h)).map(SiteVisit::getVisitorId).distinct().count();

        long companyCount = companies.count();
        long productCount = products.count();
        long userCount = users.count();
        long activeProducts = products.findAllByOrderByCreatedAtDesc().stream().filter(p -> p.isActive()).count();

        String health = failedLast24h > 0 ? "DEGRADED" : "HEALTHY";
        return new DiagnosticsView(
                health,
                ManagementFactory.getRuntimeMXBean().getUptime(),
                new MemoryView(bytesToMb(used), bytesToMb(remaining), bytesToMb(max), percent(used, max)),
                new RunView(allRuns.size(), running, failed, failedLast24h),
                new TrafficView(allVisits.size(), uniqueVisitors, visitsLast24h, uniqueVisitorsLast24h),
                new TenantView(companyCount, userCount, productCount, activeProducts),
                Instant.now());
    }

    private long bytesToMb(long bytes) { return Math.round(bytes / 1024.0 / 1024.0); }
    private double percent(long value, long total) { return total <= 0 ? 0 : Math.round((value * 10000.0 / total)) / 100.0; }

    public record DiagnosticsView(String health, long uptimeMs, MemoryView memory, RunView uatRuns,
                                  TrafficView traffic, TenantView tenants, Instant generatedAt) {}
    public record MemoryView(long usedMb, long remainingMb, long maxMb, double usedPercent) {}
    public record RunView(long total, long running, long failed, long failedLast24h) {}
    public record TrafficView(long totalVisits, long uniqueVisitors, long visitsLast24h, long uniqueVisitorsLast24h) {}
    public record TenantView(long companies, long users, long products, long activeProducts) {}
}
