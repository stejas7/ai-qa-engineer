package com.aiqa.platform;

import com.aiqa.security.AppUser;
import com.aiqa.security.AppUserRepository;
import com.aiqa.security.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlatformAdminBootstrapTest {
    @Mock AppUserRepository users;
    @Mock PasswordEncoder encoder;

    @Test
    void doesNothingWhenBootstrapNotConfigured() {
        new PlatformAdminBootstrap(users, encoder, "", "").run(new DefaultApplicationArguments());
        verifyNoInteractions(users, encoder);
    }

    @Test
    void createsFirstPlatformAdminWithoutPersistingRawPassword() {
        when(users.findAll()).thenReturn(List.of());
        when(users.existsByEmailIgnoreCase("owner@example.test")).thenReturn(false);
        when(encoder.encode("StrongPassword123!")).thenReturn("bcrypt-hash");

        new PlatformAdminBootstrap(users, encoder, "Owner@Example.Test", "StrongPassword123!")
                .run(new DefaultApplicationArguments());

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(users).save(captor.capture());
        AppUser saved = captor.getValue();
        assertEquals("owner@example.test", saved.getEmail());
        assertEquals(UserRole.PLATFORM_ADMIN, saved.getRole());
        assertEquals(PlatformAdminBootstrap.PLATFORM_TENANT_ID, saved.getCompanyId());
        assertEquals("bcrypt-hash", saved.getPasswordHash());
        assertNotEquals("StrongPassword123!", saved.getPasswordHash());
    }

    @Test
    void refusesPartialConfiguration() {
        PlatformAdminBootstrap bootstrap = new PlatformAdminBootstrap(users, encoder, "owner@example.test", "");
        assertThrows(IllegalStateException.class, () -> bootstrap.run(new DefaultApplicationArguments()));
    }

    @Test
    void refusesWeakPassword() {
        PlatformAdminBootstrap bootstrap = new PlatformAdminBootstrap(users, encoder, "owner@example.test", "too-short");
        assertThrows(IllegalStateException.class, () -> bootstrap.run(new DefaultApplicationArguments()));
    }

    @Test
    void doesNotCreateSecondPlatformAdmin() {
        AppUser existing = new AppUser(PlatformAdminBootstrap.PLATFORM_TENANT_ID, "existing@example.test", "hash", UserRole.PLATFORM_ADMIN);
        when(users.findAll()).thenReturn(List.of(existing));

        new PlatformAdminBootstrap(users, encoder, "owner@example.test", "StrongPassword123!")
                .run(new DefaultApplicationArguments());

        verify(users, never()).save(any());
        verify(encoder, never()).encode(anyString());
    }
}
