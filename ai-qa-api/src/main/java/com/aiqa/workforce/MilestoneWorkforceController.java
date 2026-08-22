package com.aiqa.workforce;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/workforce/milestones")
public class MilestoneWorkforceController {
    private final MilestoneWorkforceCoordinator coordinator;

    public MilestoneWorkforceController(MilestoneWorkforceCoordinator coordinator) { this.coordinator = coordinator; }

    @GetMapping("/plan")
    public MilestoneWorkforceCoordinator.WorkforcePlan plan(@RequestParam String milestone) { return coordinator.plan(milestone); }

    @GetMapping("/full-plan")
    public List<MilestoneWorkforceCoordinator.WorkforcePlan> fullPlan() { return coordinator.fullPlan(); }
}
