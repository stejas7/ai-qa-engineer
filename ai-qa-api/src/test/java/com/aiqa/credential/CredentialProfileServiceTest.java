package com.aiqa.credential;

import com.aiqa.application.ApplicationTarget;
import com.aiqa.application.ApplicationTargetRepository;
import com.aiqa.security.AppUser;
import com.aiqa.security.AppUserRepository;
import com.aiqa.security.UserRole;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CredentialProfileServiceTest {
    private final CredentialProfileRepository profiles = mock(CredentialProfileRepository.class);
    private final ApplicationTargetRepository targets = mock(ApplicationTargetRepository.class);
    private final AppUserRepository users = mock(AppUserRepository.class);
    private final CredentialProfileService service = new CredentialProfileService(profiles, targets, users);

    @Test
    void configuresReferenceWithoutReturningSecretReference() {
        UUID companyId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        AppUser admin = new AppUser(companyId, "admin@example.com", "hash", UserRole.COMPANY_ADMIN);
        ApplicationTarget target = mock(ApplicationTarget.class);
        when(target.getId()).thenReturn(targetId);
        when(target.getCompanyId()).thenReturn(companyId);
        when(target.isActive()).thenReturn(true);
        when(users.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));
        when(targets.findById(targetId)).thenReturn(Optional.of(target));
        when(profiles.findByApplicationTargetId(targetId)).thenReturn(Optional.empty());
        when(profiles.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        CredentialProfileService.CredentialProfileSummary result = service.configure("admin@example.com",
                new CredentialProfileService.ConfigureCredentialProfileRequest(
                        targetId, "username_password", "env:TARGET_PORTAL_UAT"));

        assertEquals(targetId, result.applicationTargetId());
        assertEquals("USERNAME_PASSWORD", result.type());
        assertTrue(result.configured());
        verify(profiles).save(argThat(profile ->
                "env:TARGET_PORTAL_UAT".equals(profile.getSecretReference()) &&
                companyId.equals(profile.getCompanyId())));
    }

    @Test
    void rejectsRawSecretInsteadOfReference() {
        UUID companyId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        AppUser admin = new AppUser(companyId, "admin@example.com", "hash", UserRole.COMPANY_ADMIN);
        ApplicationTarget target = mock(ApplicationTarget.class);
        when(target.getId()).thenReturn(targetId);
        when(target.getCompanyId()).thenReturn(companyId);
        when(target.isActive()).thenReturn(true);
        when(users.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));
        when(targets.findById(targetId)).thenReturn(Optional.of(target));
        when(profiles.findByApplicationTargetId(targetId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.configure("admin@example.com",
                new CredentialProfileService.ConfigureCredentialProfileRequest(
                        targetId, "API_TOKEN", "my-real-token-value")));
        verify(profiles, never()).save(any());
    }

    @Test
    void deniesCrossTenantConfiguration() {
        UUID companyA = UUID.randomUUID();
        UUID companyB = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        AppUser admin = new AppUser(companyA, "admin@example.com", "hash", UserRole.COMPANY_ADMIN);
        ApplicationTarget target = mock(ApplicationTarget.class);
        when(target.getId()).thenReturn(targetId);
        when(target.getCompanyId()).thenReturn(companyB);
        when(target.isActive()).thenReturn(true);
        when(users.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));
        when(targets.findById(targetId)).thenReturn(Optional.of(target));

        assertThrows(SecurityException.class, () -> service.configure("admin@example.com",
                new CredentialProfileService.ConfigureCredentialProfileRequest(
                        targetId, "API_TOKEN", "env:TARGET_TOKEN")));
        verify(profiles, never()).save(any());
    }

    @Test
    void testerCannotConfigureCredentialProfile() {
        AppUser tester = new AppUser(UUID.randomUUID(), "tester@example.com", "hash", UserRole.TESTER);
        when(users.findByEmailIgnoreCase("tester@example.com")).thenReturn(Optional.of(tester));

        assertThrows(SecurityException.class, () -> service.configure("tester@example.com",
                new CredentialProfileService.ConfigureCredentialProfileRequest(
                        UUID.randomUUID(), "API_TOKEN", "env:TARGET_TOKEN")));
    }

    @Test
    void listsOnlyAuthenticatedCompanyProfiles() {
        UUID companyId = UUID.randomUUID();
        AppUser viewer = new AppUser(companyId, "viewer@example.com", "hash", UserRole.VIEWER);
        when(users.findByEmailIgnoreCase("viewer@example.com")).thenReturn(Optional.of(viewer));
        when(profiles.findByCompanyIdOrderByCreatedAtDesc(companyId)).thenReturn(List.of());

        service.list("viewer@example.com");

        verify(profiles).findByCompanyIdOrderByCreatedAtDesc(companyId);
    }
}
