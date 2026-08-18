package com.aiqa.testdesign;

import com.aiqa.requirement.Requirement;
import com.aiqa.requirement.RequirementAnalysis;
import com.aiqa.requirement.TestScenario;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestDesignServiceTest {

    private final TestDesignService service = new TestDesignService();

    @Test
    void generatesRiskBusinessRuleBoundaryNegativeAndTraceabilityCoverage() {
        Requirement requirement = new Requirement();
        requirement.setTitle("Customer checkout");
        requirement.setDescription("Customer pays for an order and receives confirmation.");
        requirement.setAcceptanceCriteria(List.of("Payment must create exactly one confirmed order."));

        RequirementAnalysis analysis = new RequirementAnalysis(
                "Checkout flow",
                List.of("A successful payment creates one order only."),
                List.of(),
                List.of(new TestScenario(
                        "AI-1",
                        "Successful checkout",
                        "FUNCTIONAL",
                        "HIGH",
                        List.of("Add item", "Checkout", "Pay"),
                        "Order confirmation is shown."
                ))
        );

        TestDesignResponse response = service.design(requirement, analysis);

        assertThat(response.strategy()).contains("M3 intelligent coverage");
        assertThat(response.testCases()).extracting(TestCase::type)
                .contains("FUNCTIONAL", "BUSINESS_RULE", "NEGATIVE", "BOUNDARY", "RISK", "TRACEABILITY");
        assertThat(response.testCases())
                .filteredOn(tc -> "RISK".equals(tc.type()))
                .allMatch(tc -> "CRITICAL".equals(tc.priority()));
        assertThat(response.testCases())
                .filteredOn(tc -> "FUNCTIONAL".equals(tc.type()))
                .allMatch(tc -> "CRITICAL".equals(tc.priority()));
    }
}
