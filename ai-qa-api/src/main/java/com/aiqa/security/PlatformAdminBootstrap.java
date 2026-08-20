package com.aiqa.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

/**
 * Keeps the single PLATFORM_ADMIN login synchronized with deployment secrets.
 * Tenant users are never modified by this component.
 */
@Component
public class PlatformAdminBootstrap implements ApplicationRunner {
    private static final UUID PLATFORM_SCOPE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String configuredEmail;
    private final String configuredPassword;

    public PlatformAdminBootstrap(AppUserRepository users,
                                  PasswordEncoder passwordEncoder,
                                  @Value("${AI_UAT_PLATFORM_ADMIN_EMAIL:}") String configuredEmail,
                                  @Value("${AI_UAT_PLATFORM_ADMIN_PASSWORD:}") String configuredPassword) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.configuredEmail = configuredEmail == null ? "" : configuredEmail.trim().toLowerCase(Locale.ROOT);
        this.configuredPassword = configuredPassword == null ? "" : configuredPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (configuredEmail.isBlank() || configuredPassword.isBlank()) return;
        if (!configuredEmail.contains("@")) throw new IllegalStateException("AI_UAT_PLATFORM_ADMIN_EMAIL is invalid");
        if (configuredPassword.length() < 12) throw new IllegalStateException("AI_UAT_PLATFORM_ADMIN_PASSWORD must be at least 12 characters");

        AppUser admin = users.findFirstByRole(UserRole.PLATFORM_ADMIN)
                .orElseGet(() -> new AppUser(PLATFORM_SCOPE_ID, configuredEmail,
                        passwordEncoder.encode(configuredPassword), UserRole.PLATFORM_ADMIN));

        users.findByEmailIgnoreCase(configuredEmail)
                .filter(existing -> admin.getId() == null || !existing.getId().equals(admin.getId()))
                .ifPresent(existing -> {
                    throw new IllegalStateException("Configured Platform Admin email is already used by another account");
                });

        if (admin.getId() == null) {
            users.save(admin);
        } else {
            admin.synchronizePlatformAdmin(configuredEmail, passwordEncoder.encode(configuredPassword));
            users.save(admin);
        }
    }
}
