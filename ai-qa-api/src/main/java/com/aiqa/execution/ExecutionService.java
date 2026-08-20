package com.aiqa.execution;

import com.aiqa.healing.FailureCategory;
import com.aiqa.healing.FailureClassifier;
import com.aiqa.healing.HealingDecisionService;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitUntilState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Deterministic browser execution engine used by AI UAT Engineer. */
@Service
public class ExecutionService {
    private final ExecutionRecordRepository records;
    private final FailureClassifier failureClassifier;
    private final HealingDecisionService healingDecisions;
    private final ExecutionEvidenceStore evidenceStore;

    /** Compatibility constructor retained for unit tests that do not exercise evidence persistence. */
    public ExecutionService(ExecutionRecordRepository records,
                            FailureClassifier failureClassifier,
                            HealingDecisionService healingDecisions) {
        this(records, failureClassifier, healingDecisions, null);
    }

    @Autowired
    public ExecutionService(ExecutionRecordRepository records,
                            FailureClassifier failureClassifier,
                            HealingDecisionService healingDecisions,
                            ExecutionEvidenceStore evidenceStore) {
        this.records = records;
        this.failureClassifier = failureClassifier;
        this.healingDecisions = healingDecisions;
        this.evidenceStore = evidenceStore;
    }

    public ExecutionResponse run(ExecutionRequest request) {
        long start = System.currentTimeMillis();
        Path evidenceDir = Path.of("evidence");
        String base = request.testId().replaceAll("[^A-Za-z0-9_-]", "_") + "-" + UUID.randomUUID();
        String beforeFile = base + "-before.png";
        String afterFile = base + "-after.png";
        Page page = null;
        try {
            Files.createDirectories(evidenceDir);
            try (Playwright playwright = Playwright.create()) {
                boolean headless = request.headless() == null || request.headless();
                try (Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
                     BrowserContext context = browser.newContext()) {
                    page = context.newPage();
                    navigate(page, request.url());
                    for (String step : request.steps() == null ? List.<String>of() : request.steps()) {
                        executeWithHealing(page, request.testId(), step, evidenceDir, beforeFile);
                    }
                    if (request.expectedResult() != null && !request.expectedResult().isBlank()) verify(page, request.expectedResult());
                    capture(page, evidenceDir.resolve(afterFile));
                }
            }
            return persist(request, "PASS", start, evidenceIfPresent(evidenceDir, afterFile),
                    "Execution completed; controlled healing applied only when policy allowed it.");
        } catch (Exception e) {
            try {
                if (page != null && !page.isClosed()) capture(page, evidenceDir.resolve(afterFile));
            } catch (Exception ignored) { }
            return persist(request, "FAIL", start, evidenceIfPresent(evidenceDir, afterFile), rootMessage(e));
        }
    }

    private void executeWithHealing(Page page, String testId, String step, Path evidenceDir, String beforeFile) {
        try {
            perform(page, step, false);
        } catch (Exception firstFailure) {
            String message = rootMessage(firstFailure);
            FailureCategory category = failureClassifier.classify(message);
            if (!category.isRecoverable()) {
                healingDecisions.evaluate(testId, message, "No repair: protected failure category", 0.0);
                throw firstFailure;
            }
            capture(page, evidenceDir.resolve(beforeFile));
            String repair = repairDescription(category, step);
            double confidence = healingConfidence(category);
            HealingDecisionService.HealingDecision decision = healingDecisions.evaluate(testId, message, repair, confidence);
            if (!"AUTO_HEAL_ALLOWED".equals(decision.decision())) throw firstFailure;
            perform(page, step, true);
        }
    }

    private void navigate(Page page, String url) {
        page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    }

    private ExecutionResponse persist(ExecutionRequest request, String status, long start, String evidence, String message) {
        long duration = System.currentTimeMillis() - start;
        records.save(new ExecutionRecord(request.testId(), request.url(), status, duration, evidence, message));
        return new ExecutionResponse(request.testId(), status, duration, evidence, message);
    }

    private String evidenceIfPresent(Path evidenceDir, String file) {
        Path candidate = evidenceDir.resolve(file);
        return Files.isRegularFile(candidate) ? "/api/execution/evidence/" + file : null;
    }

