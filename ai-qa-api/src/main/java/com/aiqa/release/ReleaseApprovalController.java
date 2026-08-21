package com.aiqa.release;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/release-approvals")
public class ReleaseApprovalController {
    private final ReleaseApprovalService approvals;

    public ReleaseApprovalController(ReleaseApprovalService approvals) {
        this.approvals = approvals;
    }

    @GetMapping
    public List<ApprovalView> list(Authentication authentication) {
        return approvals.list(authentication.getName()).stream().map(ApprovalView::from).toList();
    }

    @PostMapping
    public ApprovalView request(Authentication authentication, @RequestBody CreateApprovalRequest request) {
        if (request == null) throw new IllegalArgumentException("release approval request is required");
        return ApprovalView.from(approvals.request(authentication.getName(), request.runId(), request.note()));
    }

    @PatchMapping("/{id}/decision")
    public ApprovalView decide(Authentication authentication, @PathVariable UUID id, @RequestBody DecisionRequest request) {
        if (request == null) throw new IllegalArgumentException("decision request is required");
        return ApprovalView.from(approvals.decide(authentication.getName(), id, request.decision(), request.note()));
    }

    public record CreateApprovalRequest(UUID runId, String note) {}
    public record DecisionRequest(String decision, String note) {}
    public record ApprovalView(UUID id, UUID companyId, UUID runId, String requestedBy, String decidedBy,
                               String decision, String note, String createdAt, String decidedAt) {
        static ApprovalView from(ReleaseApproval approval) {
            return new ApprovalView(approval.getId(), approval.getCompanyId(), approval.getRunId(), approval.getRequestedBy(),
                    approval.getDecidedBy(), approval.getDecision().name(), approval.getNote(), approval.getCreatedAt().toString(),
                    approval.getDecidedAt() == null ? null : approval.getDecidedAt().toString());
        }
    }
}
