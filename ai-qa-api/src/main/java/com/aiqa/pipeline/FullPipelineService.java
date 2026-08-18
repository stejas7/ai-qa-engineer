package com.aiqa.pipeline;

import com.aiqa.agent.AgentOrchestrator;
import com.aiqa.agent.AgentRun;
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
import com.aiqa.requirement.RequirementRepository;
import com.aiqa.testdesign.TestCase;
import com.aiqa.testdesign.TestDesignResponse;
import com.aiqa.testdesign.TestDesignService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Runs the complete autonomous requirement-to-quality-gate pipeline. */
@Service
public class FullPipelineService {

    private static final Logger log = LoggerFactory.getLogger(FullPipelineService.class);

    private final RequirementSplitter splitter;
    private final RequirementRepository requirementRepository;
    private final AiRequirementService aiRequirementService;
    private final TestDesignService testDesignService;
    private final PlaywrightAutomationService automationService;
    private final ExecutionService executionService;
    private final FailureAnalysisService failureAnalysisService;
    private final QualityGateService qualityGateService;
    private final AgentOrchestrator agentOrchestrator;
    private final PipelineRunRepository pipelineRunRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public FullPipelineService(RequirementSplitter splitter,
                                RequirementRepository requirementRepository,
                                AiRequirementService aiRequirementService,
                                TestDesignService testDesignService,
                                PlaywrightAutomationService automationService,
                                ExecutionService executionService,
                                FailureAnalysisService failureAnalysisService,
                                QualityGateService qualityGateService,
                                AgentOrchestrator agentOrchestrator,
                                PipelineRunRepository pipelineRunRepository) {
        this.splitter = splitter;
        this.requirementRepository = requirementRepository;
        this.aiRequirementService = aiRequirementService;
        this.testDesignService = testDesignService;
        this.automationService = automationService;
        this.executionService = executionService;
        this.failureAnalysisService = failureAnalysisService;
        this.qualityGateService = qualityGateService;
        this.agentOrchestrator = agentOrchestrator;
        this.pipelineRunRepository = pipelineRunRepository;
    }

