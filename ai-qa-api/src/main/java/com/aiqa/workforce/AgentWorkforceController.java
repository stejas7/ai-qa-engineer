package com.aiqa.workforce;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Read-only APIs for the 50-agent virtual engineering organization. */
@RestController
@RequestMapping("/api/agent-workforce")
public class AgentWorkforceController {
    private final AgentWorkforceCatalog catalog;

    public AgentWorkforceController(AgentWorkforceCatalog catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/catalog")
    public WorkforceView catalog() {
        List<AgentWorkforceCatalog.AgentDefinition> agents = catalog.all();
        Map<String, Long> teams = agents.stream().collect(Collectors.groupingBy(
                AgentWorkforceCatalog.AgentDefinition::team, LinkedHashMap::new, Collectors.counting()));
        return new WorkforceView(agents.size(), teams, agents);
    }

    @GetMapping("/plan")
    public MissionTeamView plan(@RequestParam(defaultValue = "requirements,risk,regression,execution,release") String capabilities,
                                @RequestParam(defaultValue = "10") int maxAgents) {
        Set<String> requested = Arrays.stream(capabilities.split(","))
                .map(String::trim)
                .filter(v -> !v.isBlank())
                .collect(Collectors.toSet());
        List<AgentWorkforceCatalog.AgentDefinition> selected = catalog.select(requested, maxAgents);
        return new MissionTeamView(catalog.all().size(), selected.size(), requested, selected,
                "Only mission-relevant specialists are activated; the 50-agent catalog is the available virtual organization, not a 50-agent-per-run fan-out.");
    }

    public record WorkforceView(int totalAgents, Map<String, Long> teams,
                                List<AgentWorkforceCatalog.AgentDefinition> agents) {}
    public record MissionTeamView(int availableAgents, int selectedAgents, Set<String> requestedCapabilities,
                                  List<AgentWorkforceCatalog.AgentDefinition> team, String policy) {}
}
