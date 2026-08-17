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

@Service
public class ExecutionService {
    public ExecutionResponse run(ExecutionRequest request) {
        long start = System.currentTimeMillis();
        Path evidenceDir = Path.of("evidence");
        String file = request.testId().replaceAll("[^A-Za-z0-9_-]", "_") + "-" + UUID.randomUUID() + ".png";
        try {
            Files.createDirectories(evidenceDir);
            try (Playwright playwright = Playwright.create()) {
                boolean headless = request.headless() == null || request.headless();
                try (Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless))) {
                    try (BrowserContext context = browser.newContext()) {
                        Page page = context.newPage();
                        page.navigate(request.url(), new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
                        for (String step : request.steps() == null ? List.<String>of() : request.steps()) perform(page, step);
                        if (request.expectedResult() != null && !request.expectedResult().isBlank()) verify(page, request.expectedResult());
                        page.screenshot(new Page.ScreenshotOptions().setPath(evidenceDir.resolve(file).toAbsolutePath()).setFullPage(true));
                    }
                }
            }
            return new ExecutionResponse(request.testId(), "PASS", System.currentTimeMillis() - start, "/api/execution/evidence/" + file, "All steps completed.");
        } catch (Exception e) {
            return new ExecutionResponse(request.testId(), "FAIL", System.currentTimeMillis() - start, "/api/execution/evidence/" + file, rootMessage(e));
        }
    }

    private void perform(Page page, String raw) {
        String step = raw.trim();
        String lower = step.toLowerCase(Locale.ROOT);
        if (lower.startsWith("open ") || lower.equals("open the application")) return;
        if (lower.contains("enter email")) { page.getByLabel("Email").fill(valueInQuotes(step, "test@example.com")); return; }
        if (lower.contains("enter password")) { page.getByLabel("Password").fill(valueInQuotes(step, "Password123")); return; }
        if (lower.contains("click login")) { page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Login")).click(); return; }
        if (lower.contains("click logout")) { page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Logout")).click(); return; }
        if (lower.contains("click ")) {
            String label = step.substring(lower.indexOf("click ") + 6).trim();
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(label).setExact(false)).click();
            return;
        }
        throw new IllegalArgumentException("Unsupported automation step: " + step);
    }

    private void verify(Page page, String expected) {
        String text = expected.replaceFirst("(?i)^verify\\s+", "").trim();
        if (!text.isBlank()) page.getByText(text, new Page.GetByTextOptions().setExact(false)).first().waitFor();
    }

    private String valueInQuotes(String text, String fallback) {
        int a = text.indexOf('"'), b = text.lastIndexOf('"');
        return a >= 0 && b > a ? text.substring(a + 1, b) : fallback;
    }

    private String rootMessage(Exception e) {
        Throwable t = e;
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage() == null ? t.toString() : t.getMessage();
    }
}
