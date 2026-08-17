package com.aiqa.impact;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Performs deterministic V12 change-impact analysis.
 *
 * <p>The service intentionally makes no unsafe deployment decision. It converts
 * changed source paths and diff hints into an explainable risk score and a
 * recommended regression scope. The V12 AI layer can consume this contract later
 * without changing the CI/CD boundary.</p>
 */
@Service
public class ImpactAnalysisService {

    /**
     * Analyse a set of changed files and recommend the smallest safe regression scope.
     *
     * @param request changed files and optional unified diff
     * @return explainable impact assessment
     */
    public ImpactAnalysisResponse analyze(ImpactAnalysisRequest request) {
        int score = 0;
        List<String> areas = new ArrayList<>();
        List<String> suites = new ArrayList<>();
        List<String> reasons = new ArrayList<>();

        for (String rawFile : request.changedFiles()) {
            String file = rawFile == null ? "" : rawFile.toLowerCase(Locale.ROOT);
            if (file.isBlank()) {
                continue;
            }

            if (isDeploymentOrWorkflow(file)) {
                score += 35;
                add(areas, "CI/CD and deployment");
                add(suites, "deployment-smoke");
                reasons.add(rawFile + " can change delivery behavior");
            }
            if (isApi(file)) {
                score += 30;
                add(areas, "API and backend behavior");
                add(suites, "api-regression");
                reasons.add(rawFile + " can change API behavior");
            }
            if (isUi(file)) {
                score += 25;
                add(areas, "UI and browser behavior");
                add(suites, "ui-regression");
                reasons.add(rawFile + " can change browser-visible behavior");
            }
            if (isExecutionOrAgent(file)) {
                score += 35;
                add(areas, "agent execution and orchestration");
                add(suites, "agentic-uat-regression");
                reasons.add(rawFile + " can change autonomous execution behavior");
            }
            if (isSecurityOrPolicy(file)) {
                score += 45;
                add(areas, "security and governance");
                add(suites, "governance-regression");
                reasons.add(rawFile + " can change protected or governed actions");
            }
            if (isDatabase(file)) {
                score += 30;
                add(areas, "persistence and data");
                add(suites, "data-regression");
                reasons.add(rawFile + " can change persisted application behavior");
            }
            if (isTestOnly(file)) {
                score += 5;
                reasons.add(rawFile + " is test-only and has limited runtime impact");
            }
        }

        String diff = request.diff() == null ? "" : request.diff().toLowerCase(Locale.ROOT);
        if (diff.contains("security") || diff.contains("authentication") || diff.contains("authorization")) {
            score += 25;
            add(areas, "security-sensitive behavior");
            add(suites, "security-regression");
            reasons.add("diff contains security-sensitive behavior");
        }
        if (diff.contains("docker") || diff.contains("workflow") || diff.contains("deploy")) {
            score += 20;
            add(suites, "deployment-smoke");
            reasons.add("diff contains delivery/deployment changes");
        }

        score = Math.min(score, 100);
        boolean fullRegression = score >= 70 || request.changedFiles().size() >= 8;
        if (fullRegression) {
            add(suites, "full-regression");
            reasons.add("impact threshold requires full regression");
        }
        if (areas.isEmpty()) {
            areas.add("general application change");
            suites.add("smoke");
            reasons.add("no specialized impact rule matched; smoke coverage is recommended");
        }

        return new ImpactAnalysisResponse(
                risk(score),
                score,
                request.changedFiles(),
                List.copyOf(areas),
                List.copyOf(suites),
                List.copyOf(reasons),
                fullRegression);
    }

    private static boolean isDeploymentOrWorkflow(String file) {
        return file.contains(".github/workflows/") || file.contains("docker") || file.contains("compose") || file.endsWith(".yml") || file.endsWith(".yaml");
    }

    private static boolean isApi(String file) {
        return file.contains("controller") || file.contains("/api/") || file.contains("request") || file.contains("response");
    }

    private static boolean isUi(String file) {
        return file.contains("frontend") || file.contains("src/main/resources/static") || file.contains("playwright") || file.contains("selenium");
    }

    private static boolean isExecutionOrAgent(String file) {
        return file.contains("execution") || file.contains("agent") || file.contains("orchestrat") || file.contains("automation");
    }

    private static boolean isSecurityOrPolicy(String file) {
        return file.contains("security") || file.contains("policy") || file.contains("approval") || file.contains("auth");
    }

    private static boolean isDatabase(String file) {
        return file.contains("repository") || file.contains("entity") || file.contains("migration") || file.contains("schema") || file.contains("database");
    }

    private static boolean isTestOnly(String file) {
        return file.contains("/test/") || file.startsWith("test/") || file.endsWith("test.java");
    }

    private static void add(List<String> values, String value) {
        if (!values.contains(value)) {
            values.add(value);
        }
    }

    private static String risk(int score) {
        if (score >= 70) {
            return "HIGH";
        }
        if (score >= 35) {
            return "MEDIUM";
        }
        return "LOW";
    }
}
