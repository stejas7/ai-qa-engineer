package com.aiqa.pipeline;

import com.aiqa.agent.AgentOrchestrator;
import com.aiqa.agent.AgentRun;
import com.aiqa.agent.AgentStep;
import com.aiqa.automation.AutomationResponse;
import com.aiqa.automation.PlaywrightAutomationService;
import com.aiqa.credential.RuntimeCredentialResolver.ResolvedCredential;
import com.aiqa.execution.ExecutionResponse;
import com.aiqa.execution.ExecutionService;
import com.aiqa.failure.FailureAnalysisService;
import com.aiqa.quality.QualityGateResponse;
import com.aiqa.quality.QualityGateService;
import com.aiqa.requirement.AiRequirementService;
import com.aiqa.requirement.Requirement;
import com.aiqa.requirement.RequirementAnalysis;
import com.aiqa.requirement.RequirementRepository;
import com.aiqa.testdesign.TestCase;
import com.aiqa.testdesign.TestDesignResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** M19 regression proof: authenticated execution evidence reaches the final release result. */
@ExtendWith(MockitoExtension.class)
class FullPipelineAuthenticatedResultTest {
    @Mock RequirementSplitter splitter;
    @Mock RequirementRepository requirementRepository;
    @Mock AiRequirementService aiRequirementService;
    @Mock com.aiqa.testdesign.TestDesignService testDesignService;
    @Mock PlaywrightAutomationService automationService;
    @Mock ExecutionService executionService;
    @Mock FailureAnalysisService failureAnalysisService;
    @Mock QualityGateService qualityGateService;
    @Mock AgentOrchestrator agentOrchestrator;
    @Mock PipelineRunRepository pipelineRunRepository;
    @Mock PipelineRun pipelineRun;
    @Mock AgentRun agentRun;
    @Mock AgentStep agentStep;
    @Mock ResolvedCredential credential;

    private FullPipelineService service;

    @BeforeEach
    void setUp() {
        service = new FullPipelineService(splitter, requirementRepository, aiRequirementService,
                testDesignService, automationService, executionService, failureAnalysisService,
                qualityGateService, agentOrchestrator, pipelineRunRepository);
    }

    @Test
    void persistsEvidenceAndReadyDecisionAfterAuthenticatedExecution() {
        UUID runId = UUID.randomUUID();
        UUID agentRunId = UUID.randomUUID();
        UUID stepId = UUID.randomUUID();
        String targetUrl = "https://uat.example.test";
        String evidence = "/api/execution/evidence/checkout-after.png";

        when(pipelineRunRepository.findById(runId)).thenReturn(Optional.of(pipelineRun));
        when(pipelineRun.getCompany()).thenReturn("tenant-a");
        when(pipelineRun.getFileName()).thenReturn("checkout.md");
        when(agentOrchestrator.start("FULL_QA_PIPELINE", "checkout.md")).thenReturn(agentRun);
        when(agentRun.getId()).thenReturn(agentRunId);
        when(agentOrchestrator.addStep(eq(agentRunId), anyString(), anyString())).thenReturn(agentStep);
        when(agentStep.getId()).thenReturn(stepId);

        when(splitter.split(anyString(), eq("checkout.md"))).thenReturn(List.of(
                new RequirementSplitter.RequirementBlock("Checkout", "Customer can checkout", List.of("Order is confirmed"))));
        when(requirementRepository.save(any(Requirement.class))).thenAnswer(inv -> inv.getArgument(0));
        when(aiRequirementService.analyze(any(Requirement.class))).thenReturn(
                new RequirementAnalysis("Checkout analysis", List.of("Payment required"), List.of(), List.of()));

        TestCase testCase = new TestCase("TC-1", "Checkout succeeds", "FUNCTIONAL", "HIGH", "Logged in",
                List.of("verify \"Order confirmed\""), "Cart with one item", "Order confirmed", "YES");
        when(testDesignService.design(any(Requirement.class), any(RequirementAnalysis.class)))
                .thenReturn(new TestDesignResponse("Checkout", "Happy path", List.of(testCase)));
        when(automationService.generate(any())).thenReturn(
                new AutomationResponse("TC-1", "Playwright", "Java", "TC-1.java", "// generated"));
        when(executionService.run(any(), same(credential))).thenReturn(
                new ExecutionResponse("TC-1", "PASS", 125, evidence, "Authenticated execution completed"));

        QualityGateResponse ready = new QualityGateResponse("READY", "All release criteria passed",
                1, 1, 0, 1, 1, 1, 100.0, 100.0, 100.0);
        when(qualityGateService.evaluate(any())).thenReturn(ready);

        service.runInBackground(runId, "Customer can checkout", "checkout.md", targetUrl, true, credential);

        ArgumentCaptor<String> result = ArgumentCaptor.forClass(String.class);
        verify(pipelineRun).complete(result.capture());
        String json = result.getValue();

        assertTrue(json.contains("\"decision\":\"READY\""));
        assertTrue(json.contains(evidence));
        assertTrue(json.contains("\"status\":\"PASS\""));
        assertTrue(json.contains(targetUrl));
        assertFalse(json.contains("password"));
        assertFalse(json.contains("secret"));
        verify(executionService).run(any(), same(credential));
        verify(failureAnalysisService, never()).analyze(any());
    }
}
