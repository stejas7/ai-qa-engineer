package com.aiqa.testdesign;

import com.aiqa.requirement.Requirement;
import com.aiqa.requirement.RequirementAnalysis;
import com.aiqa.requirement.TestScenario;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Builds an executable, risk-aware test suite from AI requirement analysis.
 *
 * <p>M3 adds deterministic intelligence on top of the AI-generated scenarios:
 * business-rule coverage, critical-flow coverage, explicit negative/boundary
 * coverage and acceptance-criteria traceability. This keeps the final suite
 * explainable and stable even when the configured AI provider changes.</p>
 */
@Service
public class TestDesignService {

    public TestDesignResponse design(Requirement requirement, RequirementAnalysis analysis) {
        List<TestCase> cases = new ArrayList<>();
        Set<String> signatures = new LinkedHashSet<>();
        int n = 1;

        for (TestScenario scenario : safeScenarios(analysis)) {
            if (addIfUnique(cases, signatures, new TestCase(
                    id(n++),
                    scenario.title(),
                    scenario.type(),
                    normalizePriority(scenario.priority(), requirement, scenario.title()),
                    "User is on the relevant application screen and required test data is available.",
                    scenario.steps(),
                    testDataFor(scenario.type()),
                    scenario.expectedResult(),
                    "YES"
            ))) {
                // scenario accepted
            } else {
                n--;
            }
        }

        // M3: every extracted business rule becomes an explicit traceable test.
        int ruleNumber = 1;
        for (String rule : safeList(analysis.businessRules())) {
            if (rule == null || rule.isBlank()) continue;
            String risk = riskPriority(requirement, rule);
            if (addIfUnique(cases, signatures, new TestCase(
                    id(n++),
                    "Business rule " + ruleNumber++ + ": " + shorten(rule, 90),
                    "BUSINESS_RULE",
                    risk,
                    "The business rule and required test data are available.",
                    List.of(
                            "Prepare data that should satisfy the business rule",
                            "Execute the related business flow",
                            "Verify the rule is enforced",
                            "Repeat with data that should violate the rule and verify rejection"
                    ),
                    "Data derived from business rule: " + rule,
                    "The application enforces the business rule exactly as specified.",
                    "YES"
            ))) {
                // rule accepted
            } else {
                n--;
            }
        }

        // M3: guarantee core coverage even if the AI response omitted a category.
        n = ensureCoverage(cases, signatures, n, requirement, "NEGATIVE",
                "Invalid or unauthorized business input is rejected safely",
                List.of("Prepare invalid or unauthorized input", "Execute the business flow", "Verify clear rejection and no unintended state change"),
                "Invalid, malformed, missing and unauthorized data",
                "The invalid request is rejected safely and the system remains consistent.");

        n = ensureCoverage(cases, signatures, n, requirement, "BOUNDARY",
                "Business limits and boundary values are handled correctly",
                List.of("Identify min/max or size/range limits", "Test exact boundary values", "Test just outside each boundary"),
                "Minimum, maximum and just-outside-boundary values",
                "Boundary values behave according to the requirement and invalid limits are rejected.");

        if (isCriticalFlow(requirement)) {
            n = ensureCoverage(cases, signatures, n, requirement, "RISK",
                    "Critical business flow preserves integrity on interruption or retry",
                    List.of("Start the critical business transaction", "Interrupt or retry the operation", "Verify no duplicate, partial or inconsistent business state"),
                    "Critical-flow data with retry/interruption conditions",
                    "The critical flow remains consistent and idempotent under retry or interruption.");
        }

        // M3: explicit acceptance-criteria traceability remains a release gate.
        addIfUnique(cases, signatures, new TestCase(
                id(n),
                "Acceptance criteria traceability coverage",
                "TRACEABILITY",
                "HIGH",
                "Requirement and acceptance criteria are available.",
                List.of("Read each acceptance criterion", "Map it to one or more generated tests", "Verify no criterion is uncovered"),
                "Acceptance criteria from requirement",
                "Every acceptance criterion is covered by at least one executable test.",
                "YES"
        ));

        return new TestDesignResponse(
                requirement.getTitle(),
                "M3 intelligent coverage: AI scenarios + business rules + negative + boundary + risk + traceability",
                cases
        );
    }

    private int ensureCoverage(List<TestCase> cases,
                               Set<String> signatures,
                               int n,
                               Requirement requirement,
                               String type,
                               String title,
                               List<String> steps,
                               String testData,
                               String expectedResult) {
        boolean alreadyCovered = cases.stream().anyMatch(tc -> type.equalsIgnoreCase(tc.type()));
        if (alreadyCovered) return n;

        String priority = "RISK".equals(type) ? "CRITICAL" : riskPriority(requirement, title);
        addIfUnique(cases, signatures, new TestCase(
                id(n++), title, type, priority,
                "Relevant business flow and test data are available.",
                steps, testData, expectedResult, "YES"));
        return n;
    }

    private boolean addIfUnique(List<TestCase> cases, Set<String> signatures, TestCase testCase) {
        String signature = (testCase.type() + "|" + testCase.title()).toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
        if (!signatures.add(signature)) return false;
        cases.add(testCase);
        return true;
    }

    private List<TestScenario> safeScenarios(RequirementAnalysis analysis) {
        return analysis == null || analysis.testScenarios() == null ? List.of() : analysis.testScenarios();
    }

    private List<String> safeList(List<String> values) {
        return values == null ? List.of() : values;
    }

    private String normalizePriority(String priority, Requirement requirement, String scenarioTitle) {
        String inferred = riskPriority(requirement, scenarioTitle);
        if ("CRITICAL".equals(inferred)) return "CRITICAL";
        if (priority == null || priority.isBlank()) return inferred;
        return priority.toUpperCase(Locale.ROOT);
    }

    private String riskPriority(Requirement requirement, String text) {
        String source = ((requirement == null ? "" : requirement.getTitle() + " " + requirement.getDescription()) + " " + text)
                .toLowerCase(Locale.ROOT);
        if (containsAny(source, "payment", "checkout", "money", "refund", "transfer", "security", "authentication", "login", "permission", "role", "delete", "privacy", "pii")) {
            return "CRITICAL";
        }
        if (containsAny(source, "order", "registration", "account", "customer", "inventory", "booking", "approval", "notification")) {
            return "HIGH";
        }
        return "MEDIUM";
    }

    private boolean isCriticalFlow(Requirement requirement) {
        return "CRITICAL".equals(riskPriority(requirement, ""));
    }

    private boolean containsAny(String source, String... terms) {
        for (String term : terms) if (source.contains(term)) return true;
        return false;
    }

    private String id(int n) {
        return String.format("TC-%03d", n);
    }

    private String shorten(String value, int max) {
        String compact = value.replaceAll("\\s+", " ").trim();
        return compact.length() <= max ? compact : compact.substring(0, max - 1) + "…";
    }

    private String testDataFor(String type) {
        return switch (type == null ? "" : type.toUpperCase(Locale.ROOT)) {
            case "NEGATIVE" -> "Missing, invalid, malformed and unauthorized input values";
            case "BOUNDARY" -> "Minimum, maximum and just-outside-boundary values";
            case "SECURITY" -> "Unauthorized user and restricted business data";
            case "RISK" -> "Critical business data with retry and interruption conditions";
            default -> "Valid business data representing a normal customer journey";
        };
    }
}
