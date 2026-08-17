package com.aiqa.automation;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record AutomationRequest(
        @NotBlank String testId,
        @NotBlank String title,
        @NotBlank String url,
        List<String> steps,
        String expectedResult
) {}
