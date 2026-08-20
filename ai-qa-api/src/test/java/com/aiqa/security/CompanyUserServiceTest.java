package com.aiqa.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CompanyUserServiceTest {
    private final AppUserRepository users = mock(AppUserRepository.class);
    private final CompanyUserService service = new CompanyUserService(users, new BCryptPasswordEncoder());

    @Test
    void companyAdminCreatesUserOnlyInsideOwnTenant() {
        UUID companyId = UUID.randomUUID();
        AppUser admin = new AppUser(companyId, "admin@example.com", "hash", UserRole.COMPANY_ADMIN);
        when(users.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));
        when(users.existsByEmailIgnoreCase("tester@example.com")).thenReturn(false);
        when(users.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.createUser("ADMIN@example.com",
                new CompanyUserService.CreateUserRequest("Tester@Example.com", "StrongPassword123!", "TESTER"));

        assertEquals(companyId, result.companyId());
        assertEquals("tester@example.com", result.email());
        assertEquals("TESTER", result.role());
    }

    @Test
    void listingNeverQueriesAnotherTenant() {
        UUID companyId = UUID.randomUUID();
        AppUser admin = new AppUser(companyId, "admin@example.com", "hash", UserRole.COMPANY_ADMIN);
        when(users.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));
        when(users.findByCompanyIdOrderByCreatedAtAsc(companyId)).thenReturn(List.of(admin));

        var result = service.listUsers("admin@example.com");

        assertEquals(1, result.size());
        assertEquals(companyId, result.get(0).companyId());
        verify(users).findByCompanyIdOrderByCreatedAtAsc(companyId);
    }

    @Test
    void crossTenantDeactivationIsDenied() {
        UUID companyA = UUID.randomUUID(), companyB = UUID.randomUUID(), targetId = UUID.randomUUID();
        AppUser admin = new AppUser(companyA, "admin@example.com", "hash", UserRole.COMPANY_ADMIN);
        AppUser otherTenantUser = new AppUser(companyB, "other@example.com", "hash", UserRole.TESTER);
        when(users.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));
        when(users.findById(targetId)).thenReturn(Optional.of(otherTenantUser));

        assertThrows(SecurityException.class, () -> service.deactivateUser("admin@example.com", targetId));
        verify(users, never()).save(otherTenantUser);
    }

    @Test
    void nonAdminCannotManageUsers() {
        AppUser tester = new AppUser(UUID.randomUUID(), "tester@example.com", "hash", UserRole.TESTER);
        when(users.findByEmailIgnoreCase("tester@example.com")).thenReturn(Optional.of(tester));
        assertThrows(SecurityException.class, () -> service.listUsers("tester@example.com"));
    }

    @Test
    void companyAdminCannotCreatePrivilegedAdminRole() {
        AppUser admin = new AppUser(UUID.randomUUID(), "admin@example.com", "hash", UserRole.COMPANY_ADMIN);
        when(users.findByEmailIgnoreCase("admin@example.com")).thenReturn(Optional.of(admin));
        assertThrows(IllegalArgumentException.class, () -> service.createUser("admin@example.com",
                new CompanyUserService.CreateUserRequest("new@example.com", "StrongPassword123!", "PLATFORM_ADMIN")));
        verify(users, never()).save(any());
    }
}
