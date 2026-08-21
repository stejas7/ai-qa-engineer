package com.aiqa.platform;

import com.aiqa.application.ApplicationTargetRepository;
import com.aiqa.company.CompanyRepository;
import com.aiqa.pipeline.PipelineRun;
import com.aiqa.pipeline.PipelineRunRepository;
import com.aiqa.release.ReleaseApproval;
import com.aiqa.release.ReleaseApprovalRepository;
import com.aiqa.security.AppUserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/** Platform-owner release/adoption intelligence without exposing tenant secrets. */
@RestController
@RequestMapping("/api/platform/analytics")
public class PlatformAnalyticsController {
    private final CompanyRepository companies;
    private final ApplicationTargetRepository products;
    private final AppUserRepository users;
    private final PipelineRunRepository runs;
    private final ReleaseApprovalRepository approvals;

    public PlatformAnalyticsController(CompanyRepository companies,
                                       ApplicationTargetRepository products,
                                       AppUserRepository users,
                                       PipelineRunRepository runs,
                                       ReleaseApprovalRepository approvals) {
        this.companies = companies;
        this.products = products;
        this.users = users;
        this.runs = runs;
        this.approvals = approvals;
    }

    @GetMapping("/summary")
    public AnalyticsView summary() {
        Instant since7d = Instant.now().minus(7, ChronoUnit.DAYS);
        List<PipelineRun> allRuns = runs.findAllByOrderByCreatedAtDesc();
        long completed = allRuns.stream().filter(r -> "COMPLETED".equalsIgnoreCase(r.getStatus())).count();
        long failed = allRuns.stream().filter(r -> "FAILED".equalsIgnoreCase(r.getStatus())).count();
        long terminal = completed + failed;
        double runSuccessRate = terminal == 0 ? 100.0 : round(completed * 100.0 / terminal);
        long runs7d = allRuns.stream().filter(r -> r.getCreatedAt() != null && r.getCreatedAt().isAfter(since7d)).count();

        List<ReleaseApproval> allApprovals = approvals.findAll();
        long approved = allApprovals.stream().filter(a -> a.getDecision() == ReleaseApproval.Decision.APPROVED).count();
        long blocked = allApprovals.stream().filter(a -> a.getDecision() == ReleaseApproval.Decision.BLOCKED).count();
        long pending = allApprovals.stream().filter(a -> a.getDecision() == ReleaseApproval.Decision.PENDING).count();

        return new AnalyticsView(
                companies.count(),
                products.count(),
                users.count(),
                allRuns.size(),
                runs7d,
                completed,
                failed,
                runSuccessRate,
                allApprovals.size(),
                approved,
                blocked,
                pending,
                Instant.now().toString()
        );
    }

    private double round(double value) { return Math.round(value * 10.0) / 10.0; }

    public record AnalyticsView(long companies, long products, long users, long totalUatRuns, long uatRuns7d,
                                long completedRuns, long failedRuns, double runSuccessRate,
                                long releaseApprovals, long approvedReleases, long blockedReleases,
                                long pendingApprovals, String generatedAt) {}
}
