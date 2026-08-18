package com.aiqa.pipeline;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The single entry point a company's QA team actually needs day to day: upload a business
 * requirement file, get a run id back, watch the dashboard.
 *
 * <p>Everything downstream (requirement understanding, test design, automation generation,
 * real UAT execution and the quality gate decision) happens automatically in
 * {@link FullPipelineService} - no manual chaining of the individual APIs is required.</p>
 */
@RestController
@RequestMapping("/api/pipeline")
public class PipelineController {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024; // 10 MB
    private static final String DEFAULT_URL = "https://example.com";

    private final RequirementFileExtractor extractor;
    private final FullPipelineService pipelineService;
    private final PipelineRunRepository pipelineRunRepository;

    public PipelineController(RequirementFileExtractor extractor,
                               FullPipelineService pipelineService,
                               PipelineRunRepository pipelineRunRepository) {
        this.extractor = extractor;
        this.pipelineService = pipelineService;
        this.pipelineRunRepository = pipelineRunRepository;
    }

    /**
     * Uploads a business requirement file ({@code .txt}, {@code .md}, {@code .docx} or
     * {@code .pdf}) and kicks off the full requirement-to-quality-gate pipeline in the background.
     *
     * @param file            the business requirement document
     * @param company         optional company / tenant label, shown on the dashboard (each
     *                        company can tag its own runs; defaults to "default")
     * @param targetUrl       optional application URL to execute the generated UAT against; if
     *                        omitted, automation code is still generated but not executed
     * @param executeAutomation whether to run the generated automation for real (default true
     *                        when a target URL is supplied)
     */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
                                     @RequestParam(value = "company", required = false) String company,
                                     @RequestParam(value = "targetUrl", required = false) String targetUrl,
                                     @RequestParam(value = "executeAutomation", required = false) Boolean executeAutomation) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "A business requirement file is required"));
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            return ResponseEntity.badRequest().body(Map.of("error", "File exceeds the 10 MB limit"));
        }

        String rawText;
        try {
            rawText = extractor.extract(file);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Could not read file: " + e.getMessage()));
        }
        if (rawText == null || rawText.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No readable text found in the uploaded file"));
        }

        String resolvedCompany = (company == null || company.isBlank()) ? "default" : company.trim();
        boolean hasRealUrl = targetUrl != null && !targetUrl.isBlank();
        String resolvedUrl = hasRealUrl ? targetUrl.trim() : DEFAULT_URL;
        boolean willExecute = hasRealUrl && (executeAutomation == null || executeAutomation);

        String fallbackTitle = file.getOriginalFilename() == null ? "Uploaded requirement" : file.getOriginalFilename();
        PipelineRun run = new PipelineRun(resolvedCompany, fallbackTitle);
        run = pipelineRunRepository.save(run);

        pipelineService.runInBackground(run.getId(), rawText, fallbackTitle, resolvedUrl, willExecute);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "runId", run.getId(),
                "status", run.getStatus(),
                "company", resolvedCompany,
                "willExecuteAgainstUrl", willExecute
        ));
    }

    @GetMapping("/runs")
    public List<PipelineRunSummary> runs(@RequestParam(value = "company", required = false) String company) {
        List<PipelineRun> runs = (company == null || company.isBlank())
                ? pipelineRunRepository.findAllByOrderByCreatedAtDesc()
                : pipelineRunRepository.findByCompanyOrderByCreatedAtDesc(company.trim());
        return runs.stream().map(PipelineRunSummary::of).toList();
    }

    /** Returns operational counters used by the Auravis dashboard. */
    @GetMapping("/stats")
    public PipelineStats stats(@RequestParam(value = "company", required = false) String company) {
        List<PipelineRun> runs = (company == null || company.isBlank())
                ? pipelineRunRepository.findAllByOrderByCreatedAtDesc()
                : pipelineRunRepository.findByCompanyOrderByCreatedAtDesc(company.trim());

        long uploaded = runs.size();
        long completed = runs.stream().filter(r -> "COMPLETED".equalsIgnoreCase(r.getStatus())).count();
        long failed = runs.stream().filter(r -> "FAILED".equalsIgnoreCase(r.getStatus())).count();
        long processed = completed + failed;
        long processing = uploaded - processed;
        double completionRate = uploaded == 0 ? 0.0 : (processed * 100.0) / uploaded;

        return new PipelineStats(uploaded, processed, completed, failed, processing,
                Math.round(completionRate * 10.0) / 10.0);
    }

    @GetMapping("/runs/{id}")
    public ResponseEntity<?> run(@PathVariable UUID id) {
        return pipelineRunRepository.findById(id)
                .<ResponseEntity<?>>map(r -> ResponseEntity.ok(PipelineRunDetail.of(r)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record PipelineStats(long uploaded, long processed, long completed, long failed,
                                long processing, double completionRate) {}

    public record PipelineRunSummary(UUID id, String company, String fileName, String status,
                                      String currentStage, Object createdAt, Object completedAt) {
        static PipelineRunSummary of(PipelineRun r) {
            return new PipelineRunSummary(r.getId(), r.getCompany(), r.getFileName(), r.getStatus(),
                    r.getCurrentStage(), r.getCreatedAt(), r.getCompletedAt());
        }
    }

    public record PipelineRunDetail(UUID id, String company, String fileName, String status,
                                     String currentStage, Object createdAt, Object completedAt,
                                     String errorMessage, String resultJson) {
        static PipelineRunDetail of(PipelineRun r) {
            return new PipelineRunDetail(r.getId(), r.getCompany(), r.getFileName(), r.getStatus(),
                    r.getCurrentStage(), r.getCreatedAt(), r.getCompletedAt(), r.getErrorMessage(), r.getResultJson());
        }
    }
}
