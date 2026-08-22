package com.aiqa.integration;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the M35 enterprise integration catalog.
 *
 * @author Tejas Shah
 */
class EnterpriseIntegrationCatalogTest {

    private final EnterpriseIntegrationCatalog catalog = new EnterpriseIntegrationCatalog();

    @Test
    void exposesFourEnterpriseProvidersWithoutSecrets() {
        var providers = catalog.providers();

        assertEquals(4, providers.size());
        assertTrue(providers.stream().anyMatch(provider -> provider.key().equals("JIRA")));
        assertTrue(providers.stream().anyMatch(provider -> provider.key().equals("GITHUB")));
        assertTrue(providers.stream().anyMatch(provider -> provider.key().equals("SLACK")));
        assertTrue(providers.stream().anyMatch(provider -> provider.key().equals("MICROSOFT_TEAMS")));
    }

    @Test
    void resolvesProviderKeysCaseInsensitively() {
        assertEquals("GitHub", catalog.requireProvider("github").displayName());
    }

    @Test
    void rejectsUnknownProviders() {
        assertThrows(IllegalArgumentException.class, () -> catalog.requireProvider("unknown"));
    }
}
