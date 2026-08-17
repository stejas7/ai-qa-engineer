package com.aiqa.execution;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitUntilState;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Executes a small, deterministic UAT vocabulary using Playwright for Java.
 *
 * <p>The service is intentionally deterministic: an AI agent may generate the test steps, but this
 * service is responsible only for executing supported browser actions and returning evidence.
 * Unsupported actions fail explicitly instead of being guessed.</p>
 */
@Service
public class ExecutionService {

    /**
     * Executes a UAT request and captures a full-page screenshot as evidence.
     *
     * @param request target URL, test steps, expected result and headless preference
     * @return execution status, duration, evidence location and diagnostic message
     */
    public ExecutionResponse run(ExecutionRequest request) {
        long start = System.currentTimeMillis();
        Path evidenceDir = Path.of("evidence");
        String file = request.testId().replaceAll("[^A-Za-z0-9_-]", "_")
                + "-" + UUID.randomUUID() + ".png";
        try {
            Files.createDirectories(evidenceDir);
            try (Playwright playwright = Playwright.create()) {
                boolean headless = request.headless() == null || request.headless();
                try (Browser browser = playwright.chromium()
                        .launch(new BrowserType.LaunchOptions().setHeadless(headless))) {
                    try (BrowserContext context = browser.newContext()) {
                        Page page = context.newPage();
                        page.navigate(
                                request.url(),
                                new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
                        for (String step : request.steps() == null ? List.<String>of() : request.steps()) {
                            perform(page, step);
                        }
                        if (request.expectedResult() != null && !request.expectedResult().isBlank()) {
                            verify(page, request.expectedResult());
                        }
                        page.screenshot(new Page.ScreenshotOptions()
                                .setPath(evidenceDir.resolve(file).toAbsolutePath())
                                .setFullPage(true));
                    }
                }
            }
            return new ExecutionResponse(
                    request.testId(),
                    "PASS",
                    System.currentTimeMillis() - start,
                    "/api/execution/evidence/" + file,
                    "All steps completed.");
        } catch (Exception e) {
            return new ExecutionResponse(
                    request.testId(),
                    "FAIL",
                    System.currentTimeMillis() - start,
                    "/api/execution/evidence/" + file,
                    rootMessage(e));
        }
    }

    private void perform(Page page, String raw) {
        String step = raw.trim();
        String lower = step.toLowerCase(Locale.ROOT);
        if (lower.startsWith("open ") || lower.equals("open the application")) {
            return;
        }
        if (lower.contains("enter email")) {
            page.getByLabel("Email").fill(valueInQuotes(step, "test@example.com"));
            return;
        }
        if (lower.contains("enter password")) {
            page.getByLabel("Password").fill(valueInQuotes(step, "Password123"));
            return;
        }
        if (lower.contains("click login")) {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login")).click();
            return;
        }
        if (lower.contains("click logout")) {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Logout")).click();
            return;
        }
        if (lower.contains("click ")) {
            String label = step.substring(lower.indexOf("click ") + 6).trim();
            page.getByRole(
                    AriaRole.BUTTON,
                    new Page.GetByRoleOptions().setName(label).setExact(false)).click();
            return;
        }
        throw new IllegalArgumentException("Unsupported automation step: " + step);
    }

    private void verify(Page page, String expected) {
        String text = expected.replaceFirst("(?i)^verify\\s+", "").trim();
        if (!text.isBlank()) {
            page.getByText(text, new Page.GetByTextOptions().setExact(false)).first().waitFor();
        }
    }

    private String valueInQuotes(String text, String fallback) {
        int start = text.indexOf('"');
        int end = text.lastIndexOf('"');
        return start >= 0 && end > start ? text.substring(start + 1, end) : fallback;
    }

    private String rootMessage(Exception exception) {
        Throwable root = exception;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getMessage() == null ? root.toString() : root.getMessage();
    }
}
