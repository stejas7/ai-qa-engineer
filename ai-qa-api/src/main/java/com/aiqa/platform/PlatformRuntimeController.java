package com.aiqa.platform;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Runtime APIs for M39-M50. */
@RestController
@RequestMapping("/api/platform/runtime")
public class PlatformRuntimeController {
    private final GovernanceEngine governance;
    private final ReliabilityEngine reliability;
    private final AutonomousReleaseEngine autonomy;

    public PlatformRuntimeController(GovernanceEngine governance, ReliabilityEngine reliability, AutonomousReleaseEngine autonomy) {
        this.governance = governance; this.reliability = reliability; this.autonomy = autonomy;
    }

    @PostMapping("/policy/evaluate") public GovernanceEngine.PolicyDecision evaluate(@RequestBody GovernanceEngine.PolicyInput input){return governance.evaluate(input);}
    @PostMapping("/policy/{decisionId}/approval") public GovernanceEngine.ApprovalDecision approve(@PathVariable String decisionId,@RequestParam String tenantId,@RequestParam String actor,@RequestParam boolean approved,@RequestParam String reason){return governance.approve(tenantId,decisionId,actor,approved,reason);}
    @GetMapping("/audit") public List<GovernanceEngine.AuditRecord> audit(@RequestParam String tenantId){return governance.evidence(tenantId);}
    @GetMapping("/compliance") public GovernanceEngine.ComplianceBundle compliance(@RequestParam String tenantId){return governance.export(tenantId);}

    @PostMapping("/quota/acquire") public ReliabilityEngine.QuotaDecision acquire(@RequestParam String tenantId,@RequestParam(defaultValue="8") int maxConcurrent){return reliability.acquire(tenantId,maxConcurrent);}
    @PostMapping("/quota/release") public ReliabilityEngine.QuotaDecision release(@RequestParam String tenantId,@RequestParam(defaultValue="8") int maxConcurrent){return reliability.release(tenantId,maxConcurrent);}
    @PostMapping("/checkpoint") public ReliabilityEngine.Checkpoint checkpoint(@RequestParam String tenantId,@RequestParam String missionId,@RequestParam String stage,@RequestParam(required=false) String payloadRef){return reliability.checkpoint(tenantId,missionId,stage,payloadRef);}
    @GetMapping("/checkpoint") public ReliabilityEngine.Checkpoint resume(@RequestParam String tenantId,@RequestParam String missionId){return reliability.resume(tenantId,missionId);}
    @PostMapping("/observe") public ReliabilityEngine.MetricWindow observe(@RequestParam String tenantId,@RequestParam long latencyMs,@RequestParam boolean error){return reliability.observe(tenantId,latencyMs,error);}
    @GetMapping("/slo") public ReliabilityEngine.SloDecision slo(@RequestParam String tenantId,@RequestParam(defaultValue="0.05") double maxErrorRate,@RequestParam(defaultValue="2500") long maxAvgLatencyMs){return reliability.slo(tenantId,maxErrorRate,maxAvgLatencyMs);}

    @PostMapping("/release/assess") public AutonomousReleaseEngine.ReleaseAssessment assess(@RequestBody AutonomousReleaseEngine.ReleaseInput input){return autonomy.assess(input);}
    @PostMapping("/release/consensus") public AutonomousReleaseEngine.ConsensusResult consensus(@RequestBody List<AutonomousReleaseEngine.AgentVote> votes){return autonomy.consensus(votes);}
    @PostMapping("/self-uat") public AutonomousReleaseEngine.SelfUatResult selfUat(@RequestBody AutonomousReleaseEngine.SelfUatInput input){return autonomy.selfUat(input);}

    public record GateRequest(AutonomousReleaseEngine.ReleaseInput release,List<AutonomousReleaseEngine.AgentVote> votes,AutonomousReleaseEngine.SelfUatInput selfUat,GovernanceEngine.PolicyDecision policy,ReliabilityEngine.SloDecision slo){}
    @PostMapping("/release/gate") public AutonomousReleaseEngine.GateDecision gate(@RequestBody GateRequest request){return autonomy.gate(request.release(),request.votes(),request.selfUat(),request.policy(),request.slo());}
}
