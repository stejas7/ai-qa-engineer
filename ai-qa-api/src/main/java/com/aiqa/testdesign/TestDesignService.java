package com.aiqa.testdesign;

import com.aiqa.requirement.Requirement;
import com.aiqa.requirement.RequirementAnalysis;
import com.aiqa.requirement.TestScenario;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TestDesignService {

    public TestDesignResponse design(Requirement requirement, RequirementAnalysis analysis) {
        List<TestCase> cases = new ArrayList<>();
        int n = 1;

        for (TestScenario scenario : analysis.testScenarios()) {
            cases.add(new TestCase(
                    String.format("TC-%03d", n++),
                    scenario.title(),
                    scenario.type(),
                    scenario.priority(),
                    "User is on the relevant application screen and required test data is available.",
                    scenario.steps(),
                    testDataFor(scenario.type()),
                    scenario.expectedResult(),
                    "YES"
            ));
        }

        cases.add(new TestCase(
                String.format("TC-%03d", n),
                "Acceptance criteria coverage",
                "TRACEABILITY",
                "HIGH",
                "Requirement and acceptance criteria are available.",
                List.of("Read each acceptance criterion", "Map it to at least one test", "Verify no criterion is uncovered"),
                "Acceptance criteria from requirement",
                "Every acceptance criterion is covered by one or more executable tests.",
                "YES"
        ));

        return new TestDesignResponse(
                requirement.getTitle(),
                "Functional + negative + boundary + traceability coverage",
                cases
        );
    }

    private String testDataFor(String type) {
        return switch (type == null ? "" : type.toUpperCase()) {
            case "NEGATIVE" -> "Missing, invalid and malformed input values";
            case "BOUNDARY" -> "Minimum, maximum and just-outside-boundary values";
            default -> "Valid business data representing a normal customer journey";
        };
    }
}
