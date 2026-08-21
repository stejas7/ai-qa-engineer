package com.aiqa.integration;

import com.aiqa.security.AppUser;
import com.aiqa.security.AppUserRepository;
import com.aiqa.security.UserRole;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

/**
 * Builds tenant-bound enterprise integration execution plans without receiving
 * or exposing provider credential values.
 *
 * @author Tejas Shah
 */
@Service
public class EnterpriseIntegrationPlanService {

    private final EnterpriseIntegrationCatalog catalog;
    private final AppUserRepository users;

    public EnterpriseIntegrationPlanService(EnterpriseIntegrationCatalog catalog, AppUserRepository users) {
        this.catalog = catalog;
        this.users = users;
    }

    /** Creates a validated plan in the authenticated user's tenant context. */
    public IntegrationPlan plan(String actorEmail, String providerKey, String action, String eventType) {
        AppUser actor = requireManager(actorEmail);
        EnterpriseIntegrationCatalog.ProviderDefinition provider = catalog.requireProvider(providerKey);

        String normalizedAction = normalize(action, "action");
        String normalizedEvent = normalize(eventType, "event type");
        if (!provider.supportedActions().contains(normalizedAction)) {
            throw new IllegalArgumentException("Action is not supported by provider");
        }
        if (!provider.supportedEvents().contains(normalizedEvent)) {
            throw new IllegalArgumentException("Event type is not supported by provider");
        }

        return new IntegrationPlan(
                UUID.randomUUID(),
                actor.getCompanyId(),
                provider.key(),
                provider.displayName(),
                normalizedAction,
                normalizedEvent,
                provider.authenticationMode(),
                "CREDENTIAL_REFERENCE_REQUIRED",
                "TENANT_BOUND",
                false
        );
    }

    private AppUser requireManager(String email) {
        if (email == null || email.isBlank()) {
            throw new SecurityException("Authentication required");
        }
        AppUser actor = users.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new SecurityException("Authenticated user not found"));
        if (!actor.isActive()) {
            throw new SecurityException("User is inactive");
        }
        if (actor.getRole() != UserRole.COMPANY_ADMIN && actor.getRole() != UserRole.QA_MANAGER) {
            throw new SecurityException("Company admin or QA manager role required");
        }
        return actor;
    }

    private String normalize(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    /** Safe, tenant-scoped planning output for orchestration and UI preview. */
    public record IntegrationPlan(
            UUID planId,
            UUID companyId,
            String providerKey,
            String providerName,
            String action,
            String eventType,
            String authenticationMode,
            String credentialState,
            String tenantScope,
            boolean executable
    ) {
    }
}
