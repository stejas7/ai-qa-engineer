package com.aiqa.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Keeps the existing PLATFORM_ADMIN password aligned with deployment secrets.
 * It never creates, renames, or modifies tenant users.
 */
@Component
public class PlatformAdminPasswordSynchronizer implements ApplicationRunner {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public PlatformAdminPasswordSynchronizer(
            AppUserRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${AI_UAT_PLATFORM_ADMIN_EMAIL:}") String adminEmail,
            @Value("${AI_UAT_PLATFORM_ADMIN_PASSWORD:}") String adminPassword) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail == null ? "" : adminEmail.trim();
        this.adminPassword = adminPassword == null ? "" : adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            return;
        }
        AppUser user = users.findByEmailIgnoreCase(adminEmail)
                .orElseThrow(() -> new IllegalStateException("Configured platform admin email does not exist"));
        if (user.getRole() != UserRole.PLATFORM_ADMIN) {
            throw new IllegalStateException("Configured platform admin email is not a PLATFORM_ADMIN");
        }
        user.replacePasswordHash(passwordEncoder.encode(adminPassword));
        users.save(user);
    }
}
