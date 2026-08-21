package com.aiqa.workforce;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Canonical catalog for the AI UAT Engineer virtual engineering organization.
 * Fifty specialists are available, but a mission should activate only the smallest useful subset.
 *
 * @author Tejas Shah
 */
@Component
public class AgentWorkforceCatalog {

    private final List<AgentDefinition> agents = List.of(
            a(1,"Chief UAT Orchestrator","ORCHESTRATION","plans mission teams and keeps policy boundaries","orchestration","planning","release"),
            a(2,"Mission Planner","ORCHESTRATION","turns objectives into bounded work plans","planning","requirements"),
            a(3,"Policy Guard","ORCHESTRATION","checks role, tenant and execution policy before actions","security","governance"),
            a(4,"Evidence Coordinator","ORCHESTRATION","ensures every important action produces traceable evidence","evidence","audit"),
            a(5,"Release Coordinator","ORCHESTRATION","assembles final release readiness inputs","release","approval"),

            a(6,"Requirement Analyst","REQUIREMENTS","extracts business intent and acceptance criteria","requirements","analysis"),
            a(7,"Ambiguity Analyst","REQUIREMENTS","flags unclear or conflicting requirement language","requirements","risk"),
            a(8,"Change Impact Analyst","REQUIREMENTS","identifies changed behavior and impacted areas","change","impact","regression"),
            a(9,"Risk Analyst","REQUIREMENTS","scores business and technical testing risk","risk","prioritization"),
            a(10,"Domain Context Analyst","REQUIREMENTS","grounds reasoning with product/domain knowledge","rag","knowledge","requirements"),

            a(11,"Functional Test Designer","TEST_DESIGN","designs positive business-path coverage","test-design","functional"),
            a(12,"Negative Test Designer","TEST_DESIGN","designs invalid and failure-path coverage","test-design","negative"),
            a(13,"Boundary Test Designer","TEST_DESIGN","designs limits and edge-condition coverage","test-design","boundary"),
            a(14,"Data Test Designer","TEST_DESIGN","designs data-state and validation scenarios","test-design","data"),
            a(15,"Regression Pack Designer","TEST_DESIGN","builds focused regression packs from impact and history","regression","prioritization"),

            a(16,"UI Automation Engineer","AUTOMATION","generates deterministic browser automation","automation","ui","playwright"),
            a(17,"API Automation Engineer","AUTOMATION","generates deterministic API validation flows","automation","api"),
            a(18,"Locator Engineer","AUTOMATION","creates resilient bounded locator strategies","automation","locator"),
            a(19,"Test Data Engineer","AUTOMATION","prepares safe deterministic test data strategies","automation","data"),
            a(20,"Automation Reviewer","AUTOMATION","reviews generated automation for safety and maintainability","automation","review"),

            a(21,"Browser Execution Engineer","EXECUTION","runs supported UI tests and captures evidence","execution","ui"),
            a(22,"API Execution Engineer","EXECUTION","runs API checks and captures response evidence","execution","api"),
            a(23,"Session State Engineer","EXECUTION","manages authenticated execution state safely","execution","auth"),
            a(24,"Parallel Execution Planner","EXECUTION","selects safe parallelization under tenant limits","execution","parallel"),
            a(25,"Environment Readiness Engineer","EXECUTION","checks target availability and prerequisites","execution","environment"),

            a(26,"Failure Classifier","DIAGNOSTICS","classifies product, automation, environment and data failures","failure","diagnostics"),
            a(27,"Root Cause Analyst","DIAGNOSTICS","explains likely failure causes from evidence","failure","root-cause"),
            a(28,"Flaky Test Analyst","DIAGNOSTICS","detects instability from repeated execution history","flaky","history"),
            a(29,"Healing Advisor","DIAGNOSTICS","proposes bounded repairs only for recoverable automation failures","healing","automation"),
            a(30,"Evidence Analyst","DIAGNOSTICS","summarizes screenshots, logs and execution traces","evidence","diagnostics"),

            a(31,"Performance Test Engineer","QUALITY","designs deterministic load and latency checks","performance","load"),
            a(32,"SLO Analyst","QUALITY","compares measured performance with configured thresholds","performance","slo"),
            a(33,"Security Test Analyst","QUALITY","checks supported application security controls and exposure risks","security","quality"),
            a(34,"Accessibility Analyst","QUALITY","reviews accessibility-oriented validation coverage","accessibility","quality"),
            a(35,"Quality Gate Analyst","QUALITY","combines evidence into READY/BLOCKED quality recommendation","quality-gate","release"),

            a(36,"RAG Retrieval Agent","KNOWLEDGE","retrieves tenant/product knowledge for grounded reasoning","rag","retrieval"),
            a(37,"Knowledge Curator","KNOWLEDGE","identifies reusable validated product knowledge","knowledge","curation"),
            a(38,"Traceability Agent","KNOWLEDGE","links requirements, tests, executions, defects and evidence","traceability","evidence"),
            a(39,"Historical Learning Agent","KNOWLEDGE","summarizes useful patterns from prior tenant runs","history","learning"),
            a(40,"Context Quality Reviewer","KNOWLEDGE","checks whether retrieved context is relevant and safe","rag","review"),

            a(41,"Defect Triage Agent","INTEGRATIONS","prepares defect-ready summaries from failed evidence","defect","integration"),
            a(42,"Jira/Azure DevOps Connector Agent","INTEGRATIONS","maps validated defects to external work trackers","integration","defect"),
            a(43,"SCM Release Agent","INTEGRATIONS","links source/build metadata to UAT evidence","integration","scm","release"),
            a(44,"Notification Agent","INTEGRATIONS","publishes approved status events to configured channels","integration","notification"),
            a(45,"CI Trigger Agent","INTEGRATIONS","accepts authorized external UAT trigger context","integration","ci"),

            a(46,"Approval Policy Agent","GOVERNANCE","evaluates release approval policy requirements","governance","approval"),
            a(47,"Separation of Duties Agent","GOVERNANCE","checks conflicting actor/approval responsibilities","governance","security"),
            a(48,"Audit Compliance Agent","GOVERNANCE","prepares audit-safe operational trace","audit","governance"),
            a(49,"Enterprise Readiness Agent","GOVERNANCE","evaluates platform readiness and operational posture","readiness","governance","slo"),
            a(50,"Self-UAT Agent","GOVERNANCE","validates AI UAT Engineer critical flows before stable release","self-uat","release","readiness")
    );

    public List<AgentDefinition> all() { return agents; }

    public List<AgentDefinition> select(Set<String> requestedCapabilities, int maxAgents) {
        int boundedMax = Math.max(1, Math.min(maxAgents, 15));
        if (requestedCapabilities == null || requestedCapabilities.isEmpty()) {
            return agents.stream().filter(a -> a.id() <= 5).limit(boundedMax).toList();
        }
        Set<String> normalized = requestedCapabilities.stream()
                .filter(v -> v != null && !v.isBlank())
                .map(v -> v.trim().toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());
        List<AgentDefinition> matched = agents.stream()
                .filter(a -> a.capabilities().stream().anyMatch(normalized::contains))
                .limit(boundedMax)
                .toList();
        if (!matched.isEmpty()) return matched;
        return agents.stream().filter(a -> a.id() <= 5).limit(boundedMax).toList();
    }

    private static AgentDefinition a(int id, String name, String team, String purpose, String... capabilities) {
        return new AgentDefinition(id, name, team, purpose, List.of(capabilities));
    }

    public record AgentDefinition(int id, String name, String team, String purpose, List<String> capabilities) {}
}
