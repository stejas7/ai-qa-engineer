package com.aiqa.execution;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitUntilState;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Deterministic browser execution engine used by Auravis M4.
 * AI may design a test, while this service controls the allowed browser actions,
 * assertions, evidence capture and execution audit trail.
 */
@Service
public class ExecutionService {
    private final ExecutionRecordRepository records;

    public ExecutionService(ExecutionRecordRepository records) {
        this.records = records;
    }

    public ExecutionResponse run(ExecutionRequest request) {
        long start = System.currentTimeMillis();
        Path evidenceDir = Path.of("evidence");
        String file = request.testId().replaceAll("[^A-Za-z0-9_-]", "_")
                + "-" + UUID.randomUUID() + ".png";
        Page page = null;
        try {
            Files.createDirectories(evidenceDir);
            try (Playwright playwright = Playwright.create()) {
                boolean headless = request.headless() == null || request.headless();
                try (Browser browser = playwright.chromium()
                        .launch(new BrowserType.LaunchOptions().setHeadless(headless))) {
                    try (BrowserContext context = browser.newContext()) {
                        page = context.newPage();
                        page.navigate(request.url(), new Page.NavigateOptions()
                                .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
                        for (String step : request.steps() == null ? List.<String>of() : request.steps()) {
                            perform(page, step);
                        }
                        if (request.expectedResult() != null && !request.expectedResult().isBlank()) {
                            verify(page, request.expectedResult());
                        }
                        capture(page, evidenceDir.resolve(file));
                    }
                }
            }
            return persist(request, "PASS", start, file, "All supported steps and assertions completed.");
        } catch (Exception e) {
            try {
                if (page != null && !page.isClosed()) capture(page, evidenceDir.resolve(file));
            } catch (Exception ignored) { }
            return persist(request, "FAIL", start, file, rootMessage(e));
        }
    }

    private ExecutionResponse persist(ExecutionRequest request, String status, long start,
                                      String file, String message) {
        long duration = System.currentTimeMillis() - start;
        String evidence = "/api/execution/evidence/" + file;
        records.save(new ExecutionRecord(request.testId(), request.url(), status, duration, evidence, message));
        return new ExecutionResponse(request.testId(), status, duration, evidence, message);
    }

    private void capture(Page page, Path destination) {
        page.screenshot(new Page.ScreenshotOptions().setPath(destination.toAbsolutePath()).setFullPage(true));
    }

    private void perform(Page page, String raw) {
        String step = raw == null ? "" : raw.trim();
        String lower = step.toLowerCase(Locale.ROOT);
        if (step.isBlank() || lower.startsWith("open ") || lower.equals("open the application")) return;

        List<String> quoted = quotedValues(step);
        if ((lower.startsWith("enter ") || lower.startsWith("fill ")) && quoted.size() >= 2) {
            page.getByLabel(quoted.get(1), new Page.GetByLabelOptions().setExact(false)).fill(quoted.get(0));
            return;
        }
        if (lower.startsWith("select ") && quoted.size() >= 2) {
            page.getByLabel(quoted.get(1), new Page.GetByLabelOptions().setExact(false)).selectOption(quoted.get(0));
            return;
        }
        if ((lower.startsWith("check ") || lower.startsWith("tick ")) && !quoted.isEmpty()) {
            page.getByLabel(quoted.get(0), new Page.GetByLabelOptions().setExact(false)).check();
            return;
        }
        if (lower.contains("enter email")) {
            page.getByLabel("Email", new Page.GetByLabelOptions().setExact(false)).fill(valueInQuotes(step, "test@example.com"));
            return;
        }
        if (lower.contains("enter password")) {
            page.getByLabel("Password", new Page.GetByLabelOptions().setExact(false)).fill(valueInQuotes(step, "Password123"));
            return;
        }
        if (lower.startsWith("click ")) {
            String label = !quoted.isEmpty() ? quoted.get(0) : step.substring(6).trim();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(label).setExact(false)).click();
            return;
        }
        if (lower.startsWith("verify ")) {
            verify(page, step);
            return;
        }
        throw new IllegalArgumentException("Unsupported automation step: " + step);
    }

    private void verify(Page page, String expected) {
        String text = expected.replaceFirst("(?i)^verify\\s+", "").trim();
        if (text.toLowerCase(Locale.ROOT).startsWith("text ")) text = text.substring(5).trim();
        text = stripQuotes(text);
        if (!text.isBlank()) {
            page.getByText(text, new Page.GetByTextOptions().setExact(false)).first().waitFor();
        }
    }

    private List<String> quotedValues(String text) {
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\\"([^\\\"]*)\\\"").matcher(text);
        while (matcher.find()) values.add(matcher.group(1));
        return values;
    }

    private String stripQuotes(String text) {
        if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) return text.substring(1, text.length() - 1);
        return text;
    }

    private String valueInQuotes(String text, String fallback) {
        List<String> values = quotedValues(text);
        return values.isEmpty() ? fallback : values.get(0);
    }

    private String rootMessage(Exception exception) {
        Throwable root = exception;
        while (root.getCause() != null) root = root.getCause();
        return root.getMessage() == null ? root.toString() : root.getMessage();
    }
}
