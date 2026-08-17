package com.aiqa.testdesign;

import java.util.List;

public record TestCase(
        String id,
        String title,
        String type,
        String priority,
        String preconditions,
        List<String> steps,
        String testData,
        String expectedResult,
        String automationCandidate
) {}
