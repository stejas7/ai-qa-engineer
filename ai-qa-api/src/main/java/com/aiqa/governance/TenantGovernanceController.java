package com.aiqa.governance;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Platform-owner controls for per-company governance limits. */
@RestController
@RequestMapping("/api/platform/governance")
public class TenantGovernanceController {
    private final TenantGovernanceService governance;

    public TenantGovernanceController(TenantGovernanceService governance) {
        this.governance = governance;
    }

    @GetMapping("/companies/{companyId}")
    public PolicyView get(@PathVariable UUID companyId) {
        return PolicyView.from(governance.policy(companyId));
    }

    @PutMapping("/companies/{companyId}")
    public PolicyView update(@PathVariable UUID companyId, @RequestBody UpdatePolicyRequest request) {
        if (request == null) throw new IllegalArgumentException("policy request is required");
        return PolicyView.from(governance.update(companyId, request.maxUsers(), request.maxProducts(), request.maxConcurrentUat()));
    }

    public record UpdatePolicyRequest(int maxUsers, int maxProducts, int maxConcurrentUat) {}
    public record PolicyView(UUID companyId, int maxUsers, int maxProducts, int maxConcurrentUat, String updatedAt) {
        static PolicyView from(TenantGovernancePolicy policy) {
            return new PolicyView(policy.getCompanyId(), policy.getMaxUsers(), policy.getMaxProducts(),
                    policy.getMaxConcurrentUat(), policy.getUpdatedAt().toString());
        }
    }
}
