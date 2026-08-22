package com.aiqa.platform;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BusinessJourneyIntelligenceTest {
    private final BusinessJourneyIntelligence service = new BusinessJourneyIntelligence();

    @Test void modelsAndSelectsCriticalJourney() {
        var steps = List.of(
                new BusinessJourneyIntelligence.JourneyStep("Login",1,true,"session"),
                new BusinessJourneyIntelligence.JourneyStep("Browse",2,false,"catalog"),
                new BusinessJourneyIntelligence.JourneyStep("Checkout",3,true,"order")
        );
        assertEquals(2, service.journeyModel(steps).criticalSteps());
        assertEquals(List.of("Login","Checkout"), service.criticalPath(steps,5).stream().map(BusinessJourneyIntelligence.JourneyStep::name).toList());
    }

    @Test void accessibilityBlocksCriticalViolations() {
        assertEquals("BLOCK", service.accessibility(100,99,1).state());
        assertEquals("PASS", service.accessibility(100,96,0).state());
    }

    @Test void securityAndPerformancePlansFocusRisk() {
        var changes = List.of(
                new BusinessJourneyIntelligence.ChangeSignal("auth-service","session token refresh",0.5),
                new BusinessJourneyIntelligence.ChangeSignal("catalog-query","database query cache",0.5)
        );
        assertTrue(service.securityRegression(changes).components().contains("auth-service"));
        assertTrue(service.performanceRegression(changes).components().contains("catalog-query"));
    }

    @Test void localeDevicePersonaAndNegativePlansAreBounded() {
        assertEquals(List.of("fr-fr"), service.localeCoverage(Set.of("en-us","fr-fr"),Set.of("en-us")).missingLocales());
        assertEquals(2, service.deviceMatrix(Set.of("chromium","firefox"),Set.of("desktop","mobile"),2).size());
        assertEquals(1, service.syntheticPersonas(List.of("Admin","Buyer"),1).size());
        var negatives = service.negativePaths(List.of(new BusinessJourneyIntelligence.FieldRule("amount",true,3,true)),10);
        assertEquals(3, negatives.size());
    }

    @Test void ambiguitySignalsVagueRequirements() {
        var result = service.ambiguity("The page should load quickly and be user friendly");
        assertNotEquals("CLEAR", result.state());
        assertTrue(result.signals().contains("quickly"));
    }
}
