package com.aiqa.automation;

public record AutomationResponse(
        String testId,
        String framework,
        String language,
        String fileName,
        String code
) {}
