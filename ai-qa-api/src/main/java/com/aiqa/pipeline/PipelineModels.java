package com.aiqa.pipeline;

import com.aiqa.execution.ExecutionResponse;
import com.aiqa.failure.FailureAnalysisResponse;
import com.aiqa.quality.QualityGateResponse;

import java.util.List;
import java.util.UUID;

/**
 * Structured, dashboard-friendly result of one full requirement-to-quality-gate pipeline run.
 */
public class PipelineModels {

    public record TestCaseResult(
            String id,
            String title,
            String type,
            String priority,
            List<String> steps,
            String expectedResult,
            String automationCandidate,
            String automationFileName,
            String automationCode,
            ExecutionResponse execution,
            FailureAnalysisResponse failureAnalysis
    ) {}

    public record RequirementResult(
            String title,
            String description,
            List<String> acceptanceCriteria,
            String analysisSummary,
            List<String> businessRules,
            List<String> openQuestions,
            List<TestCaseResult> testCases
    ) {}

    public record PipelineResult(
            UUID runId,
            UUID agentRunId,
            String company,
            String fileName,
            String targetUrl,
            List<RequirementResult> requirements,
            int totalTests,
            int passedTests,
            int failedTests,
            int automatedTests,
            QualityGateResponse qualityGate
    ) {}
}
