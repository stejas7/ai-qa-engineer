package com.aiqa.pipeline;

import com.aiqa.application.ApplicationTarget;
import com.aiqa.application.ApplicationTargetRepository;
import com.aiqa.security.AppUser;
import com.aiqa.security.AppUserRepository;
import com.aiqa.security.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantUatLaunchServiceTest {
    @Mock RequirementFileExtractor extractor;
    @Mock FullPipelineService pipeline;
    @Mock PipelineRunRepository runs;
    @Mock ApplicationTargetRepository targets;
    @Mock AppUserRepository users;

    private TenantUatLaunchService service;

    @BeforeEach
    void setUp() {
        service = new TenantUatLaunchService(extractor, pipeline, runs, targets, users);
    }

    @Test
    void launchesUsingServerResolvedTenantAndTarget() throws Exception {
        UUID companyId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        AppUser actor = actor(companyId, UserRole.TESTER);
        ApplicationTarget target = target(companyId, true, "https://uat.example.test");
        MockMultipartFile file = new MockMultipartFile("file", "story.txt", "text/plain", "Customer can checkout".getBytes(StandardCharsets.UTF_8));
        PipelineRun run = new PipelineRun(companyId.toString(), "story.txt");

        when(users.findByEmailIgnoreCase("tester@example.test")).thenReturn(Optional.of(actor));
        when(targets.findById(targetId)).thenReturn(Optional.of(target));
        when(extractor.extract(file)).thenReturn("Customer can checkout");
        when(runs.save(any(PipelineRun.class))).thenReturn(run);

        service.launch("TESTER@example.test", targetId, file);

        verify(pipeline).runInBackground(run.getId(), "Customer can checkout", "story.txt", "https://uat.example.test", true);
    }

    @Test
    void deniesCrossTenantLaunch() {
        UUID targetId = UUID.randomUUID();
        when(users.findByEmailIgnoreCase("tester@example.test")).thenReturn(Optional.of(actor(UUID.randomUUID(), UserRole.TESTER)));
        when(targets.findById(targetId)).thenReturn(Optional.of(target(UUID.randomUUID(), true, "https://other.example.test")));

        assertThrows(SecurityException.class, () -> service.launch("tester@example.test", targetId, requirement()));
        verifyNoInteractions(pipeline);
    }

    @Test
    void viewerCannotExecuteUat() {
        when(users.findByEmailIgnoreCase("viewer@example.test")).thenReturn(Optional.of(actor(UUID.randomUUID(), UserRole.VIEWER)));

        assertThrows(SecurityException.class, () -> service.launch("viewer@example.test", UUID.randomUUID(), requirement()));
        verifyNoInteractions(targets, pipeline);
    }

    @Test
    void inactiveTargetCannotExecuteUat() {
        UUID companyId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        when(users.findByEmailIgnoreCase("tester@example.test")).thenReturn(Optional.of(actor(companyId, UserRole.TESTER)));
        when(targets.findById(targetId)).thenReturn(Optional.of(target(companyId, false, "https://uat.example.test")));

        assertThrows(IllegalStateException.class, () -> service.launch("tester@example.test", targetId, requirement()));
        verifyNoInteractions(pipeline);
    }

    private MockMultipartFile requirement() {
        return new MockMultipartFile("file", "story.txt", "text/plain", "requirement".getBytes(StandardCharsets.UTF_8));
    }

    private AppUser actor(UUID companyId, UserRole role) {
        return new AppUser(companyId, role.name().toLowerCase() + "@example.test", "hash", role);
    }

    private ApplicationTarget target(UUID companyId, boolean active, String baseUrl) {
        ApplicationTarget target = new ApplicationTarget("Checkout", baseUrl, "UAT", "NONE", companyId);
        target.setActive(active);
        return target;
    }
}
