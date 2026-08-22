package com.aiqa.platform;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** REST surface for M61-M70 dependency and contract intelligence. */
@RestController
@RequestMapping("/api/platform/intelligence")
public class DependencyContractIntelligenceController {
    private final DependencyContractIntelligence service;

    public DependencyContractIntelligenceController(DependencyContractIntelligence service) { this.service = service; }

    @PostMapping("/dependency-graph")
    public DependencyContractIntelligence.DependencyGraph graph(@RequestBody List<DependencyContractIntelligence.DependencyEdge> edges){return service.dependencyGraph(edges);}

    @PostMapping("/blast-radius")
    public DependencyContractIntelligence.BlastRadius blast(@RequestBody BlastRequest request){return service.blastRadius(request.changedComponent(), request.edges());}

    @PostMapping("/contract")
    public DependencyContractIntelligence.ContractAssessment contract(@RequestBody ContractRequest request){return service.assessContract(request.expected(), request.actual());}

    @PostMapping("/schema-drift")
    public DependencyContractIntelligence.DriftAssessment schema(@RequestBody SchemaRequest request){return service.schemaDrift(request.expected(), request.actual());}

    @PostMapping("/environment-readiness")
    public DependencyContractIntelligence.EnvironmentReadiness readiness(@RequestBody ReadinessRequest r){return service.environmentReadiness(r.healthyDependencies(), r.totalDependencies(), r.schemaStable(), r.secretsConfigured(), r.testDataReady());}

    @PostMapping("/test-selection")
    public List<DependencyContractIntelligence.TestCandidate> select(@RequestBody SelectionRequest r){return service.optimizeSelection(r.tests(), r.budgetMinutes());}

    @PostMapping("/evidence-confidence")
    public DependencyContractIntelligence.EvidenceConfidence evidence(@RequestBody EvidenceRequest r){return service.evidenceConfidence(r.assertions(), r.passedAssertions(), r.screenshot(), r.trace(), r.video());}

    @PostMapping("/defect-deduplication")
    public List<DependencyContractIntelligence.DefectGroup> defects(@RequestBody List<DependencyContractIntelligence.DefectSignal> defects){return service.deduplicateDefects(defects);}

    @PostMapping("/release-memory")
    public DependencyContractIntelligence.ReleaseMemory memory(@RequestBody List<DependencyContractIntelligence.ReleaseOutcome> history){return service.crossReleaseMemory(history);}

    public record BlastRequest(String changedComponent, List<DependencyContractIntelligence.DependencyEdge> edges){}
    public record ContractRequest(DependencyContractIntelligence.ContractSnapshot expected, DependencyContractIntelligence.ContractSnapshot actual){}
    public record SchemaRequest(Map<String,String> expected, Map<String,String> actual){}
    public record ReadinessRequest(int healthyDependencies,int totalDependencies,boolean schemaStable,boolean secretsConfigured,boolean testDataReady){}
    public record SelectionRequest(List<DependencyContractIntelligence.TestCandidate> tests,int budgetMinutes){}
    public record EvidenceRequest(int assertions,int passedAssertions,boolean screenshot,boolean trace,boolean video){}
}
