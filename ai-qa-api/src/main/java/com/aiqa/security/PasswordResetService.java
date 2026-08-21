package com.aiqa.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class PasswordResetService {
    private static final Duration TTL = Duration.ofMinutes(30);
    private final AppUserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(AppUserRepository users, PasswordResetTokenRepository tokens, PasswordEncoder encoder) {
        this.users = users;
        this.tokens = tokens;
        this.encoder = encoder;
    }

    /** Returns a token only to the caller so a delivery adapter can send it; unknown emails remain indistinguishable. */
    public ResetTicket request(String email) {
        if (email == null || email.isBlank()) return new ResetTicket(null, 1800);
        return users.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .filter(AppUser::isActive)
                .map(user -> {
                    String raw = randomToken();
                    tokens.save(new PasswordResetToken(user.getId(), sha256(raw), Instant.now().plus(TTL)));
                    return new ResetTicket(raw, TTL.toSeconds());
                })
                .orElseGet(() -> new ResetTicket(null, TTL.toSeconds()));
    }

    @Transactional
    public void reset(String rawToken, String newPassword) {
        validatePassword(newPassword);
        if (rawToken == null || rawToken.isBlank()) throw new IllegalArgumentException("reset token is required");
        PasswordResetToken token = tokens.findByTokenHashAndUsedFalse(sha256(rawToken))
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));
        if (token.getExpiresAt().isBefore(Instant.now())) throw new IllegalArgumentException("Invalid or expired reset token");
        AppUser user = users.findById(token.getUserId()).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.replacePasswordHash(encoder.encode(newPassword));
        users.save(user);
        token.markUsed();
        tokens.save(token);
    }

    private void validatePassword(String value) {
        if (value == null || value.length() < 12) throw new IllegalArgumentException("Password must be at least 12 characters");
    }

    private String randomToken() {
        byte[] bytes = new byte[32]; random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }

    public record ResetTicket(String token, long expiresInSeconds) {}
}
