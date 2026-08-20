package com.aiqa.application;

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

class ProductRegistryServiceTest {
    private final ApplicationTargetRepository targets = mock(ApplicationTargetRepository.class);
    private final AppUserRepository users = mock(AppUserRepository.class);
    private final ProductRegistryService service = new ProductRegistryService(targets, users);

    @Test
    void createsProductForAuthenticatedCompanyWithoutClientCompanyId() {
        UUID companyId = UUID.randomUUID();
        AppUser admin = new AppUser(companyId, "admin@example.com", "hash", UserRole.COMPANY_ADMIN);
        when(users.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));
        when(targets.existsByCompanyIdAndNameIgnoreCaseAndEnvironmentIgnoreCase(companyId, "Portal", "UAT")).thenReturn(false);
        when(targets.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationTarget created = service.create("admin@example.com",
                new ProductRegistryService.CreateProductEnvironmentRequest("Portal", "https://uat.example.com", "uat", "none"));

        assertEquals(companyId, created.getCompanyId());
        assertEquals("UAT", created.getEnvironment());
        assertEquals("NONE", created.getAuthType());
    }

    @Test
    void listsOnlyAuthenticatedCompanyProducts() {
        UUID companyId = UUID.randomUUID();
        AppUser tester = new AppUser(companyId, "tester@example.com", "hash", UserRole.TESTER);
        when(users.findByEmailIgnoreCase("tester@example.com")).thenReturn(Optional.of(tester));
        when(targets.findByCompanyIdAndActiveTrueOrderByCreatedAtDesc(companyId)).thenReturn(List.of());

        service.list("tester@example.com", true);

        verify(targets).findByCompanyIdAndActiveTrueOrderByCreatedAtDesc(companyId);
        verify(targets, never()).findByActiveTrueOrderByCreatedAtDesc();
    }

    @Test
    void deniesCrossTenantProductDeactivation() {
        UUID companyA = UUID.randomUUID();
        UUID companyB = UUID.randomUUID();
        AppUser admin = new AppUser(companyA, "admin@example.com", "hash", UserRole.COMPANY_ADMIN);
        ApplicationTarget target = new ApplicationTarget("Other", "https://other.example.com", "UAT", "NONE", companyB);
        when(users.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));
        when(targets.findById(any())).thenReturn(Optional.of(target));

        assertThrows(SecurityException.class, () -> service.setActive("admin@example.com", UUID.randomUUID(), false));
        verify(targets, never()).save(any());
    }

    @Test
    void testerCannotRegisterProduct() {
        AppUser tester = new AppUser(UUID.randomUUID(), "tester@example.com", "hash", UserRole.TESTER);
        when(users.findByEmailIgnoreCase("tester@example.com")).thenReturn(Optional.of(tester));

        assertThrows(SecurityException.class, () -> service.create("tester@example.com",
                new ProductRegistryService.CreateProductEnvironmentRequest("Portal", "https://uat.example.com", "UAT", "NONE")));
    }
}
