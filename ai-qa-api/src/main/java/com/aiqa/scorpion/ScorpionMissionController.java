package com.aiqa.scorpion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/** REST entry point for Scorpion's one-click autonomous QA mission. */
@RestController
@RequestMapping("/api/scorpion/missions")
public class ScorpionMissionController {
    private final ScorpionMissionRepository missions;
    private final ScorpionMissionOrchestrator orchestrator;

    public ScorpionMissionController(ScorpionMissionRepository missions, ScorpionMissionOrchestrator orchestrator) {
        this.missions = missions;
        this.orchestrator = orchestrator;
    }

    public record StartMissionRequest(
            @NotBlank String title,
            @NotBlank String requirement,
            @NotBlank String uatUrl) {}

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ScorpionMission start(@Valid @RequestBody StartMissionRequest request) {
        ScorpionMission mission = missions.save(
                new ScorpionMission(request.title(), request.requirement(), request.uatUrl()));
        return orchestrator.run(mission.getId());
    }

    @GetMapping
    public List<ScorpionMission> list() { return missions.findAll(); }

    @GetMapping("/{id}")
    public ScorpionMission get(@PathVariable UUID id) {
        return missions.findById(id).orElseThrow(() -> new IllegalArgumentException("Scorpion mission not found"));
    }
}
