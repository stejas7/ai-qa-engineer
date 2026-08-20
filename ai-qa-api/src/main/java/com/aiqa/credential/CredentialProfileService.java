package com.aiqa.credential;

import com.aiqa.application.ApplicationTarget;
import com.aiqa.application.ApplicationTargetRepository;
import com.aiqa.security.AppUser;
import com.aiqa.security.AppUserRepository;
import com.aiqa.security.UserRole;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * M17 tenant-safe credential profile lifecycle.
 *
 * <p>Only references to runtime environment variables are stored. Secret values are never accepted or returned.</p>
 */
@Service
public class CredentialProfileService {
    private final CredentialProfileRepository profiles;
    private final ApplicationTargetRepository targets;
    private final AppUserRepository users;

    public CredentialProfileService(CredentialProfileRepository profiles,
                                    ApplicationTargetRepository targets,
                                    AppUserRepository users) {
        this.profiles = profiles;
        this.targets = targets;
        this.users = users;
    }

    public List<CredentialProfileSummary> list(String actorEmail) {
        AppUser actor = requireActiveUser(actorEmail);
        return profiles.findByCompanyIdOrderByCreatedAtDesc(actor.getCompanyId()).stream()
                .map(CredentialProfileSummary::from)
                .toList();
    }

    public CredentialProfileSummary configure(String actorEmail, ConfigureCredentialProfileRequest request) {
        AppUser actor = requireManager(actorEmail);
        if (request == null) throw new IllegalArgumentException("credential profile request is required");
        if (request.applicationTargetId() == null) throw new IllegalArgumentException("applicationTargetId is required");

        ApplicationTarget target = targets.findById(request.applicationTargetId())
                .orElseThrow(() -> new IllegalArgumentException("Product environment not found"));
        ensureSameTenant(actor, target);
        if (!target.isActive()) throw new IllegalStateException("Credential profile cannot be configured for an inactive product environment");
        if (profiles.findByApplicationTargetId(target.getId()).isPresent()) {
            throw new IllegalStateException("Credential profile is already configured for this product environment");
        }

        CredentialProfile.CredentialType type = parseType(request.type());
        String secretReference = validateSecretReference(request.secretReference());
        CredentialProfile saved = profiles.save(new CredentialProfile(
                actor.getCompanyId(), target.getId(), type, secretReference));
        return CredentialProfileSummary.from(saved);
    }

    public CredentialProfileSummary setActive(String actorEmail, UUID profileId, boolean active) {
        AppUser actor = requireManager(actorEmail);
        CredentialProfile profile = profiles.findById(profileId)
                .orElseThrow(() -> new IllegalArgumentException("Credential profile not found"));
        if (!actor.getCompanyId().equals(profile.getCompanyId())) {
            throw new SecurityException("Cross-tenant credential profile access denied");
        }
        profile.setActive(active);
        return CredentialProfileSummary.from(profiles.save(profile));
    }

    private AppUser requireManager(String email) {
        AppUser actor = requireActiveUser(email);
        if (actor.getRole() != UserRole.COMPANY_ADMIN && actor.getRole() != UserRole.QA_MANAGER) {
            throw new SecurityException("Company admin or QA manager role required");
        }
        return actor;
    }

    private AppUser requireActiveUser(String email) {
        if (email == null || email.isBlank()) throw new SecurityException("Authentication required");
        AppUser actor = users.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new SecurityException("Authenticated user not found"));
        if (!actor.isActive()) throw new IllegalStateException("User is inactive");
        return actor;
    }

    private void ensureSameTenant(AppUser actor, ApplicationTarget target) {
        if (!actor.getCompanyId().equals(target.getCompanyId())) {
            throw new SecurityException("Cross-tenant product credential access denied");
        }
    }

    private CredentialProfile.CredentialType parseType(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("type is required");
        try {
            return CredentialProfile.CredentialType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported credential type");
        }
    }

    private String validateSecretReference(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("secretReference is required");
        String reference = value.trim();
        if (!reference.matches("env:[A-Z][A-Z0-9_]{2,127}")) {
            throw new IllegalArgumentException("secretReference must use env:VARIABLE_NAME format");
        }
        return reference;
    }

    public record ConfigureCredentialProfileRequest(UUID applicationTargetId, String type, String secretReference) {}

    public record CredentialProfileSummary(UUID id,
                                           UUID applicationTargetId,
                                           String type,
                                           boolean configured,
                                           boolean active) {
        static CredentialProfileSummary from(CredentialProfile profile) {
            return new CredentialProfileSummary(profile.getId(), profile.getApplicationTargetId(),
                    profile.getType().name(), true, profile.isActive());
        }
    }
}
