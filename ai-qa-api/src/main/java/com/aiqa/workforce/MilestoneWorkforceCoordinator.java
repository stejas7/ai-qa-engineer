package com.aiqa.workforce;

import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Composes bounded specialist teams from the canonical 100-agent workforce for M35-M50.
 * Teams remain small and purpose-specific; the platform never runs all 100 agents blindly.
 */
@Service
public class MilestoneWorkforceCoordinator {
    private final AgentWorkforceCatalog catalog;

    private static final Map<String, Set<String>> CAPABILITIES = Map.ofEntries(
            Map.entry("M35", Set.of("integration","github","slack","teams")),
            Map.entry("M36", Set.of("governance","security","integration")),
            Map.entry("M37", Set.of("integration","notification","release")),
            Map.entry("M38", Set.of("retry","webhook","diagnostics")),
            Map.entry("M39", Set.of("governance","security","approval")),
            Map.entry("M40", Set.of("audit","evidence","traceability")),
            Map.entry("M41", Set.of("approval","governance","exception")),
            Map.entry("M42", Set.of("retention","residency","audit")),
            Map.entry("M43", Set.of("capacity","parallel","scheduling")),
            Map.entry("M44", Set.of("healing","diagnostics","environment")),
            Map.entry("M45", Set.of("analytics","logs","agent")),
            Map.entry("M46", Set.of("slo","performance","readiness")),
            Map.entry("M47", Set.of("risk","impact","confidence")),
            Map.entry("M48", Set.of("review","quality-gate","release")),
            Map.entry("M49", Set.of("self-uat","test-design","execution")),
            Map.entry("M50", Set.of("release","approval","readiness","workforce"))
    );

    public MilestoneWorkforceCoordinator(AgentWorkforceCatalog catalog) { this.catalog = catalog; }

    public WorkforcePlan plan(String milestone) {
        String key = milestone == null ? "" : milestone.trim().toUpperCase();
        Set<String> requested = CAPABILITIES.get(key);
        if (requested == null) throw new IllegalArgumentException("Milestone must be M35 through M50");
        List<AgentWorkforceCatalog.AgentDefinition> specialists = catalog.select(requested, 8);
        LinkedHashSet<AgentWorkforceCatalog.AgentDefinition> team = new LinkedHashSet<>();
        team.addAll(catalog.select(Set.of("orchestration"), 1));
        team.addAll(specialists);
        return new WorkforcePlan(key, requested, team.stream().limit(10).toList(), "BOUNDED_SPECIALIST_TEAM");
    }

    public List<WorkforcePlan> fullPlan() {
        return CAPABILITIES.keySet().stream().sorted().map(this::plan).toList();
    }

    public record WorkforcePlan(String milestone, Set<String> requestedCapabilities,
                                List<AgentWorkforceCatalog.AgentDefinition> selectedAgents, String strategy) {}
}
