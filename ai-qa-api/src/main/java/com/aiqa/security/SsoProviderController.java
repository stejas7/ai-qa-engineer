package com.aiqa.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/** Safe discovery endpoint for configured SSO providers. Client secrets are never exposed. */
@RestController
@RequestMapping("/api/auth/sso")
public class SsoProviderController {
    private final String googleClientId;
    private final String githubClientId;

    public SsoProviderController(
            @Value("${spring.security.oauth2.client.registration.google.client-id:}") String googleClientId,
            @Value("${spring.security.oauth2.client.registration.github.client-id:}") String githubClientId) {
        this.googleClientId = googleClientId == null ? "" : googleClientId.trim();
        this.githubClientId = githubClientId == null ? "" : githubClientId.trim();
    }

    @GetMapping("/providers")
    public Providers providers() {
        List<String> enabled = new ArrayList<>();
        if (!googleClientId.isBlank()) enabled.add("google");
        if (!githubClientId.isBlank()) enabled.add("github");
        return new Providers(enabled);
    }

    public record Providers(List<String> providers) {}
}
