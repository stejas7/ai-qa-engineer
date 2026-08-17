package com.aiqa.failure;

import jakarta.validation.constraints.NotBlank;

public record FailureAnalysisRequest(
    @NotBlank String testId,
    @NotBlank String errorMessage,
    String expectedResult,
    String url,
    String screenshot
) {}
