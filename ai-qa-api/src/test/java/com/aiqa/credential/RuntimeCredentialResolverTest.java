package com.aiqa.credential;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RuntimeCredentialResolverTest {
    private final CredentialProfileRepository profiles = mock(CredentialProfileRepository.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void resolvesUsernamePasswordFromEnvironmentWithoutExposingItInToString() {
        UUID companyId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        CredentialProfile profile = new CredentialProfile(companyId, targetId,
                CredentialProfile.CredentialType.USERNAME_PASSWORD, "env:PORTAL_UAT_CREDENTIAL");
        when(profiles.findByApplicationTargetId(targetId)).thenReturn(Optional.of(profile));
        RuntimeCredentialResolver resolver = new RuntimeCredentialResolver(profiles, mapper,
                name -> Map.of("PORTAL_UAT_CREDENTIAL", "{\"username\":\"uat.user\",\"password\":\"S3cret!\"}").get(name));

        RuntimeCredentialResolver.ResolvedCredential resolved = resolver.resolve(companyId, targetId);

        assertEquals(CredentialProfile.CredentialType.USERNAME_PASSWORD, resolved.type());
        assertEquals("uat.user", resolved.principal());
        assertEquals("S3cret!", resolved.secret());
        assertFalse(resolved.toString().contains("uat.user"));
        assertFalse(resolved.toString().contains("S3cret!"));
    }

    @Test
    void readinessDoesNotExposeEnvironmentValue() {
        UUID companyId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        CredentialProfile profile = new CredentialProfile(companyId, targetId,
                CredentialProfile.CredentialType.API_TOKEN, "env:PORTAL_TOKEN");
        when(profiles.findByApplicationTargetId(targetId)).thenReturn(Optional.of(profile));
        RuntimeCredentialResolver resolver = new RuntimeCredentialResolver(profiles, mapper, name -> "top-secret-token");

        RuntimeCredentialResolver.CredentialReadiness readiness = resolver.readiness(companyId, targetId);

        assertTrue(readiness.profileConfigured());
        assertTrue(readiness.runtimeReady());
        assertFalse(readiness.status().contains("top-secret-token"));
    }

    @Test
    void deniesCrossTenantResolutionBeforeReadingEnvironment() {
        UUID companyA = UUID.randomUUID();
        UUID companyB = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        CredentialProfile profile = new CredentialProfile(companyB, targetId,
                CredentialProfile.CredentialType.API_TOKEN, "env:PORTAL_TOKEN");
        when(profiles.findByApplicationTargetId(targetId)).thenReturn(Optional.of(profile));
        RuntimeCredentialResolver resolver = new RuntimeCredentialResolver(profiles, mapper,
                name -> fail("environment must not be read for another tenant"));

        assertThrows(SecurityException.class, () -> resolver.resolve(companyA, targetId));
    }

    @Test
    void failsClosedWhenRuntimeSecretIsMissing() {
        UUID companyId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        CredentialProfile profile = new CredentialProfile(companyId, targetId,
                CredentialProfile.CredentialType.API_TOKEN, "env:PORTAL_TOKEN");
        when(profiles.findByApplicationTargetId(targetId)).thenReturn(Optional.of(profile));
        RuntimeCredentialResolver resolver = new RuntimeCredentialResolver(profiles, mapper, name -> null);

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> resolver.resolve(companyId, targetId));
        assertEquals("Runtime secret is not configured", error.getMessage());
    }
}
