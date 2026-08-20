package com.aiqa.pipeline;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PipelineRerunServiceTest {
    private final PipelineRunRepository runs = mock(PipelineRunRepository.class);
    private final FullPipelineService pipeline = mock(FullPipelineService.class);
    private final PipelineRerunService service = new PipelineRerunService(runs, pipeline, new ObjectMapper());

    @Test
    void reconstructsCompletedRequirementAndUsesPersistedTargetUrl() {
        UUID sourceId = UUID.randomUUID();
        PipelineRun source = new PipelineRun("demo-company", "checkout.md");
        source.complete("""
                {"targetUrl":"https://uat.example.com","requirements":[{"title":"Checkout","description":"User completes checkout","acceptanceCriteria":["Order is confirmed"]}]}
                """);
        when(runs.findById(sourceId)).thenReturn(Optional.of(source));
        when(runs.save(any(PipelineRun.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.rerun(sourceId);

        ArgumentCaptor<String> requirement = ArgumentCaptor.forClass(String.class);
        verify(pipeline).runInBackground(any(), requirement.capture(), eq("checkout.md"), eq("https://uat.example.com"), eq(true));
        String reconstructed = requirement.getValue();
        org.junit.jupiter.api.Assertions.assertTrue(reconstructed.contains("Checkout"));
        org.junit.jupiter.api.Assertions.assertTrue(reconstructed.contains("User completes checkout"));
        org.junit.jupiter.api.Assertions.assertTrue(reconstructed.contains("Acceptance Criteria: Order is confirmed"));
    }

    @Test
    void rejectsHistoricalRunWithoutReusableResult() {
        UUID sourceId = UUID.randomUUID();
        when(runs.findById(sourceId)).thenReturn(Optional.of(new PipelineRun("demo-company", "old.md")));

        assertThrows(IllegalStateException.class, () -> service.rerun(sourceId));
        verify(pipeline, never()).runInBackground(any(), any(), any(), any(), anyBoolean());
    }
}
