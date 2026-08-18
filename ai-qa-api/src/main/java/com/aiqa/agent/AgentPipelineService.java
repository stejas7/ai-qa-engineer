package com.aiqa.agent;

import com.aiqa.automation.AutomationRequest;
import com.aiqa.automation.AutomationResponse;
import com.aiqa.automation.PlaywrightAutomationService;
import com.aiqa.execution.ExecutionRequest;
import com.aiqa.execution.ExecutionResponse;
import com.aiqa.execution.ExecutionService;
import com.aiqa.failure.FailureAnalysisRequest;
import com.aiqa.failure.FailureAnalysisResponse;
import com.aiqa.failure.FailureAnalysisService;
import com.aiqa.quality.QualityGateRequest;
import com.aiqa.quality.QualityGateResponse;
import com.aiqa.quality.QualityGateService;
import com.aiqa.requirement.AiRequirementService;
import com.aiqa.requirement.Requirement;
import com.aiqa.requirement.RequirementAnalysis;
import com.aiqa.testdesign.TestCase;
import com.aiqa.testdesign.TestDesignResponse;
import com.aiqa.testdesign.TestDesignService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * M5 end-to-end orchestration pipeline.
 *
 * <p>The orchestrator records every logical agent step while deterministic services
 * perform requirement analysis, test design, automation generation, browser execution,
 * failure diagnosis and the final quality decision. M6 self-healing is invoked inside
 * {@link ExecutionService} and therefore remains governed by the execution layer.</p>
 *
 * @author Tejas Shah
 */
@Service
public class AgentPipelineService {
    private final AgentOrchestrator orchestrator;
    private final AiRequirementService requirementAgent;
    private final TestDesignService testDesignAgent;
    private final PlaywrightAutomationService automationAgent;
    private final ExecutionService executionAgent;
    private final FailureAnalysisService diagnosisAgent;
    private final QualityGateService qualityAgent;

    public AgentPipelineService(AgentOrchestrator orchestrator,
                                AiRequirementService requirementAgent,
                                TestDesignService testDesignAgent,
                                PlaywrightAutomationService automationAgent,
                                ExecutionService executionAgent,
                                FailureAnalysisService diagnosisAgent,
                                QualityGateService qualityAgent) {
        this.orchestrator = orchestrator;
        this.requirementAgent = requirementAgent;
        this.testDesignAgent = testDesignAgent;
        this.automationAgent = automationAgent;
        this.executionAgent = executionAgent;
        this.diagnosisAgent = diagnosisAgent;
        this.qualityAgent = qualityAgent;
    }

    public PipelineResult run(PipelineRequest request) {
        AgentRun run = orchestrator.start("QA_ENGINEERING_PIPELINE", request.title());
        AgentStep activeStep = null;
        try {
            Requirement requirement = new Requirement();
            requirement.setTitle(request.title());
            requirement.setDescription(request.description());
            requirement.setAcceptanceCriteria(request.acceptanceCriteria());

            activeStep = orchestrator.addStep(run.getId(), "REQUIREMENT_ANALYSIS", request.description());
            RequirementAnalysis analysis = requirementAgent.analyze(requirement);
            orchestrator.completeStep(activeStep.getId(), "Generated " + analysis.testScenarios().size() + " candidate scenarios");

            activeStep = orchestrator.addStep(run.getId(), "TEST_DESIGN", "Convert requirement intelligence into executable tests");
            TestDesignResponse design = testDesignAgent.design(requirement, analysis);
            orchestrator.completeStep(activeStep.getId(), "Designed " + design.testCases().size() + " traceable test cases");

            List<String> generated = new ArrayList<>();
            List<TestExecutionResult> executions = new ArrayList<>();
            int passed = 0;
            int failed = 0;

            for (TestCase test : design.testCases()) {
                activeStep = orchestrator.addStep(run.getId(), "AUTOMATION_GENERATION", test.id() + " - " + test.title());
                AutomationResponse automation = automationAgent.generate(new AutomationRequest(
                        test.id(), test.title(), request.url(), test.steps(), test.expectedResult()));
                generated.add(automation.fileName());
                orchestrator.completeStep(activeStep.getId(), "Generated " + automation.fileName());

                activeStep = orchestrator.addStep(run.getId(), "UAT_EXECUTION", test.id() + " against " + request.url());
                ExecutionResponse execution = executionAgent.run(new ExecutionRequest(
                        test.id(), request.url(), test.steps(), test.expectedResult(), true));

                FailureAnalysisResponse diagnosis = null;
                if ("PASS".equalsIgnoreCase(execution.status())) {
                    passed++;
                    orchestrator.completeStep(activeStep.getId(), "PASS • " + execution.durationMs() + "ms • " + execution.screenshot());
                } else {
                    failed++;
                    orchestrator.failStep(activeStep.getId(), "FAIL • " + execution.message() + " • " + execution.screenshot());

                    AgentStep diagnosisStep = orchestrator.addStep(run.getId(), "FAILURE_DIAGNOSIS", test.id() + " • " + execution.message());
                    diagnosis = diagnosisAgent.analyze(new FailureAnalysisRequest(
                            test.id(), execution.message(), test.expectedResult(), request.url(), execution.screenshot()));
                    orchestrator.completeStep(diagnosisStep.getId(), diagnosis.classification() + " • " + diagnosis.severity() + " • " + diagnosis.recommendation());
                }

                executions.add(new TestExecutionResult(test.id(), execution.status(), execution.durationMs(),
                        execution.screenshot(), execution.message(), diagnosis));
            }

            activeStep = orchestrator.addStep(run.getId(), "QUALITY_DECISION", "Evaluate execution facts and requirement coverage");
            int total = design.testCases().size();
            int requirements = Math.max(1, analysis.testScenarios().size());
            int coveredRequirements = total == 0 ? 0 : requirements;
            QualityGateResponse gate = qualityAgent.evaluate(new QualityGateRequest(
                    total, passed, failed, generated.size(), requirements, coveredRequirements));
            orchestrator.completeStep(activeStep.getId(), gate.decision() + " • " + gate.reason());

            String summary = "M5 orchestration completed: " + passed + " passed, " + failed + " failed, QA decision " + gate.decision();
            AgentRun completed = orchestrator.complete(run.getId(), summary);
            return new PipelineResult(completed.getId(), completed.getStatus(), analysis.testScenarios().size(),
                    total, passed, failed, generated, executions, gate);
        } catch (Exception e) {
            String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            if (activeStep != null && !"FAILED".equalsIgnoreCase(activeStep.getStatus()) && !"COMPLETED".equalsIgnoreCase(activeStep.getStatus())) {
                try { orchestrator.failStep(activeStep.getId(), message); } catch (Exception ignored) { }
            }
            orchestrator.fail(run.getId(), "Pipeline failed: " + message);
            throw e;
        }
    }

    public record PipelineRequest(String title, String description, List<String> acceptanceCriteria, String url) {}

    public record TestExecutionResult(String testId,
                                      String status,
                                      long durationMs,
                                      String screenshot,
                                      String message,
                                      FailureAnalysisResponse diagnosis) {}

    public record PipelineResult(UUID runId,
                                 String status,
                                 int scenarios,
                                 int testCases,
                                 int passed,
                                 int failed,
                                 List<String> automationArtifacts,
                                 List<TestExecutionResult> executions,
                                 QualityGateResponse qualityGate) {}
}
