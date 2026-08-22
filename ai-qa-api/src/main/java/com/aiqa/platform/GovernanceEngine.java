package com.aiqa.platform;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** M39-M42 governance runtime: policy evaluation, approvals, audit evidence and compliance export. */
@Service
public class GovernanceEngine {
    private final List<AuditRecord> audit = Collections.synchronizedList(new ArrayList<>());

    public PolicyDecision evaluate(PolicyInput input) {
        if (input == null) throw new IllegalArgumentException("policy input is required");
        double risk = clamp(input.riskScore());
        boolean approvalRequired = risk >= 0.65 || input.criticalChange() || input.productionTarget();
        boolean allowed = risk < 0.90 && !input.securityBlocker();
        String reason = !allowed ? "BLOCKED_BY_POLICY" : approvalRequired ? "HUMAN_APPROVAL_REQUIRED" : "POLICY_PASS";
        String decisionId = UUID.randomUUID().toString();
        append(input.tenantId(), decisionId, "POLICY_EVALUATED", reason, input.actor());
        return new PolicyDecision(decisionId, allowed, approvalRequired, risk, reason);
    }

    public ApprovalDecision approve(String tenantId, String decisionId, String actor, boolean approved, String reason) {
        require(tenantId, "tenantId"); require(decisionId, "decisionId"); require(actor, "actor"); require(reason, "reason");
        append(tenantId, decisionId, approved ? "APPROVED" : "REJECTED", reason.trim(), actor.trim());
        return new ApprovalDecision(decisionId, approved, reason.trim(), actor.trim(), Instant.now().toString());
    }

    public List<AuditRecord> evidence(String tenantId) {
        require(tenantId, "tenantId");
        synchronized (audit) { return audit.stream().filter(r -> r.tenantId().equals(tenantId)).toList(); }
    }

    public ComplianceBundle export(String tenantId) {
        List<AuditRecord> records = evidence(tenantId);
        return new ComplianceBundle(tenantId, Instant.now().toString(), records.size(), records, "PII_MINIMIZED");
    }

    private void append(String tenantId, String correlationId, String action, String detail, String actor) {
        audit.add(new AuditRecord(UUID.randomUUID().toString(), require(tenantId, "tenantId"), correlationId, action, detail, actor == null ? "system" : actor, Instant.now().toString()));
    }
    private String require(String v, String f) { if (v == null || v.isBlank()) throw new IllegalArgumentException(f + " is required"); return v.trim(); }
    private double clamp(double v) { return Math.max(0, Math.min(1, v)); }

    public record PolicyInput(String tenantId, String actor, double riskScore, boolean criticalChange, boolean productionTarget, boolean securityBlocker) {}
    public record PolicyDecision(String decisionId, boolean allowed, boolean approvalRequired, double riskScore, String reason) {}
    public record ApprovalDecision(String decisionId, boolean approved, String reason, String actor, String decidedAt) {}
    public record AuditRecord(String id, String tenantId, String correlationId, String action, String detail, String actor, String occurredAt) {}
    public record ComplianceBundle(String tenantId, String generatedAt, int evidenceCount, List<AuditRecord> evidence, String redactionPolicy) {}
}
