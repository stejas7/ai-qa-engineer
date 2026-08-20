package com.aiqa.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

/** M14 application-user lifecycle. Public registration is intentionally not exposed yet. */
@Service
public class AppUserService {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public AppUser createCompanyAdmin(UUID companyId, String email, String rawPassword) {
        if (companyId == null) throw new IllegalArgumentException("companyId is required");
        String normalizedEmail = normalizeEmail(email);
        validatePassword(rawPassword);
        if (users.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new IllegalStateException("User email is already registered");
        }
        return users.save(new AppUser(companyId, normalizedEmail, passwordEncoder.encode(rawPassword), UserRole.COMPANY_ADMIN));
    }

    AppUser loadActiveUser(String email) {
        AppUser user = users.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!user.isActive()) throw new IllegalStateException("User is inactive");
        return user;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("email is required");
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (!normalized.contains("@") || normalized.startsWith("@") || normalized.endsWith("@")) {
            throw new IllegalArgumentException("email is invalid");
        }
        return normalized;
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 12) {
            throw new IllegalArgumentException("password must contain at least 12 characters");
        }
    }
}
