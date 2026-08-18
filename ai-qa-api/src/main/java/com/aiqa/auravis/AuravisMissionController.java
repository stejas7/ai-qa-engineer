package com.aiqa.auravis;

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

/** REST entry point for Auravis one-click autonomous QA mission. */
@RestController
@RequestMapping("/api/auravis/missions")
public class AuravisMissionController {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private final AuravisMissionRepository missions;
    private final AuravisMissionOrchestrator orchestrator;
    private final RequirementFileExtractor fileExtractor;

    public AuravisMissionController(AuravisMissionRepository missions,
                                    AuravisMissionOrchestrator orchestrator,
                                    RequirementFileExtractor fileExtractor) {
        this.missions = missions;
        this.orchestrator = orchestrator;
        this.fileExtractor = fileExtractor;
    }

    public record StartMissionRequest(@NotBlank String title,
                                      @NotBlank String requirement,
                                      @NotBlank String uatUrl) {}

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AuravisMission start(@Valid @RequestBody StartMissionRequest request) {
        AuravisMission mission = missions.save(new AuravisMission(request.title(), request.requirement(), request.uatUrl()));
        return orchestrator.run(mission.getId());
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> startFromFile(@RequestParam("file") MultipartFile file,
                                            @RequestParam("title") String title,
                                            @RequestParam("uatUrl") String uatUrl) {
        if (file == null || file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "A business requirement file is required"));
        if (file.getSize() > MAX_FILE_SIZE) return ResponseEntity.badRequest().body(Map.of("error", "File exceeds the 10 MB limit"));
        if (title == null || title.isBlank() || uatUrl == null || uatUrl.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "title and uatUrl are required"));

        String requirementText;
        try {
            requirementText = fileExtractor.extract(file);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Could not read file: " + e.getMessage()));
        }
        if (requirementText == null || requirementText.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "No readable text found in the uploaded file"));

        AuravisMission mission = missions.save(new AuravisMission(title, requirementText, uatUrl));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(orchestrator.run(mission.getId()));
    }

    @GetMapping
    public List<AuravisMission> list() { return missions.findAll(); }

    @GetMapping("/{id}")
    public AuravisMission get(@PathVariable UUID id) {
        return missions.findById(id).orElseThrow(() -> new IllegalArgumentException("Auravis mission not found"));
    }
}
