package com.aiqa.agent;

import com.aiqa.automation.AutomationRequest;
import com.aiqa.automation.AutomationResponse;
import com.aiqa.automation.PlaywrightAutomationService;
import com.aiqa.requirement.AiRequirementService;
import com.aiqa.requirement.Requirement;
import com.aiqa.requirement.RequirementAnalysis;
import com.aiqa.testdesign.TestCase;
import com.aiqa.testdesign.TestDesignResponse;
import com.aiqa.testdesign.TestDesignService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class AgentPipelineService {
    private final AgentOrchestrator orchestrator;
    private final AiRequirementService requirementAgent;
    private final TestDesignService testDesignAgent;
    private final PlaywrightAutomationService automationAgent;

    public AgentPipelineService(AgentOrchestrator orchestrator,
                                AiRequirementService requirementAgent,
                                TestDesignService testDesignAgent,
                                PlaywrightAutomationService automationAgent) {
        this.orchestrator = orchestrator;
        this.requirementAgent = requirementAgent;
        this.testDesignAgent = testDesignAgent;
        this.automationAgent = automationAgent;
    }

    @Transactional
    public PipelineResult run(PipelineRequest request) {
        AgentRun run = orchestrator.start("QA_ENGINEERING_PIPELINE", request.title());
        try {
            Requirement requirement = new Requirement();
            requirement.setTitle(request.title());
            requirement.setDescription(request.description());
            requirement.setAcceptanceCriteria(request.acceptanceCriteria());

            AgentStep requirementStep = orchestrator.addStep(run.getId(), "REQUIREMENT_ANALYSIS", request.description());
            RequirementAnalysis analysis = requirementAgent.analyze(requirement);
            orchestrator.completeStep(requirementStep.getId(), "Generated " + analysis.testScenarios().size() + " test scenarios");

            AgentStep designStep = orchestrator.addStep(run.getId(), "TEST_DESIGN", "Design executable tests from requirement analysis");
            TestDesignResponse design = testDesignAgent.design(requirement, analysis);
            orchestrator.completeStep(designStep.getId(), "Designed " + design.testCases().size() + " test cases");

            List<String> generated = new ArrayList<>();
            int index = 1;
            for (TestCase test : design.testCases()) {
                AgentStep automationStep = orchestrator.addStep(run.getId(), "AUTOMATION_GENERATION", test.id() + " - " + test.title());
                AutomationResponse automation = automationAgent.generate(new AutomationRequest(
                        test.id(), test.title(), request.url(), test.steps(), test.expectedResult()));
                generated.add(automation.fileName());
                orchestrator.completeStep(automationStep.getId(), "Generated " + automation.fileName());
                index++;
            }

            String summary = "Pipeline completed: requirement analysis -> test design -> automation generation (" + generated.size() + " artifacts)";
            AgentRun completed = orchestrator.complete(run.getId(), summary);
            return new PipelineResult(completed.getId(), completed.getStatus(), analysis.testScenarios().size(), design.testCases().size(), generated);
        } catch (Exception e) {
            run.fail(e.getMessage() == null ? "Pipeline failed" : e.getMessage());
            throw e;
        }
    }

    public record PipelineRequest(String title, String description, List<String> acceptanceCriteria, String url) {}
    public record PipelineResult(UUID runId, String status, int scenarios, int testCases, List<String> automationArtifacts) {}
}
