package com.aiqa.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AppUserServiceTest {
    private final AppUserRepository users = mock(AppUserRepository.class);
    private final AppUserService service = new AppUserService(users, new BCryptPasswordEncoder());

    @Test
    void createsCompanyAdminWithNormalizedEmailAndHashedPassword() {
        UUID companyId = UUID.randomUUID();
        when(users.existsByEmailIgnoreCase("admin@example.com")).thenReturn(false);
        when(users.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        AppUser user = service.createCompanyAdmin(companyId, " Admin@Example.com ", "StrongPassword123!");

        assertEquals(companyId, user.getCompanyId());
        assertEquals("admin@example.com", user.getEmail());
        assertEquals(UserRole.COMPANY_ADMIN, user.getRole());
        assertNotEquals("StrongPassword123!", user.getPasswordHash());
        assertTrue(new BCryptPasswordEncoder().matches("StrongPassword123!", user.getPasswordHash()));
    }

    @Test
    void rejectsWeakPasswordBeforePersistence() {
        assertThrows(IllegalArgumentException.class,
                () -> service.createCompanyAdmin(UUID.randomUUID(), "admin@example.com", "short"));
        verify(users, never()).save(any());
    }

    @Test
    void rejectsDuplicateEmail() {
        when(users.existsByEmailIgnoreCase("admin@example.com")).thenReturn(true);
        assertThrows(IllegalStateException.class,
                () -> service.createCompanyAdmin(UUID.randomUUID(), "admin@example.com", "StrongPassword123!"));
    }

    @Test
    void rejectsInactiveUser() {
        AppUser user = new AppUser(UUID.randomUUID(), "user@example.com", "hash", UserRole.TESTER);
        user.deactivate();
        when(users.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
        assertThrows(IllegalStateException.class, () -> service.loadActiveUser("USER@example.com"));
    }
}
