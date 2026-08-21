package com.aiqa.intelligence;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** M31-M34 bounded intelligence endpoints used by the 50-agent organization. */
@RestController
@RequestMapping("/api/intelligence")
public class AutonomousIntelligenceController {
    private final AutonomousIntelligenceService service;

    public AutonomousIntelligenceController(AutonomousIntelligenceService service) {
        this.service = service;
    }

    @PostMapping("/risk-score")
    public AutonomousIntelligenceService.RiskScore risk(@RequestBody AutonomousIntelligenceService.RiskRequest request) {
        return service.risk(request);
    }

    @PostMapping("/change-impact")
    public AutonomousIntelligenceService.ChangeImpact impact(@RequestBody AutonomousIntelligenceService.ChangeImpactRequest request) {
        return service.impact(request);
    }

    @PostMapping("/flaky-assessment")
    public AutonomousIntelligenceService.FlakyAssessment flaky(@RequestBody AutonomousIntelligenceService.FlakyRequest request) {
        return service.flaky(request);
    }

    @PostMapping("/regression-pack")
    public AutonomousIntelligenceService.RegressionPack regression(@RequestBody AutonomousIntelligenceService.RegressionPackRequest request) {
        return service.regression(request);
    }
}
