package com.aiqa.platform;

import com.aiqa.security.AppUser;
import com.aiqa.security.AppUserRepository;
import com.aiqa.security.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

/**
 * One-time M20 platform-owner bootstrap.
 *
 * <p>The account is created only when both environment-backed properties are supplied and no
 * PLATFORM_ADMIN already exists. Password material is BCrypt encoded immediately and never logged.
 */
@Component
public class PlatformAdminBootstrap implements ApplicationRunner {
    static final UUID PLATFORM_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public PlatformAdminBootstrap(AppUserRepository users,
                                  PasswordEncoder passwordEncoder,
                                  @Value("${ai-uat.platform-admin.email:}") String email,
                                  @Value("${ai-uat.platform-admin.password:}") String password) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.email = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        this.password = password == null ? "" : password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() && password.isBlank()) return;
        if (email.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Platform admin bootstrap requires both email and password");
        }
        if (password.length() < 12) {
            throw new IllegalStateException("Platform admin bootstrap password must contain at least 12 characters");
        }

        boolean platformAdminExists = users.findAll().stream()
                .anyMatch(user -> user.getRole() == UserRole.PLATFORM_ADMIN);
        if (platformAdminExists) return;

        if (users.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException("Platform admin bootstrap email already belongs to another account");
        }

        users.save(new AppUser(PLATFORM_TENANT_ID, email, passwordEncoder.encode(password), UserRole.PLATFORM_ADMIN));
    }
}
