package com.aiqa.auravis;

import com.aiqa.automation.AutomationRequest;
import com.aiqa.automation.PlaywrightAutomationService;
import com.aiqa.execution.ExecutionRequest;
import com.aiqa.execution.ExecutionResponse;
import com.aiqa.execution.ExecutionService;
import com.aiqa.failure.FailureAnalysisRequest;
import com.aiqa.failure.FailureAnalysisResponse;
import com.aiqa.failure.FailureAnalysisService;
import com.aiqa.knowledge.KnowledgeService;
import com.aiqa.requirement.AiRequirementService;
import com.aiqa.requirement.Requirement;
import com.aiqa.requirement.RequirementAnalysis;
import com.aiqa.testdesign.TestCase;
import com.aiqa.testdesign.TestDesignResponse;
import com.aiqa.testdesign.TestDesignService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Coordinates Auravis autonomous QA pipeline without manual stage transitions. */
@Service
public class AuravisMissionOrchestrator {
    private final AuravisMissionRepository missions;
    private final KnowledgeService knowledgeService;
    private final AiRequirementService requirementService;
    private final TestDesignService testDesignService;
    private final PlaywrightAutomationService automationService;
    private final ExecutionService executionService;
    private final FailureAnalysisService failureAnalysisService;

    public AuravisMissionOrchestrator(
            AuravisMissionRepository missions,
            KnowledgeService knowledgeService,
            AiRequirementService requirementService,
            TestDesignService testDesignService,
            PlaywrightAutomationService automationService,
            ExecutionService executionService,
            FailureAnalysisService failureAnalysisService) {
        this.missions = missions;
        this.knowledgeService = knowledgeService;
        this.requirementService = requirementService;
        this.testDesignService = testDesignService;
        this.automationService = automationService;
        this.executionService = executionService;
        this.failureAnalysisService = failureAnalysisService;
    }

    @Transactional
    public AuravisMission run(UUID missionId) {
        AuravisMission mission = missions.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Auravis mission not found"));
        mission.start();
        missions.save(mission);

        try {
            String knowledgeContext = knowledgeService.buildContext(mission.getRequirement(), 5);
            Requirement requirement = new Requirement();
            requirement.setTitle(mission.getTitle());
            String groundedDescription = knowledgeContext.isBlank()
                    ? mission.getRequirement()
                    : mission.getRequirement() + "\n\n" + knowledgeContext;
            requirement.setDescription(groundedDescription);
            requirement.setAcceptanceCriteria(List.of(mission.getRequirement()));

            RequirementAnalysis analysis = requirementService.analyze(requirement);
            mission.requirementReady(analysis.summary());

            TestDesignResponse design = testDesignService.design(requirement, analysis);
            List<TestCase> testCases = design.testCases();
            mission.scenarioCount(testCases.size());
            missions.save(mission);

            for (TestCase test : testCases) {
                automationService.generate(new AutomationRequest(
                        test.id(), test.title(), mission.getUatUrl(), test.steps(), test.expectedResult()));

                ExecutionResponse execution = executionService.run(new ExecutionRequest(
                        test.id(), mission.getUatUrl(), test.steps(), test.expectedResult(), true));

                if ("PASS".equals(execution.status())) {
                    mission.testPassed();
                } else {
                    mission.testFailed();
                    FailureAnalysisResponse failure = failureAnalysisService.analyze(
                            new FailureAnalysisRequest(test.id(), execution.message(), test.expectedResult(),
                                    mission.getUatUrl(), execution.screenshot()));
                    if (failure.retryRecommended()) {
                        mission.complete("QA requires retry: " + failure.probableCause());
                        return missions.save(mission);
                    }
                }
                missions.save(mission);
            }

            mission.complete(mission.getTestsFailed() == 0
                    ? "UAT QA PASSED: all generated scenarios completed successfully."
                    : "UAT QA FAILED: one or more scenarios failed and were automatically analyzed.");
            return missions.save(mission);
        } catch (Exception e) {
            mission.fail("Autonomous QA mission stopped: " + rootMessage(e));
            return missions.save(mission);
        }
    }

    private String rootMessage(Exception e) {
        Throwable root = e;
        while (root.getCause() != null) root = root.getCause();
        return root.getMessage() == null ? root.toString() : root.getMessage();
    }
}
