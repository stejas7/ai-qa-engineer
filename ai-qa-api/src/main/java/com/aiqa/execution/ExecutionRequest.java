package com.aiqa.execution;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ExecutionRequest(
    @NotBlank String testId,
    @NotBlank String url,
    List<String> steps,
    String expectedResult,
    Boolean headless
) {}
