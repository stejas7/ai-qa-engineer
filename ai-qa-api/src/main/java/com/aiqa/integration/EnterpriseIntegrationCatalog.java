package com.aiqa.integration;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * Catalog of enterprise integration providers exposed by the AI UAT platform.
 *
 * <p>The catalog intentionally contains capability metadata only. Provider secrets,
 * access tokens and OAuth credentials are never exposed through this API.</p>
 *
 * @author Tejas Shah
 */
@Service
public class EnterpriseIntegrationCatalog {

    private static final List<ProviderDefinition> PROVIDERS = List.of(
            new ProviderDefinition(
                    "JIRA",
                    "Jira",
                    "Issue tracking",
                    "API_TOKEN",
                    List.of("CREATE_DEFECT", "LINK_UAT_RUN", "COMMENT_RELEASE_DECISION"),
                    List.of("UAT_FAILED", "RELEASE_BLOCKED", "RELEASE_READY")
            ),
            new ProviderDefinition(
                    "GITHUB",
                    "GitHub",
                    "Source control & delivery",
                    "OAUTH_OR_TOKEN",
                    List.of("CREATE_ISSUE", "COMMENT_PULL_REQUEST", "PUBLISH_CHECK_SUMMARY"),
                    List.of("UAT_COMPLETED", "UAT_FAILED", "RELEASE_READY", "RELEASE_BLOCKED")
            ),
            new ProviderDefinition(
                    "SLACK",
                    "Slack",
                    "Team notifications",
                    "WEBHOOK_OR_OAUTH",
                    List.of("POST_CHANNEL_MESSAGE", "POST_RELEASE_SUMMARY"),
                    List.of("UAT_COMPLETED", "UAT_FAILED", "RELEASE_READY", "RELEASE_BLOCKED")
            ),
            new ProviderDefinition(
                    "MICROSOFT_TEAMS",
                    "Microsoft Teams",
                    "Team notifications",
                    "WEBHOOK_OR_OAUTH",
                    List.of("POST_CHANNEL_MESSAGE", "POST_RELEASE_SUMMARY"),
                    List.of("UAT_COMPLETED", "UAT_FAILED", "RELEASE_READY", "RELEASE_BLOCKED")
            )
    );

    /** Returns the immutable provider capability catalog. */
    public List<ProviderDefinition> providers() {
        return PROVIDERS;
    }

    /** Resolves one provider by stable provider key. */
    public ProviderDefinition requireProvider(String providerKey) {
        if (providerKey == null || providerKey.isBlank()) {
            throw new IllegalArgumentException("provider key is required");
        }
        String normalized = providerKey.trim().toUpperCase(Locale.ROOT);
        return PROVIDERS.stream()
                .filter(provider -> provider.key().equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported enterprise integration provider"));
    }

    /** Safe provider metadata used by UI and orchestration layers. */
    public record ProviderDefinition(
            String key,
            String displayName,
            String category,
            String authenticationMode,
            List<String> supportedActions,
            List<String> supportedEvents
    ) {
    }
}