    @Async
    public void runInBackground(UUID pipelineRunId, String rawText, String fallbackTitle,
                                 String targetUrl, boolean executeAutomation) {
        PipelineRun pipelineRun = pipelineRunRepository.findById(pipelineRunId)
                .orElseThrow(() -> new IllegalArgumentException("Pipeline run not found"));

        AgentRun agentRun = agentOrchestrator.start("FULL_QA_PIPELINE", fallbackTitle);
        try {
            pipelineRun.markRunning("Reading requirement document");
            pipelineRunRepository.save(pipelineRun);

            List<RequirementSplitter.RequirementBlock> blocks = splitter.split(rawText, fallbackTitle);
            if (blocks.isEmpty()) {
                throw new IllegalArgumentException("No requirement content found in the uploaded file");
            }

            List<PipelineModels.RequirementResult> requirementResults = new ArrayList<>();
            int totalTests = 0, passedTests = 0, failedTests = 0, automatedTests = 0;

            for (RequirementSplitter.RequirementBlock block : blocks) {
                pipelineRun.updateStage("Analyzing requirement: " + block.title());
                pipelineRunRepository.save(pipelineRun);

                Requirement requirement = new Requirement();
                requirement.setTitle(block.title());
                requirement.setDescription(block.description());
                requirement.setAcceptanceCriteria(block.acceptanceCriteria());
                requirement = requirementRepository.save(requirement);

                var analysisStep = agentOrchestrator.addStep(agentRun.getId(), "REQUIREMENT_ANALYSIS", block.title());
                RequirementAnalysis analysis = aiRequirementService.analyze(requirement);
                agentOrchestrator.completeStep(analysisStep.getId(),
                        "Generated " + analysis.testScenarios().size() + " scenarios for " + block.title());

                pipelineRun.updateStage("Designing test cases: " + block.title());
                pipelineRunRepository.save(pipelineRun);
                var designStep = agentOrchestrator.addStep(agentRun.getId(), "TEST_DESIGN", block.title());
                TestDesignResponse design = testDesignService.design(requirement, analysis);
                agentOrchestrator.completeStep(designStep.getId(),
                        "Designed " + design.testCases().size() + " test cases for " + block.title());

                List<PipelineModels.TestCaseResult> testCaseResults = new ArrayList<>();
                for (TestCase testCase : design.testCases()) {
                    totalTests++;
                    boolean automate = "YES".equalsIgnoreCase(testCase.automationCandidate());
                    String automationFileName = null;
                    String automationCode = null;
                    ExecutionResponse execution = null;
                    FailureAnalysisResponse failureAnalysis = null;

                    if (automate) {
                        pipelineRun.updateStage("Generating automation: " + testCase.id());
                        pipelineRunRepository.save(pipelineRun);
                        var automationStep = agentOrchestrator.addStep(agentRun.getId(), "AUTOMATION_GENERATION", testCase.id());
                        AutomationResponse automation = automationService.generate(new AutomationRequest(
                                testCase.id(), testCase.title(), targetUrl, testCase.steps(), testCase.expectedResult()));
                        automationFileName = automation.fileName();
                        automationCode = automation.code();
                        automatedTests++;
                        agentOrchestrator.completeStep(automationStep.getId(), "Generated " + automation.fileName());

                        if (executeAutomation) {
                            pipelineRun.updateStage("Executing: " + testCase.id());
                            pipelineRunRepository.save(pipelineRun);
                            var executionStep = agentOrchestrator.addStep(agentRun.getId(), "UAT_EXECUTION", testCase.id());
                            execution = executionService.run(new ExecutionRequest(
                                    testCase.id(), targetUrl, testCase.steps(), testCase.expectedResult(), true));
                            agentOrchestrator.completeStep(executionStep.getId(), execution.status() + " - " + execution.message());

                            if ("PASS".equalsIgnoreCase(execution.status())) {
                                passedTests++;
                            } else {
                                failedTests++;
                                var failureStep = agentOrchestrator.addStep(agentRun.getId(), "FAILURE_ANALYSIS", testCase.id());
                                failureAnalysis = failureAnalysisService.analyze(new FailureAnalysisRequest(
                                        testCase.id(), execution.message(), testCase.expectedResult(), targetUrl, execution.screenshot()));
                                agentOrchestrator.completeStep(failureStep.getId(), failureAnalysis.classification());
                            }
                        }
                    }

                    testCaseResults.add(new PipelineModels.TestCaseResult(
                            testCase.id(), testCase.title(), testCase.type(), testCase.priority(),
                            testCase.preconditions(), testCase.steps(), testCase.testData(), testCase.expectedResult(),
                            testCase.automationCandidate(), automationFileName, automationCode, execution, failureAnalysis));
                }

                requirementResults.add(new PipelineModels.RequirementResult(
                        block.title(), block.description(), block.acceptanceCriteria(),
                        analysis.summary(), analysis.businessRules(), analysis.questions(), testCaseResults));
            }

            pipelineRun.updateStage("Evaluating quality gate");
            pipelineRunRepository.save(pipelineRun);

            int requirementsCovered = (int) requirementResults.stream().filter(r -> !r.testCases().isEmpty()).count();
            QualityGateResponse gate = qualityGateService.evaluate(new QualityGateRequest(
                    totalTests, executeAutomation ? passedTests : 0, executeAutomation ? failedTests : 0,
                    automatedTests, requirementResults.size(), requirementsCovered));

            PipelineModels.PipelineResult result = new PipelineModels.PipelineResult(
                    pipelineRunId, agentRun.getId(), pipelineRun.getCompany(), pipelineRun.getFileName(),
                    targetUrl, requirementResults, totalTests, passedTests, failedTests, automatedTests, gate);

            agentOrchestrator.complete(agentRun.getId(),
                    "Pipeline completed: " + requirementResults.size() + " requirements, " + totalTests + " tests, gate=" + gate.decision());
            pipelineRun.complete(mapper.writeValueAsString(result));
            pipelineRunRepository.save(pipelineRun);
        } catch (Exception e) {
            log.error("Pipeline run {} failed", pipelineRunId, e);
            try {
                agentRun.fail(e.getMessage());
                agentOrchestrator.complete(agentRun.getId(), "Pipeline failed: " + e.getMessage());
            } catch (Exception ignored) {
                // best-effort bookkeeping
            }
            pipelineRun.fail(e.getMessage() == null ? e.toString() : e.getMessage());
            pipelineRunRepository.save(pipelineRun);
        }
    }
}
