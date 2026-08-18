package com.aiqa.scorpion;

import com.aiqa.pipeline.RequirementFileExtractor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** REST entry point for Scorpion's one-click autonomous QA mission. */
@RestController
@RequestMapping("/api/scorpion/missions")
public class ScorpionMissionController {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB

    private final ScorpionMissionRepository missions;
    private final ScorpionMissionOrchestrator orchestrator;
    private final RequirementFileExtractor fileExtractor;

    public ScorpionMissionController(ScorpionMissionRepository missions,
                                      ScorpionMissionOrchestrator orchestrator,
                                      RequirementFileExtractor fileExtractor) {
        this.missions = missions;
        this.orchestrator = orchestrator;
        this.fileExtractor = fileExtractor;
    }

    public record StartMissionRequest(
            @NotBlank String title,
            @NotBlank String requirement,
            @NotBlank String uatUrl) {}

    /** Starts a mission from a pasted requirement. */
    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ScorpionMission start(@Valid @RequestBody StartMissionRequest request) {
        ScorpionMission mission = missions.save(
                new ScorpionMission(request.title(), request.requirement(), request.uatUrl()));
        return orchestrator.run(mission.getId());
    }

    /**
     * Starts a mission from an uploaded business requirement file ({@code .txt}, {@code .md},
     * {@code .docx} or {@code .pdf}) instead of pasted text - this is what the Scorpion UI's
     * "Upload or paste" entry point actually calls.
     */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> startFromFile(@RequestParam("file") MultipartFile file,
                                            @RequestParam("title") String title,
                                            @RequestParam("uatUrl") String uatUrl) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "A business requirement file is required"));
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body(Map.of("error", "File exceeds the 10 MB limit"));
        }
        if (title == null || title.isBlank() || uatUrl == null || uatUrl.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "title and uatUrl are required"));
        }

        String requirementText;
        try {
            requirementText = fileExtractor.extract(file);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Could not read file: " + e.getMessage()));
        }
        if (requirementText == null || requirementText.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No readable text found in the uploaded file"));
        }

        ScorpionMission mission = missions.save(new ScorpionMission(title, requirementText, uatUrl));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(orchestrator.run(mission.getId()));
    }

    @GetMapping
    public List<ScorpionMission> list() { return missions.findAll(); }

    @GetMapping("/{id}")
    public ScorpionMission get(@PathVariable UUID id) {
        return missions.findById(id).orElseThrow(() -> new IllegalArgumentException("Scorpion mission not found"));
    }
}
