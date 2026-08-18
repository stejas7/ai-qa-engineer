package com.aiqa.scorpion;

import com.aiqa.automation.AutomationRequest;
import com.aiqa.automation.PlaywrightAutomationService;
import com.aiqa.execution.ExecutionRequest;
import com.aiqa.execution.ExecutionResponse;
import com.aiqa.execution.ExecutionService;
import com.aiqa.failure.FailureAnalysisRequest;
import com.aiqa.failure.FailureAnalysisResponse;
import com.aiqa.failure.FailureAnalysisService;
import com.aiqa.requirement.AiRequirementService;
import com.aiqa.requirement.Requirement;
import com.aiqa.requirement.RequirementAnalysis;
import com.aiqa.requirement.TestScenario;
import com.aiqa.testdesign.TestCase;
import com.aiqa.testdesign.TestDesignResponse;
import com.aiqa.testdesign.TestDesignService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Coordinates Scorpion's autonomous V1-V5 QA pipeline without manual stage transitions. */
@Service
public class ScorpionMissionOrchestrator {
    private final ScorpionMissionRepository missions;
    private final AiRequirementService requirementService;
    private final TestDesignService testDesignService;
    private final PlaywrightAutomationService automationService;
    private final ExecutionService executionService;
    private final FailureAnalysisService failureAnalysisService;

    public ScorpionMissionOrchestrator(
            ScorpionMissionRepository missions,
            AiRequirementService requirementService,
            TestDesignService testDesignService,
            PlaywrightAutomationService automationService,
            ExecutionService executionService,
            FailureAnalysisService failureAnalysisService) {
        this.missions = missions;
        this.requirementService = requirementService;
        this.testDesignService = testDesignService;
        this.automationService = automationService;
        this.executionService = executionService;
        this.failureAnalysisService = failureAnalysisService;
    }

    /** Runs requirement analysis, test design, automation generation, execution and failure analysis. */
    @Transactional
    public ScorpionMission run(UUID missionId) {
        ScorpionMission mission = missions.findById(missionId)
                .orElseThrow(() -> new IllegalArgumentException("Scorpion mission not found"));
        mission.start();
        missions.save(mission);

        try {
            Requirement requirement = new Requirement();
            requirement.setTitle(mission.getTitle());
            requirement.setDescription(mission.getRequirement());
            requirement.setAcceptanceCriteria(List.of(mission.getRequirement()));

            // V1: understand the complete business requirement.
            RequirementAnalysis analysis = requirementService.analyze(requirement);
            mission.requirementReady(analysis.summary());

            // V2: convert the requirement into executable test cases.
            TestDesignResponse design = testDesignService.design(requirement, analysis);
            List<TestCase> testCases = design.testCases();
            mission.scenarioCount(testCases.size());
            missions.save(mission);

            // V3-V5: generate automation, execute it, then diagnose failures automatically.
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
                    // Keep the concise diagnosis in the mission log through the final decision summary.
                    if (failure.retryRecommended()) {
                        mission.complete("QA requires retry: " + failure.probableCause());
                        return missions.save(mission);
                    }
                }
                missions.save(mission);
            }

            String decision = mission.getTestsFailed() == 0
                    ? "UAT QA PASSED: all generated scenarios completed successfully."
                    : "UAT QA FAILED: one or more scenarios failed and were automatically analyzed.";
            mission.complete(decision);
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