    private void capture(Page page, Path destination) {
        page.screenshot(new Page.ScreenshotOptions().setPath(destination.toAbsolutePath()).setFullPage(true));
        if (evidenceStore != null) evidenceStore.persist(destination);
    }

    private void perform(Page page, String raw, boolean healingRetry) {
        String step = raw == null ? "" : raw.trim();
        String lower = step.toLowerCase(Locale.ROOT);
        if (step.isBlank() || lower.startsWith("open ") || lower.equals("open the application")) return;
        List<String> quoted = quotedValues(step);
        if ((lower.startsWith("enter ") || lower.startsWith("fill ")) && quoted.size() >= 2) {
            String value = quoted.get(0), label = quoted.get(1);
            if (healingRetry) page.getByPlaceholder(label, new Page.GetByPlaceholderOptions().setExact(false)).fill(value);
            else page.getByLabel(label, new Page.GetByLabelOptions().setExact(false)).fill(value);
            return;
        }
        if (lower.startsWith("select ") && quoted.size() >= 2) { page.getByLabel(quoted.get(1), new Page.GetByLabelOptions().setExact(false)).selectOption(quoted.get(0)); return; }
        if ((lower.startsWith("check ") || lower.startsWith("tick ")) && !quoted.isEmpty()) { page.getByLabel(quoted.get(0), new Page.GetByLabelOptions().setExact(false)).check(); return; }
        if (lower.contains("enter email")) { String value=valueInQuotes(step,"test@example.com"); if(healingRetry) page.locator("input[type=email]").first().fill(value); else page.getByLabel("Email",new Page.GetByLabelOptions().setExact(false)).fill(value); return; }
        if (lower.contains("enter password")) { String value=valueInQuotes(step,"Password123"); if(healingRetry) page.locator("input[type=password]").first().fill(value); else page.getByLabel("Password",new Page.GetByLabelOptions().setExact(false)).fill(value); return; }
        if (lower.startsWith("click ")) { String label=!quoted.isEmpty()?quoted.get(0):step.substring(6).trim(); if(healingRetry) page.getByText(label,new Page.GetByTextOptions().setExact(false)).first().click(); else page.getByRole(AriaRole.BUTTON,new Page.GetByRoleOptions().setName(label).setExact(false)).click(); return; }
        if (lower.startsWith("verify ")) { verify(page, step); return; }
        throw new IllegalArgumentException("Unsupported automation step: " + step);
    }

    private String repairDescription(FailureCategory category, String step) {
        return switch (category) {
            case LOCATOR_FAILURE -> "Retry step with conservative semantic fallback locator: " + step;
            case TIMEOUT -> "Retry the same deterministic step once after Playwright failure recovery: " + step;
            case NAVIGATION_FAILURE -> "Retry deterministic browser action once; no URL mutation: " + step;
            case TRANSIENT_BROWSER_FAILURE -> "Retry deterministic browser action once: " + step;
            default -> "No repair";
        };
    }

    private double healingConfidence(FailureCategory category) {
        return switch (category) {
            case LOCATOR_FAILURE -> 0.95;
            case TIMEOUT -> 0.92;
            case NAVIGATION_FAILURE, TRANSIENT_BROWSER_FAILURE -> 0.90;
            default -> 0.0;
        };
    }

    private void verify(Page page, String expected) {
        String text = expected.replaceFirst("(?i)^verify\\s+", "").trim();
        if (text.toLowerCase(Locale.ROOT).startsWith("text ")) text = text.substring(5).trim();
        text = stripQuotes(text);
        if (!text.isBlank()) page.getByText(text, new Page.GetByTextOptions().setExact(false)).first().waitFor();
    }

    private List<String> quotedValues(String text) {
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\\"([^\\\"]*)\\\"").matcher(text);
        while (matcher.find()) values.add(matcher.group(1));
        return values;
    }

    private String stripQuotes(String text) { return text.length()>=2&&text.startsWith("\"")&&text.endsWith("\"")?text.substring(1,text.length()-1):text; }
    private String valueInQuotes(String text, String fallback) { List<String> values=quotedValues(text); return values.isEmpty()?fallback:values.get(0); }
    private String rootMessage(Exception exception) { Throwable root=exception; while(root.getCause()!=null) root=root.getCause(); return root.getMessage()==null?root.toString():root.getMessage(); }
}
