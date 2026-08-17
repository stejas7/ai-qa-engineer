package com.aiqa.automation;

import org.springframework.stereotype.Service;

@Service
public class PlaywrightAutomationService {

    public AutomationResponse generate(AutomationRequest request) {
        String className = safeClassName(request.testId() + "_" + request.title());
        StringBuilder code = new StringBuilder();
        code.append("package com.aiqa.generated;\n\n")
            .append("import com.microsoft.playwright.*;\n")
            .append("import org.junit.jupiter.api.*;\n\n")
            .append("class ").append(className).append(" {\n")
            .append("    static Playwright playwright;\n")
            .append("    static Browser browser;\n")
            .append("    BrowserContext context;\n")
            .append("    Page page;\n\n")
            .append("    @BeforeAll static void start() {\n")
            .append("        playwright = Playwright.create();\n")
            .append("        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));\n")
            .append("    }\n\n")
            .append("    @BeforeEach void open() {\n")
            .append("        context = browser.newContext();\n")
            .append("        page = context.newPage();\n")
            .append("        page.navigate(\"").append(escape(request.url())).append("\");\n")
            .append("    }\n\n")
            .append("    @Test void ").append(methodName(request.testId())).append("() {\n");

        if (request.steps() != null) {
            int i = 1;
            for (String step : request.steps()) {
                code.append("        // Step ").append(i++).append(": ").append(step).append("\n");
                code.append("        // TODO: AI locator/action mapping for this step\n");
            }
        }

        code.append("        // Expected: ").append(request.expectedResult() == null ? "Verify expected result" : request.expectedResult()).append("\n")
            .append("        Assertions.assertTrue(page.url() != null);\n")
            .append("    }\n\n")
            .append("    @AfterEach void closeContext() { context.close(); }\n")
            .append("    @AfterAll static void stop() { browser.close(); playwright.close(); }\n")
            .append("}\n");

        return new AutomationResponse(request.testId(), "Playwright", "Java", className + ".java", code.toString());
    }

    private String safeClassName(String value) {
        String s = value.replaceAll("[^A-Za-z0-9_]", "_");
        if (s.isEmpty() || Character.isDigit(s.charAt(0))) s = "GeneratedTest_" + s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String methodName(String value) {
        String s = value.replaceAll("[^A-Za-z0-9_]", "_");
        return s.isEmpty() ? "generatedTest" : Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
