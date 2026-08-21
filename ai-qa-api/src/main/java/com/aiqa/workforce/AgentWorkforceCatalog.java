package com.aiqa.workforce;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Canonical catalog for the AI UAT Engineer virtual engineering organization.
 * One hundred specialists are available, but a mission activates only the smallest useful subset.
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
            a(50,"Self-UAT Agent","GOVERNANCE","validates AI UAT Engineer critical flows before stable release","self-uat","release","readiness"),
            a(51,"Story Decomposition Agent","REQUIREMENTS_ADVANCED","splits epics and stories into testable behavior","requirements","decomposition"),
            a(52,"Acceptance Criteria Auditor","REQUIREMENTS_ADVANCED","checks acceptance criteria completeness and consistency","requirements","acceptance"),
            a(53,"Business Rule Miner","REQUIREMENTS_ADVANCED","extracts hidden business rules from requirement context","requirements","business-rules"),
            a(54,"Dependency Mapper","REQUIREMENTS_ADVANCED","maps service and feature dependencies relevant to UAT","dependency","impact"),
            a(55,"Change Diff Summarizer","REQUIREMENTS_ADVANCED","summarizes meaningful requirement revisions","change","diff"),
            a(56,"Exploratory Test Planner","TEST_DESIGN_ADVANCED","designs exploratory charters around uncertain behavior","test-design","exploratory"),
            a(57,"State Transition Designer","TEST_DESIGN_ADVANCED","builds state-machine test paths","test-design","state"),
            a(58,"Pairwise Coverage Designer","TEST_DESIGN_ADVANCED","reduces combinatorial inputs with pairwise coverage","test-design","pairwise"),
            a(59,"Contract Test Designer","TEST_DESIGN_ADVANCED","creates service contract validation scenarios","test-design","contract","api"),
            a(60,"Migration Test Designer","TEST_DESIGN_ADVANCED","designs upgrade and migration validation","test-design","migration"),
            a(61,"Mobile Web Automation Engineer","AUTOMATION_ADVANCED","adapts browser flows for responsive/mobile web coverage","automation","mobile-web"),
            a(62,"API Contract Automation Engineer","AUTOMATION_ADVANCED","generates schema and contract assertions","automation","contract","api"),
            a(63,"Visual Regression Engineer","AUTOMATION_ADVANCED","plans bounded visual-diff verification","automation","visual"),
            a(64,"Reusable Component Engineer","AUTOMATION_ADVANCED","extracts reusable automation components","automation","reuse"),
            a(65,"Automation Optimization Agent","AUTOMATION_ADVANCED","reduces redundant automation while preserving coverage","automation","optimization"),
            a(66,"Cross-Browser Execution Engineer","EXECUTION_ADVANCED","plans controlled browser-matrix execution","execution","cross-browser"),
            a(67,"Retry Policy Agent","EXECUTION_ADVANCED","applies bounded retry rules to transient failures","execution","retry"),
            a(68,"Execution Scheduler","EXECUTION_ADVANCED","prioritizes execution order by risk and dependencies","execution","scheduling"),
            a(69,"Test Isolation Agent","EXECUTION_ADVANCED","detects shared-state risks between tests","execution","isolation"),
            a(70,"Runtime Cost Optimizer","EXECUTION_ADVANCED","limits unnecessary execution while retaining confidence","execution","cost"),
            a(71,"Log Correlation Analyst","DIAGNOSTICS_ADVANCED","correlates execution and application log evidence","diagnostics","logs"),
            a(72,"Network Failure Analyst","DIAGNOSTICS_ADVANCED","classifies network and dependency failures","diagnostics","network"),
            a(73,"Data Failure Analyst","DIAGNOSTICS_ADVANCED","isolates data-driven failures from product defects","diagnostics","data"),
            a(74,"Environment Failure Analyst","DIAGNOSTICS_ADVANCED","distinguishes environment instability from code defects","diagnostics","environment"),
            a(75,"Healing Verification Agent","DIAGNOSTICS_ADVANCED","verifies proposed healing did not hide product failures","healing","verification"),
            a(76,"Performance Trend Analyst","QUALITY_ADVANCED","compares latency and throughput trends across runs","performance","trend"),
            a(77,"Resource Pressure Analyst","QUALITY_ADVANCED","interprets memory and runtime pressure signals","performance","memory"),
            a(78,"Security Regression Agent","QUALITY_ADVANCED","tracks repeatable security-oriented checks across releases","security","regression"),
            a(79,"Accessibility Regression Agent","QUALITY_ADVANCED","tracks accessibility validation across releases","accessibility","regression"),
            a(80,"Release Confidence Scorer","QUALITY_ADVANCED","builds explainable confidence from quality evidence","release","confidence"),
            a(81,"Semantic Retrieval Agent","KNOWLEDGE_ADVANCED","retrieves semantically related tenant knowledge","rag","semantic"),
            a(82,"Knowledge Freshness Agent","KNOWLEDGE_ADVANCED","flags stale knowledge that may mislead decisions","knowledge","freshness"),
            a(83,"Evidence Indexing Agent","KNOWLEDGE_ADVANCED","indexes reusable evidence references safely","evidence","indexing"),
            a(84,"Run Similarity Agent","KNOWLEDGE_ADVANCED","finds comparable historical UAT runs within the tenant","history","similarity"),
            a(85,"Learning Guard Agent","KNOWLEDGE_ADVANCED","prevents unsafe or cross-tenant learning","learning","security"),
            a(86,"Slack Connector Agent","INTEGRATIONS_ADVANCED","publishes approved events to configured Slack endpoints","integration","slack"),
            a(87,"Teams Connector Agent","INTEGRATIONS_ADVANCED","publishes approved events to configured Teams endpoints","integration","teams"),
            a(88,"GitHub Release Agent","INTEGRATIONS_ADVANCED","links GitHub release metadata to UAT evidence","integration","github"),
            a(89,"GitLab Release Agent","INTEGRATIONS_ADVANCED","links GitLab release metadata to UAT evidence","integration","gitlab"),
            a(90,"Webhook Reliability Agent","INTEGRATIONS_ADVANCED","tracks webhook delivery success and retry posture","integration","webhook"),
            a(91,"Retention Policy Agent","GOVERNANCE_ADVANCED","evaluates evidence and audit retention rules","governance","retention"),
            a(92,"Data Residency Agent","GOVERNANCE_ADVANCED","checks configured data-location constraints","governance","residency"),
            a(93,"API Scope Auditor","GOVERNANCE_ADVANCED","reviews external client scopes against least privilege","governance","api","security"),
            a(94,"Credential Hygiene Agent","GOVERNANCE_ADVANCED","checks secret-handling posture without exposing values","security","credentials"),
            a(95,"Release Exception Agent","GOVERNANCE_ADVANCED","tracks explicit release exceptions and required approvals","governance","exception"),
            a(96,"Executive Quality Analyst","PLATFORM_INTELLIGENCE","summarizes release quality for leadership views","analytics","quality"),
            a(97,"Portfolio Risk Analyst","PLATFORM_INTELLIGENCE","summarizes risk across tenant products without leaking data","analytics","risk"),
            a(98,"Capacity Planning Agent","PLATFORM_INTELLIGENCE","analyzes workload and capacity signals","analytics","capacity"),
            a(99,"Agent Performance Auditor","PLATFORM_INTELLIGENCE","measures agent usefulness, failures and selection quality","agent","analytics"),
            a(100,"Workforce Director","PLATFORM_INTELLIGENCE","coordinates workforce capability evolution and mission composition","workforce","orchestration","analytics")
    );

    public List<AgentDefinition> all() { return agents; }

    public List<AgentDefinition> select(Set<String> requestedCapabilities, int maxAgents) {
        int boundedMax = Math.max(1, Math.min(maxAgents, 20));
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
