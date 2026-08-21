package com.aiqa.governance;

import com.aiqa.application.ApplicationTargetRepository;
import com.aiqa.pipeline.PipelineRunRepository;
import com.aiqa.security.AppUserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Applies configurable tenant-level safety limits. */
@Service
public class TenantGovernanceService {
    private final TenantGovernancePolicyRepository policies;
    private final AppUserRepository users;
    private final ApplicationTargetRepository products;
    private final PipelineRunRepository runs;

    public TenantGovernanceService(TenantGovernancePolicyRepository policies,
                                   AppUserRepository users,
                                   ApplicationTargetRepository products,
                                   PipelineRunRepository runs) {
        this.policies = policies;
        this.users = users;
        this.products = products;
        this.runs = runs;
    }

    public TenantGovernancePolicy policy(UUID companyId) {
        return policies.findById(companyId).orElseGet(() -> policies.save(new TenantGovernancePolicy(companyId)));
    }

    public TenantGovernancePolicy update(UUID companyId, int maxUsers, int maxProducts, int maxConcurrentUat) {
        TenantGovernancePolicy policy = policy(companyId);
        policy.update(maxUsers, maxProducts, maxConcurrentUat);
        return policies.save(policy);
    }

    public void assertCanAddUser(UUID companyId) {
        TenantGovernancePolicy policy = policy(companyId);
        long current = users.findByCompanyIdOrderByCreatedAtAsc(companyId).size();
        if (current >= policy.getMaxUsers()) throw new IllegalStateException("Company user limit reached");
    }

    public void assertCanAddProduct(UUID companyId) {
        TenantGovernancePolicy policy = policy(companyId);
        long current = products.findByCompanyIdOrderByCreatedAtDesc(companyId).size();
        if (current >= policy.getMaxProducts()) throw new IllegalStateException("Company product limit reached");
    }

    /** Company is the persisted tenant key used by current pipeline runs. */
    public void assertCanStartUat(UUID companyId) {
        TenantGovernancePolicy policy = policy(companyId);
        long concurrent = runs.findByCompanyOrderByCreatedAtDesc(companyId.toString()).stream()
                .filter(r -> "RUNNING".equalsIgnoreCase(r.getStatus()) || "QUEUED".equalsIgnoreCase(r.getStatus()))
                .count();
        if (concurrent >= policy.getMaxConcurrentUat()) throw new IllegalStateException("Concurrent UAT limit reached");
    }
}
