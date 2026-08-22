package com.aiqa.platform;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

/** REST surface for M71-M80 business-journey intelligence. */
@RestController
@RequestMapping("/api/platform/journey-intelligence")
public class BusinessJourneyIntelligenceController {
    private final BusinessJourneyIntelligence service;

    public BusinessJourneyIntelligenceController(BusinessJourneyIntelligence service){this.service=service;}

    @PostMapping("/model")
    public BusinessJourneyIntelligence.JourneyModel model(@RequestBody List<BusinessJourneyIntelligence.JourneyStep> steps){return service.journeyModel(steps);}

    @PostMapping("/critical-path")
    public List<BusinessJourneyIntelligence.JourneyStep> critical(@RequestBody CriticalRequest r){return service.criticalPath(r.steps(),r.maxSteps());}

    @PostMapping("/accessibility")
    public BusinessJourneyIntelligence.AccessibilityAssessment accessibility(@RequestBody AccessibilityRequest r){return service.accessibility(r.totalChecks(),r.passedChecks(),r.criticalViolations());}

    @PostMapping("/security-regression")
    public BusinessJourneyIntelligence.RegressionPlan security(@RequestBody List<BusinessJourneyIntelligence.ChangeSignal> changes){return service.securityRegression(changes);}

    @PostMapping("/performance-regression")
    public BusinessJourneyIntelligence.RegressionPlan performance(@RequestBody List<BusinessJourneyIntelligence.ChangeSignal> changes){return service.performanceRegression(changes);}

    @PostMapping("/locale-coverage")
    public BusinessJourneyIntelligence.LocaleCoverage locale(@RequestBody LocaleRequest r){return service.localeCoverage(r.requiredLocales(),r.coveredLocales());}

    @PostMapping("/device-matrix")
    public List<BusinessJourneyIntelligence.DeviceTarget> matrix(@RequestBody DeviceRequest r){return service.deviceMatrix(r.browsers(),r.viewports(),r.maxTargets());}

    @PostMapping("/personas")
    public List<BusinessJourneyIntelligence.Persona> personas(@RequestBody PersonaRequest r){return service.syntheticPersonas(r.roles(),r.maxPersonas());}

    @PostMapping("/negative-paths")
    public List<BusinessJourneyIntelligence.NegativePath> negative(@RequestBody NegativeRequest r){return service.negativePaths(r.rules(),r.maxCases());}

    @PostMapping("/ambiguity")
    public BusinessJourneyIntelligence.AmbiguityAssessment ambiguity(@RequestBody RequirementRequest r){return service.ambiguity(r.requirement());}

    public record CriticalRequest(List<BusinessJourneyIntelligence.JourneyStep> steps,int maxSteps){}
    public record AccessibilityRequest(int totalChecks,int passedChecks,int criticalViolations){}
    public record LocaleRequest(Set<String> requiredLocales,Set<String> coveredLocales){}
    public record DeviceRequest(Set<String> browsers,Set<String> viewports,int maxTargets){}
    public record PersonaRequest(List<String> roles,int maxPersonas){}
    public record NegativeRequest(List<BusinessJourneyIntelligence.FieldRule> rules,int maxCases){}
    public record RequirementRequest(String requirement){}
}
