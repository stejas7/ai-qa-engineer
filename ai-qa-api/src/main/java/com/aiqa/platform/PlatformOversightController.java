package com.aiqa.platform;

import com.aiqa.application.ApplicationTarget;
import com.aiqa.application.ApplicationTargetRepository;
import com.aiqa.company.Company;
import com.aiqa.company.CompanyRepository;
import com.aiqa.performance.LoadTestRun;
import com.aiqa.performance.LoadTestRunRepository;
import com.aiqa.pipeline.PipelineRun;
import com.aiqa.pipeline.PipelineRunRepository;
import com.aiqa.security.AppUser;
import com.aiqa.security.AppUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** M20 read-only platform-owner oversight. Never exposes password hashes or credential secrets. */
@RestController
@RequestMapping("/api/platform")
public class PlatformOversightController {
    private static final Pattern RELEASE_DECISION_PATTERN = Pattern.compile(
            "(?i)\\\"(?:releaseDecision|releaseRecommendation|qualityGate|decision)\\\"\\s*:\\s*\\\"(READY|BLOCKED|PASS|FAIL)\\\"");

    private final CompanyRepository companies;
    private final ApplicationTargetRepository products;
    private final AppUserRepository users;
    private final PipelineRunRepository runs;
    private final LoadTestRunRepository loadTests;

    public PlatformOversightController(CompanyRepository companies,
                                       ApplicationTargetRepository products,
                                       AppUserRepository users,
                                       PipelineRunRepository runs,
                                       LoadTestRunRepository loadTests) {
        this.companies = companies;
        this.products = products;
        this.users = users;
        this.runs = runs;
        this.loadTests = loadTests;
    }

    @GetMapping("/companies")
    public List<CompanyView> companies() {
        return companies.findAll().stream()
                .sorted(Comparator.comparing(Company::getCreatedAt).reversed())
                .map(c -> new CompanyView(c.getId(), c.getName(), c.getSlug(), c.isActive(),
                        products.findByCompanyIdOrderByCreatedAtDesc(c.getId()).size(),
                        users.findByCompanyIdOrderByCreatedAtAsc(c.getId()).size()))
                .toList();
    }

    @GetMapping("/products")
    public List<ProductView> products() {
        return products.findAllByOrderByCreatedAtDesc().stream()
                .map(p -> new ProductView(p.getId(), p.getCompanyId(), p.getName(), p.getEnvironment(), p.getAuthType(), p.isActive()))
                .toList();
    }

    @GetMapping("/users")
    public List<UserView> users() {
        return users.findAll().stream()
                .sorted(Comparator.comparing(AppUser::getCreatedAt).reversed())
                .map(u -> new UserView(u.getId(), u.getCompanyId(), u.getEmail(), u.getRole().name(), u.isActive()))
                .toList();
    }

    @GetMapping("/operations")
    public List<OperationView> operations() {
        return runs.findAllByOrderByCreatedAtDesc().stream().map(this::operationView).toList();
    }

