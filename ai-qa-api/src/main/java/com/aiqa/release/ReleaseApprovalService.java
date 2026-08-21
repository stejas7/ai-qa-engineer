package com.aiqa.release;

import com.aiqa.pipeline.PipelineRun;
import com.aiqa.pipeline.PipelineRunRepository;
import com.aiqa.security.AppUser;
import com.aiqa.security.AppUserRepository;
import com.aiqa.security.UserRole;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Human release-approval workflow that keeps tenant boundaries server-side. */
@Service
public class ReleaseApprovalService {
    private final ReleaseApprovalRepository approvals;
    private final PipelineRunRepository runs;
    private final AppUserRepository users;

    public ReleaseApprovalService(ReleaseApprovalRepository approvals,
                                  PipelineRunRepository runs,
                                  AppUserRepository users) {
        this.approvals = approvals;
        this.runs = runs;
        this.users = users;
    }

    public List<ReleaseApproval> list(String actorEmail) {
        AppUser actor = requireUser(actorEmail);
        return approvals.findByCompanyIdOrderByCreatedAtDesc(actor.getCompanyId());
    }

    public ReleaseApproval request(String actorEmail, UUID runId, String note) {
        AppUser actor = requireReleaseRole(actorEmail);
        PipelineRun run = requireTenantRun(actor, runId);
        if (!"COMPLETED".equalsIgnoreCase(run.getStatus())) throw new IllegalStateException("Only completed UAT runs can enter release approval");
        if (approvals.existsByRunId(runId)) throw new IllegalStateException("Release approval already exists for this UAT run");
        return approvals.save(new ReleaseApproval(actor.getCompanyId(), runId, actor.getEmail(), cleanNote(note)));
    }

    public ReleaseApproval decide(String actorEmail, UUID approvalId, String rawDecision, String note) {
        AppUser actor = requireReleaseRole(actorEmail);
        ReleaseApproval approval = approvals.findById(approvalId)
                .orElseThrow(() -> new IllegalArgumentException("Release approval not found"));
        if (!actor.getCompanyId().equals(approval.getCompanyId())) throw new SecurityException("Cross-tenant release approval access denied");
        ReleaseApproval.Decision decision;
        try { decision = ReleaseApproval.Decision.valueOf(rawDecision == null ? "" : rawDecision.trim().toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("decision must be APPROVED or BLOCKED"); }
        approval.decide(decision, actor.getEmail(), cleanNote(note));
        return approvals.save(approval);
    }

    private PipelineRun requireTenantRun(AppUser actor, UUID runId) {
        if (runId == null) throw new IllegalArgumentException("runId is required");
        PipelineRun run = runs.findById(runId).orElseThrow(() -> new IllegalArgumentException("UAT run not found"));
        if (!actor.getCompanyId().toString().equalsIgnoreCase(run.getCompany())) {
            throw new SecurityException("Cross-tenant UAT run access denied");
        }
        return run;
    }

    private AppUser requireReleaseRole(String email) {
        AppUser actor = requireUser(email);
        if (actor.getRole() != UserRole.COMPANY_ADMIN && actor.getRole() != UserRole.QA_MANAGER) {
            throw new SecurityException("Company Admin or QA Manager role required");
        }
        return actor;
    }

    private AppUser requireUser(String email) {
        if (email == null || email.isBlank()) throw new SecurityException("Authentication required");
        AppUser actor = users.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new SecurityException("Authenticated user not found"));
        if (!actor.isActive()) throw new SecurityException("User is inactive");
        return actor;
    }

    private String cleanNote(String note) {
        if (note == null) return null;
        String value = note.trim();
        return value.length() <= 1200 ? value : value.substring(0, 1200);
    }
}
