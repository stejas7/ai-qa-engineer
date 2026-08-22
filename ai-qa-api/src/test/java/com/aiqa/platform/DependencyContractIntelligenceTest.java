package com.aiqa.platform;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DependencyContractIntelligenceTest {
    private final DependencyContractIntelligence service = new DependencyContractIntelligence();

    @Test void blastRadiusTraversesDependencies() {
        var result = service.blastRadius("checkout", List.of(
                new DependencyContractIntelligence.DependencyEdge("checkout","payment"),
                new DependencyContractIntelligence.DependencyEdge("payment","ledger"),
                new DependencyContractIntelligence.DependencyEdge("checkout","inventory")
        ));
        assertEquals(3, result.impactedCount());
        assertTrue(result.impactedComponents().contains("ledger"));
    }

    @Test void contractDetectsBreakingRequiredFieldChange() {
        var result = service.assessContract(
                new DependencyContractIntelligence.ContractSnapshot("1", Set.of("id","amount")),
                new DependencyContractIntelligence.ContractSnapshot("2", Set.of("id","currency"))
        );
        assertTrue(result.breaking());
        assertEquals("BLOCK", result.decision());
    }

    @Test void schemaDriftAndReadinessAreExplainable() {
        var drift = service.schemaDrift(Map.of("amount","number"), Map.of("amount","string"));
        assertEquals(1, drift.drift().size());
        var readiness = service.environmentReadiness(4,4,true,true,true);
        assertEquals("READY", readiness.state());
    }

    @Test void optimizerRespectsBudgetAndEvidenceConfidence() {
        var selected = service.optimizeSelection(List.of(
                new DependencyContractIntelligence.TestCandidate("critical",1.0,1.0,10),
                new DependencyContractIntelligence.TestCandidate("low",0.2,0.3,10)
        ),10);
        assertEquals(List.of("critical"), selected.stream().map(DependencyContractIntelligence.TestCandidate::id).toList());
        assertEquals("HIGH", service.evidenceConfidence(10,10,true,true,true).level());
    }

    @Test void defectsDeduplicateAndReleaseMemoryLearns() {
        var groups = service.deduplicateDefects(List.of(
                new DependencyContractIntelligence.DefectSignal("D1","Checkout","Null Pointer"),
                new DependencyContractIntelligence.DefectSignal("D2","Checkout","null-pointer")
        ));
        assertEquals(1, groups.size());
        assertEquals(2, groups.getFirst().count());
        var memory = service.crossReleaseMemory(List.of(
                new DependencyContractIntelligence.ReleaseOutcome("R1",0.8,"BLOCKED"),
                new DependencyContractIntelligence.ReleaseOutcome("R2",0.2,"READY")
        ));
        assertEquals("TIGHTEN_GATES", memory.recommendation());
    }
}
