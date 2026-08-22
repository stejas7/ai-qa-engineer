package com.aiqa.platform;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PortfolioWorkforceOrchestrationTest {
    private final PortfolioWorkforceOrchestration service = new PortfolioWorkforceOrchestration();

    @Test void coordinatesProductsAndReleaseWaves() {
        var portfolio = service.coordinateProducts(List.of(
                new PortfolioWorkforceOrchestration.ProductMission("A",0.9,10),
                new PortfolioWorkforceOrchestration.ProductMission("B",0.4,5)
        ),1);
        assertEquals("A", portfolio.active().getFirst().productId());
        assertEquals(1, portfolio.queued().size());
        var train = service.releaseTrain(List.of(
                new PortfolioWorkforceOrchestration.ReleaseCandidate("R1","2026-09-01",0.2),
                new PortfolioWorkforceOrchestration.ReleaseCandidate("R2","2026-09-02",0.8)
        ),1);
        assertEquals(2, train.waveCount());
    }

    @Test void forecastsCapacityCostAndWorkforce() {
        assertEquals("OVER_CAPACITY", service.tenantCapacity(8,10,10,1.2).state());
        assertEquals(5500.0, service.costForecast(100,50,0,10).totalForecast());
        assertEquals("SCALE_REQUIRED", service.workforceCapacity(5,4,10,0.8).state());
    }

    @Test void skillRoutingAndAgentScoringAreBounded() {
        var assignments = service.routeSkills(Set.of("rag","playwright"), List.of(
                new PortfolioWorkforceOrchestration.AgentProfile("A1",Set.of("rag","playwright"),0.9),
                new PortfolioWorkforceOrchestration.AgentProfile("A2",Set.of("rag"),0.8)
        ),1);
        assertEquals("A1", assignments.getFirst().agentId());
        var score = service.scoreAgent(new PortfolioWorkforceOrchestration.AgentPerformance("A1",0.95,0.9,0.9,0.05));
        assertTrue(score.score() > 85);
    }

    @Test void calibrationSlaAndExecutivePortfolioAreExplainable() {
        var calibration = service.calibrate(List.of(
                new PortfolioWorkforceOrchestration.AgentScorecard("A1",80,"GOOD"),
                new PortfolioWorkforceOrchestration.AgentScorecard("A2",90,"EXCELLENT")
        ),90);
        assertEquals(85.0, calibration.currentMean());
        assertEquals("CALIBRATED", calibration.recommendation());
        assertEquals("FAST", service.missionSla(10,5,5,10,10).tier());
        var executive = service.executivePortfolio(List.of(
                new PortfolioWorkforceOrchestration.PortfolioRelease("R1",0.2,"READY"),
                new PortfolioWorkforceOrchestration.PortfolioRelease("R2",0.8,"BLOCKED")
        ));
        assertEquals("ATTENTION", executive.state());
    }
}
