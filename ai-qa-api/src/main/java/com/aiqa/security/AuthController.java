package com.aiqa.security;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Minimal authenticated identity endpoint for M14. Never returns password material. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AppUserService users;

    public AuthController(AppUserService users) {
        this.users = users;
    }

    @GetMapping("/me")
    public CurrentUser me(Authentication authentication) {
        AppUser user = users.loadActiveUser(authentication.getName());
        return new CurrentUser(user.getId(), user.getCompanyId(), user.getEmail(), user.getRole().name());
    }

    public record CurrentUser(UUID id, UUID companyId, String email, String role) {}
}
