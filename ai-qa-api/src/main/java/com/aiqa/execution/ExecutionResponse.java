package com.aiqa.execution;

public record ExecutionResponse(
    String testId,
    String status,
    long durationMs,
    String screenshot,
    String message
) {}