    /** M20.8 sanitized read-only drill-down. Raw result JSON and evidence paths are never returned. */
    @GetMapping("/operations/{id}")
    public ResponseEntity<OperationDetailView> operation(@PathVariable UUID id) {
        return runs.findById(id)
                .map(run -> ResponseEntity.ok(operationDetailView(run)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/failures")
    public List<FailureView> failures() {
        return runs.findAllByOrderByCreatedAtDesc().stream()
                .filter(r -> "FAILED".equalsIgnoreCase(r.getStatus()))
                .map(r -> new FailureView(r.getId(), r.getCompany(), r.getFileName(), r.getCurrentStage(),
                        r.getCreatedAt(), r.getCompletedAt(), safeFailure(r.getErrorMessage())))
                .toList();
    }

    /** M20.6 read-only performance roll-up from persisted pipeline and load-test evidence. */
    @GetMapping("/performance")
    public PerformanceView performance() {
        List<PipelineRun> pipelineRuns = runs.findAllByOrderByCreatedAtDesc();
        List<Long> durations = pipelineRuns.stream()
                .filter(r -> r.getCompletedAt() != null)
                .map(r -> Duration.between(r.getCreatedAt(), r.getCompletedAt()).toMillis())
                .toList();
        long avgPipelineMs = durations.isEmpty() ? 0 : Math.round(durations.stream().mapToLong(Long::longValue).average().orElse(0));
        long maxPipelineMs = durations.stream().mapToLong(Long::longValue).max().orElse(0);

        List<LoadTestRun> recent = loadTests.findTop50ByOrderByCreatedAtDesc();
        long totalLoadRuns = recent.size();
        long sloPassed = recent.stream().filter(LoadTestRun::isSloPassed).count();
        double avgP95Ms = recent.isEmpty() ? 0 : recent.stream().mapToLong(LoadTestRun::getP95Ms).average().orElse(0);
        double avgThroughput = recent.isEmpty() ? 0 : recent.stream().mapToDouble(LoadTestRun::getThroughputPerSecond).average().orElse(0);
        double avgErrorRate = recent.isEmpty() ? 0 : recent.stream().mapToDouble(LoadTestRun::getErrorRatePercent).average().orElse(0);

        return new PerformanceView(pipelineRuns.size(), avgPipelineMs, maxPipelineMs, totalLoadRuns, sloPassed,
                round(avgP95Ms), round(avgThroughput), round(avgErrorRate));
    }

    /**
     * M20.7 platform activity audit timeline derived from persisted domain records.
     * This is operational traceability, not an immutable compliance ledger.
     */
    @GetMapping("/audit")
    public List<AuditEventView> audit() {
        List<AuditEventView> events = new ArrayList<>();

        for (Company company : companies.findAll()) {
            events.add(new AuditEventView("COMPANY_REGISTERED", company.getId().toString(), company.getName(),
                    "Company workspace registered", company.getCreatedAt()));
        }
        for (ApplicationTarget product : products.findAllByOrderByCreatedAtDesc()) {
            events.add(new AuditEventView("PRODUCT_REGISTERED", safeId(product.getCompanyId()), product.getName(),
                    product.getEnvironment() + " product environment registered", product.getCreatedAt()));
        }
        for (AppUser user : users.findAll()) {
            events.add(new AuditEventView("USER_REGISTERED", safeId(user.getCompanyId()), user.getEmail(),
                    user.getRole().name() + " user registered", user.getCreatedAt()));
        }
        for (PipelineRun run : runs.findAllByOrderByCreatedAtDesc()) {
            events.add(new AuditEventView("UAT_" + run.getStatus().toUpperCase(), run.getCompany(), run.getFileName(),
                    run.getCurrentStage(), run.getCompletedAt() == null ? run.getCreatedAt() : run.getCompletedAt()));
        }
        for (LoadTestRun load : loadTests.findTop50ByOrderByCreatedAtDesc()) {
            events.add(new AuditEventView("LOAD_TEST", "platform", "Performance validation",
                    load.isSloPassed() ? "Load-test SLO passed" : "Load-test SLO failed", load.getCreatedAt()));
        }

        return events.stream()
                .sorted(Comparator.comparing(AuditEventView::occurredAt).reversed())
                .limit(200)
                .toList();
    }

    private OperationView operationView(PipelineRun run) {
        Long durationMillis = duration(run);
        return new OperationView(run.getId(), run.getCompany(), run.getFileName(), run.getStatus(),
                run.getCurrentStage(), run.getCreatedAt(), run.getCompletedAt(), durationMillis,
                run.getErrorMessage() == null || run.getErrorMessage().isBlank() ? null : "Failure recorded");
    }

    private OperationDetailView operationDetailView(PipelineRun run) {
        String result = run.getResultJson();
        String normalized = result == null ? "" : result.toLowerCase(Locale.ROOT);
        boolean resultAvailable = !normalized.isBlank();
        boolean evidenceAvailable = normalized.contains("\"evidence\"") || normalized.contains("\"screenshot\"");
        return new OperationDetailView(run.getId(), run.getCompany(), run.getFileName(), run.getStatus(),
                run.getCurrentStage(), run.getCreatedAt(), run.getCompletedAt(), duration(run),
                safeFailure(run.getErrorMessage()), resultAvailable, evidenceAvailable, releaseDecision(result));
    }

    private Long duration(PipelineRun run) {
        return run.getCompletedAt() == null ? null : Duration.between(run.getCreatedAt(), run.getCompletedAt()).toMillis();
    }

    private String releaseDecision(String resultJson) {
        if (resultJson == null || resultJson.isBlank()) return null;
        Matcher matcher = RELEASE_DECISION_PATTERN.matcher(resultJson);
        return matcher.find() ? matcher.group(1).toUpperCase(Locale.ROOT) : null;
    }

    private String safeFailure(String message) {
        if (message == null || message.isBlank()) return null;
        String safe = message.replaceAll("(?i)(password|token|secret|api[-_ ]?key|authorization)\\s*[:=]\\s*[^\\s,;]+", "$1=[REDACTED]");
        return safe.length() <= 500 ? safe : safe.substring(0, 500) + "…";
    }

    private String safeId(UUID value) { return value == null ? "unassigned" : value.toString(); }
    private double round(double value) { return Math.round(value * 100.0) / 100.0; }

    public record CompanyView(UUID id, String name, String slug, boolean active, int products, int users) {}
    public record ProductView(UUID id, UUID companyId, String name, String environment, String authType, boolean active) {}
    public record UserView(UUID id, UUID companyId, String email, String role, boolean active) {}
    public record OperationView(UUID id, String company, String fileName, String status, String currentStage,
                                Object createdAt, Object completedAt, Long durationMillis, String failureSummary) {}
    public record OperationDetailView(UUID id, String company, String fileName, String status, String currentStage,
                                      Object createdAt, Object completedAt, Long durationMillis, String diagnostic,
                                      boolean resultAvailable, boolean evidenceAvailable, String releaseDecision) {}
    public record FailureView(UUID id, String company, String fileName, String failedStage,
                              Object createdAt, Object failedAt, String diagnostic) {}
    public record PerformanceView(long pipelineRuns, long averagePipelineDurationMs, long maxPipelineDurationMs,
                                  long loadTestRuns, long loadTestSloPassed, double averageLoadP95Ms,
                                  double averageThroughputPerSecond, double averageLoadErrorRatePercent) {}
    public record AuditEventView(String eventType, String scopeId, String subject, String detail, Instant occurredAt) {}
}
