package com.aiqa.quality;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QualityGateServiceTest {
    private final QualityGateService service = new QualityGateService();

    @Test
    void approvesWhenAllTestsPassAndRequirementsAreCovered() {
        QualityGateResponse response = service.evaluate(new QualityGateRequest(10, 10, 0, 10, 4, 4));
        assertEquals("APPROVED", response.decision());
        assertEquals(100.0, response.passRate());
        assertEquals(100.0, response.requirementCoverage());
    }

    @Test
    void blocksWhenAnyTestFails() {
        QualityGateResponse response = service.evaluate(new QualityGateRequest(10, 9, 1, 10, 4, 4));
        assertEquals("BLOCKED", response.decision());
    }

    @Test
    void blocksWhenRequirementCoverageIsIncomplete() {
        QualityGateResponse response = service.evaluate(new QualityGateRequest(10, 10, 0, 10, 4, 3));
        assertEquals("BLOCKED", response.decision());
    }
}
