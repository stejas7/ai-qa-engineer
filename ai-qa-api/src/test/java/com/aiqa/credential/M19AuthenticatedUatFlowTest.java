package com.aiqa.credential;

import com.aiqa.application.ApplicationTarget;
import com.aiqa.application.ApplicationTargetRepository;
import com.aiqa.pipeline.FullPipelineService;
import com.aiqa.pipeline.PipelineRun;
import com.aiqa.pipeline.PipelineRunRepository;
import com.aiqa.pipeline.RequirementFileExtractor;
import com.aiqa.pipeline.TenantUatLaunchService;
import com.aiqa.security.AppUser;
import com.aiqa.security.AppUserRepository;
import com.aiqa.security.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * M19 stabilization contract: authenticated tenant launch must resolve the product credential
 * server-side and carry only the in-memory credential into the autonomous pipeline.
 */
@ExtendWith(MockitoExtension.class)
class M19AuthenticatedUatFlowTest {
    @Mock RequirementFileExtractor extractor;
    @Mock FullPipelineService pipeline;
    @Mock PipelineRunRepository runs;
    @Mock ApplicationTargetRepository targets;
    @Mock AppUserRepository users;
    @Mock RuntimeCredentialResolver credentials;

    @Test
    void readyCredentialIsPassedToAutonomousPipelineWithoutFallingBackToUnauthenticatedLaunch() throws Exception {
        UUID companyId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        AppUser actor = new AppUser(companyId, "tester@example.test", "hash", UserRole.TESTER);
        ApplicationTarget target = mock(ApplicationTarget.class);
        MockMultipartFile file = new MockMultipartFile(
                "file", "checkout-story.txt", "text/plain",
                "Customer can sign in and complete checkout".getBytes(StandardCharsets.UTF_8));
        PipelineRun run = new PipelineRun(companyId.toString(), "checkout-story.txt");
        RuntimeCredentialResolver.ResolvedCredential credential =
                RuntimeCredentialResolver.ResolvedCredential.usernamePassword("uat-user@example.test", "runtime-only-secret");

        when(users.findByEmailIgnoreCase("tester@example.test")).thenReturn(Optional.of(actor));
        when(targets.findById(targetId)).thenReturn(Optional.of(target));
        when(target.getCompanyId()).thenReturn(companyId);
        when(target.isActive()).thenReturn(true);
        when(target.getAuthType()).thenReturn("USERNAME_PASSWORD");
        when(target.getId()).thenReturn(targetId);
        when(target.getBaseUrl()).thenReturn("https://uat.example.test");
        when(credentials.readiness(companyId, targetId))
                .thenReturn(new RuntimeCredentialResolver.CredentialReadiness(true, true, "Credential is ready for runtime use"));
        when(credentials.resolve(companyId, targetId)).thenReturn(credential);
        when(extractor.extract(file)).thenReturn("Customer can sign in and complete checkout");
        when(runs.save(any(PipelineRun.class))).thenReturn(run);

        TenantUatLaunchService service = new TenantUatLaunchService(
                extractor, pipeline, runs, targets, users, credentials);

        service.launch("TESTER@example.test", targetId, file);

        verify(credentials).readiness(companyId, targetId);
        verify(credentials).resolve(companyId, targetId);
        verify(pipeline).runInBackground(
                run.getId(),
                "Customer can sign in and complete checkout",
                "checkout-story.txt",
                "https://uat.example.test",
                true,
                credential);
        verify(pipeline, never()).runInBackground(
                any(UUID.class), anyString(), anyString(), anyString(), anyBoolean());
    }
}
