package com.aiqa.testdesign;

import java.util.List;

public record TestDesignResponse(
        String requirementTitle,
        String strategy,
        List<TestCase> testCases
) {}
