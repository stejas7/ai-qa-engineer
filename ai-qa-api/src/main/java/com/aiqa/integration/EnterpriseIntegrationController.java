package com.aiqa.integration;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Enterprise integration capability discovery and tenant-safe planning.
 *
 * @author Tejas Shah
 */
@RestController
@RequestMapping("/api/integrations/enterprise")
public class EnterpriseIntegrationController {

    private final EnterpriseIntegrationCatalog catalog;
    private final EnterpriseIntegrationPlanService plans;

    public EnterpriseIntegrationController(EnterpriseIntegrationCatalog catalog,
                                           EnterpriseIntegrationPlanService plans) {
        this.catalog = catalog;
        this.plans = plans;
    }

    /** Returns safe metadata for supported enterprise providers. */
    @GetMapping("/catalog")
    public List<EnterpriseIntegrationCatalog.ProviderDefinition> catalog() {
        return catalog.providers();
    }

    /** Builds a tenant-bound integration plan without accepting raw provider secrets. */
    @PostMapping("/plan")
    public EnterpriseIntegrationPlanService.IntegrationPlan plan(Authentication authentication,
                                                                  @RequestBody PlanRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("integration plan request is required");
        }
        return plans.plan(authentication.getName(), request.providerKey(), request.action(), request.eventType());
    }

    /** Request contract for enterprise integration planning. */
    public record PlanRequest(String providerKey, String action, String eventType) {
    }
}
