package com.aiqa.credential;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

/**
 * Resolves M17 credential references only at execution time.
 * Secret values are never persisted, logged or returned by controller APIs.
 */
@Service
public class RuntimeCredentialResolver {
    private final CredentialProfileRepository profiles;
    private final ObjectMapper objectMapper;
    private final Function<String, String> environment;

    @Autowired
    public RuntimeCredentialResolver(CredentialProfileRepository profiles, ObjectMapper objectMapper) {
        this(profiles, objectMapper, System::getenv);
    }

    RuntimeCredentialResolver(CredentialProfileRepository profiles,
                              ObjectMapper objectMapper,
                              Function<String, String> environment) {
        this.profiles = profiles;
        this.objectMapper = objectMapper;
        this.environment = environment;
    }

    public CredentialReadiness readiness(UUID companyId, UUID applicationTargetId) {
        if (companyId == null || applicationTargetId == null) return new CredentialReadiness(false, false, "Credential profile is not selected");
        return profiles.findByApplicationTargetId(applicationTargetId)
                .map(profile -> {
                    if (!companyId.equals(profile.getCompanyId())) {
                        throw new SecurityException("Cross-tenant credential access denied");
                    }
                    if (!profile.isActive()) return new CredentialReadiness(true, false, "Credential profile is inactive");
                    String value = environment.apply(environmentName(profile.getSecretReference()));
                    return value == null || value.isBlank()
                            ? new CredentialReadiness(true, false, "Runtime secret is not configured")
                            : new CredentialReadiness(true, true, "Credential is ready for runtime use");
                })
                .orElseGet(() -> new CredentialReadiness(false, false, "Credential profile is not configured"));
    }

    public ResolvedCredential resolve(UUID companyId, UUID applicationTargetId) {
        CredentialProfile profile = profiles.findByApplicationTargetId(applicationTargetId)
                .orElseThrow(() -> new IllegalStateException("Credential profile is not configured"));
        if (!companyId.equals(profile.getCompanyId())) throw new SecurityException("Cross-tenant credential access denied");
        if (!profile.isActive()) throw new IllegalStateException("Credential profile is inactive");

        String secret = environment.apply(environmentName(profile.getSecretReference()));
        if (secret == null || secret.isBlank()) throw new IllegalStateException("Runtime secret is not configured");

        return switch (profile.getType()) {
            case USERNAME_PASSWORD -> parseUsernamePassword(secret);
            case API_TOKEN -> ResolvedCredential.apiToken(secret);
            case OAUTH_CLIENT -> parseOauthClient(secret);
        };
    }

    private ResolvedCredential parseUsernamePassword(String secret) {
        JsonNode json = parseJson(secret, "USERNAME_PASSWORD runtime secret must be JSON");
        String username = requiredText(json, "username");
        String password = requiredText(json, "password");
        return ResolvedCredential.usernamePassword(username, password);
    }

    private ResolvedCredential parseOauthClient(String secret) {
        JsonNode json = parseJson(secret, "OAUTH_CLIENT runtime secret must be JSON");
        String clientId = requiredText(json, "clientId");
        String clientSecret = requiredText(json, "clientSecret");
        return ResolvedCredential.oauthClient(clientId, clientSecret);
    }

    private JsonNode parseJson(String value, String error) {
        try {
            return objectMapper.readTree(value);
        } catch (Exception e) {
            throw new IllegalStateException(error);
        }
    }

    private String requiredText(JsonNode json, String field) {
        JsonNode node = json.get(field);
        if (node == null || !node.isTextual() || node.asText().isBlank()) {
            throw new IllegalStateException("Runtime credential field is missing: " + field);
        }
        return node.asText();
    }

    private String environmentName(String reference) {
        if (reference == null || !reference.startsWith("env:")) {
            throw new IllegalStateException("Unsupported secret reference");
        }
        return reference.substring(4).trim().toUpperCase(Locale.ROOT);
    }

    public record CredentialReadiness(boolean profileConfigured, boolean runtimeReady, String status) {}

    /** Sensitive runtime object. toString intentionally never exposes credential values. */
    public static final class ResolvedCredential {
        private final CredentialProfile.CredentialType type;
        private final String principal;
        private final String secret;

        private ResolvedCredential(CredentialProfile.CredentialType type, String principal, String secret) {
            this.type = type;
            this.principal = principal;
            this.secret = secret;
        }

        static ResolvedCredential usernamePassword(String username, String password) {
            return new ResolvedCredential(CredentialProfile.CredentialType.USERNAME_PASSWORD, username, password);
        }

        static ResolvedCredential apiToken(String token) {
            return new ResolvedCredential(CredentialProfile.CredentialType.API_TOKEN, null, token);
        }

        static ResolvedCredential oauthClient(String clientId, String clientSecret) {
            return new ResolvedCredential(CredentialProfile.CredentialType.OAUTH_CLIENT, clientId, clientSecret);
        }

        public CredentialProfile.CredentialType type() { return type; }
        public String principal() { return principal; }
        public String secret() { return secret; }

        @Override
        public String toString() {
            return "ResolvedCredential{type=" + type + ", principal=<redacted>, secret=<redacted>}";
        }
    }
}
