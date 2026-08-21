package com.aiqa.platform;

import com.aiqa.company.CompanyRepository;
import com.aiqa.governance.SecurityAuditEventRepository;
import com.aiqa.governance.TenantGovernancePolicyRepository;
import com.aiqa.integration.IntegrationEndpointRepository;
import com.aiqa.release.ReleaseApproval;
import com.aiqa.release.ReleaseApprovalRepository;
import com.aiqa.security.AppUserRepository;
import com.aiqa.security.UserRole;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Final M30 platform readiness gate built from persisted security/governance/operations facts. */
@RestController
@RequestMapping("/api/platform/enterprise-readiness")
public class EnterpriseReadinessController {
    private final CompanyRepository companies;
    private final TenantGovernancePolicyRepository policies;
    private final IntegrationEndpointRepository integrations;
    private final ReleaseApprovalRepository approvals;
    private final SecurityAuditEventRepository auditEvents;
    private final AppUserRepository users;

    public EnterpriseReadinessController(CompanyRepository companies,
                                         TenantGovernancePolicyRepository policies,
                                         IntegrationEndpointRepository integrations,
                                         ReleaseApprovalRepository approvals,
                                         SecurityAuditEventRepository auditEvents,
                                         AppUserRepository users) {
        this.companies = companies;
        this.policies = policies;
        this.integrations = integrations;
        this.approvals = approvals;
        this.auditEvents = auditEvents;
        this.users = users;
    }

    @GetMapping
    public ReadinessView readiness() {
        List<CheckView> checks = new ArrayList<>();
        long platformOwners = users.countByRoleAndActiveTrue(UserRole.SUPER_ADMIN)
                + users.countByRoleAndActiveTrue(UserRole.PLATFORM_ADMIN);
        checks.add(new CheckView("platformOwner", platformOwners > 0, platformOwners + " active platform owner(s)"));

        long companyCount = companies.count();
        long policyCount = policies.count();
        boolean governanceCovered = companyCount == 0 || policyCount >= companyCount;
        checks.add(new CheckView("tenantGovernance", governanceCovered,
                policyCount + "/" + companyCount + " companies have persisted governance policy"));

        long integrationCount = integrations.count();
        checks.add(new CheckView("integrations", true, integrationCount + " integration endpoint(s) configured"));

        long pending = approvals.findAll().stream()
                .filter(a -> a.getDecision() == ReleaseApproval.Decision.PENDING)
                .count();
        checks.add(new CheckView("releaseGovernance", pending == 0, pending + " pending release approval(s)"));

        long auditCount = auditEvents.count();
        checks.add(new CheckView("securityAudit", auditCount > 0, auditCount + " durable mutation audit event(s)"));

        Runtime runtime = Runtime.getRuntime();
        long used = runtime.totalMemory() - runtime.freeMemory();
        long max = runtime.maxMemory();
        double heapPercent = max <= 0 ? 0.0 : Math.round((used * 1000.0 / max)) / 10.0;
        checks.add(new CheckView("memory", heapPercent < 90.0, heapPercent + "% JVM max heap used"));

        long passed = checks.stream().filter(CheckView::passed).count();
        boolean ready = checks.stream().allMatch(CheckView::passed);
        return new ReadinessView(ready ? "READY" : "NEEDS_ATTENTION", passed, checks.size(), checks, Instant.now().toString());
    }

    public record CheckView(String id, boolean passed, String detail) {}
    public record ReadinessView(String status, long passedChecks, long totalChecks, List<CheckView> checks, String generatedAt) {}
}
