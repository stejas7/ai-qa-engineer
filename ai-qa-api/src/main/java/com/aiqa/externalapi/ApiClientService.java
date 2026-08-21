package com.aiqa.externalapi;

import com.aiqa.security.AppUser;
import com.aiqa.security.AppUserRepository;
import com.aiqa.security.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/** M21 tenant API-client lifecycle and client-credentials token issuance. */
@Service
public class ApiClientService {
    private static final Duration TOKEN_TTL = Duration.ofMinutes(30);

    private final ApiClientRepository clients;
    private final ExternalApiAccessTokenRepository tokens;
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;

    public ApiClientService(ApiClientRepository clients,
                            ExternalApiAccessTokenRepository tokens,
                            AppUserRepository users,
                            PasswordEncoder passwordEncoder) {
        this.clients = clients;
        this.tokens = tokens;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public List<ClientSummary> listForCompanyAdmin(String actorEmail) {
        AppUser actor = requireCompanyAdmin(actorEmail);
        return clients.findByCompanyIdOrderByCreatedAtAsc(actor.getCompanyId()).stream().map(ClientSummary::from).toList();
    }

    @Transactional
    public ClientSecret create(String actorEmail, CreateClientRequest request) {
        AppUser actor = requireCompanyAdmin(actorEmail);
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("client name is required");
        }
        Set<ApiScope> scopes = parseScopes(request.scopes());
        String clientId = "aiuat_" + UUID.randomUUID().toString().replace("-", "");
        String rawSecret = randomSecret();
        ApiClient saved = clients.save(new ApiClient(actor.getCompanyId(), request.name().trim(), clientId,
                passwordEncoder.encode(rawSecret), encodeScopes(scopes)));
        return new ClientSecret(saved.getId(), saved.getCompanyId(), saved.getName(), saved.getClientId(), rawSecret,
                scopes.stream().map(Enum::name).sorted().toList(), saved.isActive(), saved.getCreatedAt());
    }

    @Transactional
    public ClientSecret rotateSecret(String actorEmail, UUID clientRecordId) {
        AppUser actor = requireCompanyAdmin(actorEmail);
        ApiClient client = requireTenantClient(actor.getCompanyId(), clientRecordId);
        String rawSecret = randomSecret();
        client.rotateSecret(passwordEncoder.encode(rawSecret));
        tokens.findByApiClientIdAndRevokedFalse(client.getId()).forEach(ApiAccessToken::revoke);
        clients.save(client);
        return new ClientSecret(client.getId(), client.getCompanyId(), client.getName(), client.getClientId(), rawSecret,
                decodeScopes(client.getScopes()).stream().map(Enum::name).sorted().toList(), client.isActive(), client.getCreatedAt());
    }

    @Transactional
    public ClientSummary deactivate(String actorEmail, UUID clientRecordId) {
        AppUser actor = requireCompanyAdmin(actorEmail);
        ApiClient client = requireTenantClient(actor.getCompanyId(), clientRecordId);
        client.deactivate();
        tokens.findByApiClientIdAndRevokedFalse(client.getId()).forEach(ApiAccessToken::revoke);
        return ClientSummary.from(clients.save(client));
    }

    @Transactional
    public TokenResponse issueToken(String clientId, String clientSecret, String requestedScopes) {
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            throw new IllegalArgumentException("client_id and client_secret are required");
        }
        ApiClient client = clients.findByClientId(clientId.trim())
                .orElseThrow(() -> new SecurityException("Invalid client credentials"));
        if (!client.isActive() || !passwordEncoder.matches(clientSecret, client.getClientSecretHash())) {
            throw new SecurityException("Invalid client credentials");
        }

        Set<ApiScope> granted = decodeScopes(client.getScopes());
        Set<ApiScope> requested = requestedScopes == null || requestedScopes.isBlank()
                ? EnumSet.copyOf(granted)
                : parseScopes(requestedScopes);
        if (!granted.containsAll(requested)) throw new SecurityException("Requested scope is not granted to this client");

        String rawToken = randomSecret();
        Instant expiresAt = Instant.now().plus(TOKEN_TTL);
        tokens.save(new ApiAccessToken(client.getId(), client.getCompanyId(), sha256(rawToken), encodeScopes(requested), expiresAt));
        return new TokenResponse(rawToken, "Bearer", TOKEN_TTL.toSeconds(), encodeScopes(requested));
    }

    public ExternalApiPrincipal authenticateBearer(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return null;
        ApiAccessToken token = tokens.findByTokenHash(sha256(rawToken)).orElse(null);
        if (token == null || token.isRevoked() || !token.getExpiresAt().isAfter(Instant.now())) return null;
        ApiClient client = clients.findById(token.getApiClientId()).orElse(null);
        if (client == null || !client.isActive() || !client.getCompanyId().equals(token.getCompanyId())) return null;
        return new ExternalApiPrincipal(client.getClientId(), client.getId(), client.getCompanyId(), decodeScopes(token.getScopes()));
    }

    private ApiClient requireTenantClient(UUID companyId, UUID clientRecordId) {
        if (clientRecordId == null) throw new IllegalArgumentException("client id is required");
        ApiClient client = clients.findById(clientRecordId).orElseThrow(() -> new IllegalArgumentException("API client not found"));
        if (!companyId.equals(client.getCompanyId())) throw new SecurityException("Cross-tenant API client access denied");
        return client;
    }

    private AppUser requireCompanyAdmin(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        AppUser actor = users.findByEmailIgnoreCase(normalized).orElseThrow(() -> new SecurityException("Authenticated user not found"));
        if (!actor.isActive() || actor.getRole() != UserRole.COMPANY_ADMIN) throw new SecurityException("Company admin role required");
        return actor;
    }

    static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    private String randomSecret() {
        byte[] bytes = new byte[48];
        new java.security.SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private Set<ApiScope> parseScopes(String raw) {
        if (raw == null || raw.isBlank()) return EnumSet.of(ApiScope.UAT_READ);
        Set<ApiScope> scopes = Arrays.stream(raw.trim().split("[ ,]+"))
                .filter(s -> !s.isBlank())
                .map(s -> ApiScope.valueOf(s.trim().toUpperCase(Locale.ROOT)))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ApiScope.class)));
        if (scopes.isEmpty()) throw new IllegalArgumentException("At least one scope is required");
        return scopes;
    }

    private String encodeScopes(Set<ApiScope> scopes) {
        return scopes.stream().map(Enum::name).sorted().collect(Collectors.joining(" "));
    }

    private Set<ApiScope> decodeScopes(String raw) {
        if (raw == null || raw.isBlank()) return EnumSet.noneOf(ApiScope.class);
        return parseScopes(raw);
    }

    public record CreateClientRequest(String name, String scopes) {}
    public record ClientSummary(UUID id, UUID companyId, String name, String clientId, List<String> scopes,
                                boolean active, Instant createdAt) {
        static ClientSummary from(ApiClient client) {
            List<String> scopes = client.getScopes() == null || client.getScopes().isBlank()
                    ? List.of()
                    : Arrays.stream(client.getScopes().split(" ")).filter(s -> !s.isBlank()).sorted().toList();
            return new ClientSummary(client.getId(), client.getCompanyId(), client.getName(), client.getClientId(), scopes,
                    client.isActive(), client.getCreatedAt());
        }
    }
    public record ClientSecret(UUID id, UUID companyId, String name, String clientId, String clientSecret,
                               List<String> scopes, boolean active, Instant createdAt) {}
    public record TokenResponse(String access_token, String token_type, long expires_in, String scope) {}
}
