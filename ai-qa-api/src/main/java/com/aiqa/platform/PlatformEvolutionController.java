package com.aiqa.platform;

import com.aiqa.integration.EnterpriseDeliveryPolicy;
import com.aiqa.integration.EnterpriseEventRouter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only contracts for M37-M50 platform behavior and delivery policy. */
@RestController
@RequestMapping("/api/platform/evolution")
public class PlatformEvolutionController {
    private final PlatformEvolutionCatalog catalog;
    private final EnterpriseEventRouter router;
    private final EnterpriseDeliveryPolicy deliveryPolicy;

    public PlatformEvolutionController(PlatformEvolutionCatalog catalog, EnterpriseEventRouter router, EnterpriseDeliveryPolicy deliveryPolicy) {
        this.catalog = catalog;
        this.router = router;
        this.deliveryPolicy = deliveryPolicy;
    }

    @GetMapping("/milestones")
    public List<PlatformEvolutionCatalog.Milestone> milestones() { return catalog.milestones(); }

    @GetMapping("/routes")
    public List<EnterpriseEventRouter.Route> routes(@RequestParam String eventType) { return router.routesFor(eventType); }

    @GetMapping("/delivery-policy")
    public EnterpriseDeliveryPolicy.DeliveryDecision deliveryPolicy(@RequestParam int attempt, @RequestParam(required = false) Integer statusCode) {
        return deliveryPolicy.decide(attempt, statusCode);
    }
}
