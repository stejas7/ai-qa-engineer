package com.aiqa.pipeline;

import com.aiqa.security.AppUser;
import com.aiqa.security.AppUserRepository;
import com.aiqa.security.UserRole;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** REST API for AI UAT Engineer requirement-to-UAT pipeline operations. */
@RestController
@RequestMapping("/api/pipeline")
public class PipelineController {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final String DEFAULT_URL = "https://example.com";

    private final RequirementFileExtractor extractor;
    private final FullPipelineService pipelineService;
    private final PipelineRunRepository pipelineRunRepository;
    private final PipelineExportService exportService;
    private final AppUserRepository users;

    public PipelineController(RequirementFileExtractor extractor,
                              FullPipelineService pipelineService,
                              PipelineRunRepository pipelineRunRepository,
                              PipelineExportService exportService,
                              AppUserRepository users) {
        this.extractor = extractor;
        this.pipelineService = pipelineService;
        this.pipelineRunRepository = pipelineRunRepository;
        this.exportService = exportService;
        this.users = users;
    }

    /**
     * Legacy direct pipeline launch. Company users must use /api/company/uat/upload so the
     * approved registered target and credential profile are always resolved server-side.
     */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> upload(Authentication authentication,
                                     @RequestParam("file") MultipartFile file,
                                     @RequestParam(value = "company", required = false) String company,
                                     @RequestParam(value = "targetUrl", required = false) String targetUrl,
                                     @RequestParam(value = "executeAutomation", required = false) Boolean executeAutomation) {
        AppUser actor = requireActor(authentication);
        if (actor.getRole() != UserRole.PLATFORM_ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Use the registered-product UAT flow for company execution"));
        }
        if (file == null || file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "A business requirement file is required"));
        if (file.getSize() > MAX_FILE_SIZE) return ResponseEntity.badRequest().body(Map.of("error", "File exceeds the 10 MB limit"));

        String rawText;
        try {
            rawText = extractor.extract(file);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Could not read file: " + e.getMessage()));
        }
        if (rawText == null || rawText.isBlank()) return ResponseEntity.badRequest().body(Map.of("error", "No readable text found in the uploaded file"));

        String resolvedCompany = (company == null || company.isBlank()) ? "default" : company.trim();
        boolean hasRealUrl = targetUrl != null && !targetUrl.isBlank();
        String resolvedUrl = hasRealUrl ? targetUrl.trim() : DEFAULT_URL;
        boolean willExecute = hasRealUrl && (executeAutomation == null || executeAutomation);
        String fallbackTitle = file.getOriginalFilename() == null ? "Uploaded requirement" : file.getOriginalFilename();

        PipelineRun run = pipelineRunRepository.save(new PipelineRun(resolvedCompany, fallbackTitle));
        pipelineService.runInBackground(run.getId(), rawText, fallbackTitle, resolvedUrl, willExecute);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of(
                "runId", run.getId(), "status", run.getStatus(), "company", resolvedCompany,
                "willExecuteAgainstUrl", willExecute));
    }

    @GetMapping("/runs")
    @Transactional(readOnly = true)
    public List<PipelineRunSummary> runs(Authentication authentication,
                                         @RequestParam(value = "company", required = false) String company) {
        AppUser actor = requireActor(authentication);
        List<PipelineRun> runs;
        if (actor.getRole() == UserRole.PLATFORM_ADMIN) {
            runs = (company == null || company.isBlank())
                    ? pipelineRunRepository.findAllByOrderByCreatedAtDesc()
                    : pipelineRunRepository.findByCompanyOrderByCreatedAtDesc(company.trim());
        } else {
            runs = pipelineRunRepository.findByCompanyOrderByCreatedAtDesc(actor.getCompanyId().toString());
        }
        return runs.stream().map(PipelineRunSummary::of).toList();
    }

    @GetMapping("/stats")
    @Transactional(readOnly = true)
    public PipelineStats stats(Authentication authentication,
                               @RequestParam(value = "company", required = false) String company) {
        AppUser actor = requireActor(authentication);
        List<PipelineRun> runs;
        if (actor.getRole() == UserRole.PLATFORM_ADMIN) {
            runs = (company == null || company.isBlank())
                    ? pipelineRunRepository.findAllByOrderByCreatedAtDesc()
                    : pipelineRunRepository.findByCompanyOrderByCreatedAtDesc(company.trim());
        } else {
            runs = pipelineRunRepository.findByCompanyOrderByCreatedAtDesc(actor.getCompanyId().toString());
        }
        long uploaded = runs.size();
        long completed = runs.stream().filter(r -> "COMPLETED".equalsIgnoreCase(r.getStatus())).count();
        long failed = runs.stream().filter(r -> "FAILED".equalsIgnoreCase(r.getStatus())).count();
        long processed = completed + failed;
        long processing = uploaded - processed;
        double completionRate = uploaded == 0 ? 0.0 : (processed * 100.0) / uploaded;
        return new PipelineStats(uploaded, processed, completed, failed, processing, Math.round(completionRate * 10.0) / 10.0);
    }

    @GetMapping("/runs/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<?> run(Authentication authentication, @PathVariable UUID id) {
        AppUser actor = requireActor(authentication);
        return pipelineRunRepository.findById(id)
                .map(r -> canAccess(actor, r)
                        ? ResponseEntity.ok(PipelineRunDetail.of(r))
                        : ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Cross-tenant UAT report access denied")))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Downloads the complete structured AI UAT Engineer result as JSON. */
    @GetMapping("/runs/{id}/test-cases.json")
    @Transactional(readOnly = true)
    public ResponseEntity<?> downloadJson(Authentication authentication, @PathVariable UUID id) {
        AppUser actor = requireActor(authentication);
        return pipelineRunRepository.findById(id).map(run -> {
            if (!canAccess(actor, run)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Cross-tenant UAT report access denied"));
            }
            if (!"COMPLETED".equalsIgnoreCase(run.getStatus()) || run.getResultJson() == null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Mission must be completed before export"));
            }
            byte[] bytes = run.getResultJson().getBytes(StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                            .filename(exportName(run, "json")).build().toString())
                    .body(bytes);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** Downloads generated test cases as an Excel workbook. */
    @GetMapping("/runs/{id}/test-cases.xlsx")
    @Transactional(readOnly = true)
    public ResponseEntity<?> downloadExcel(Authentication authentication, @PathVariable UUID id) {
        AppUser actor = requireActor(authentication);
        return pipelineRunRepository.findById(id).map(run -> {
            if (!canAccess(actor, run)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Cross-tenant UAT report access denied"));
            }
            if (!"COMPLETED".equalsIgnoreCase(run.getStatus()) || run.getResultJson() == null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Mission must be completed before export"));
            }
            byte[] bytes = exportService.toExcel(run.getResultJson());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                            .filename(exportName(run, "xlsx")).build().toString())
                    .body(bytes);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    private AppUser requireActor(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw new SecurityException("Authentication required");
        }
        return users.findByEmailIgnoreCase(authentication.getName())
                .filter(AppUser::isActive)
                .orElseThrow(() -> new SecurityException("Authenticated user not found"));
    }

    private boolean canAccess(AppUser actor, PipelineRun run) {
        return actor.getRole() == UserRole.PLATFORM_ADMIN
                || actor.getCompanyId().toString().equals(run.getCompany());
    }

    private String exportName(PipelineRun run, String extension) {
        String base = run.getFileName() == null ? "ai-uat-test-cases" : run.getFileName().replaceAll("[^a-zA-Z0-9._-]", "-");
        int dot = base.lastIndexOf('.');
        if (dot > 0) base = base.substring(0, dot);
        return base + "-test-cases." + extension;
    }

    public record PipelineStats(long uploaded, long processed, long completed, long failed, long processing, double completionRate) {}

    public record PipelineRunSummary(UUID id, String company, String fileName, String status,
                                     String currentStage, Object createdAt, Object completedAt) {
        static PipelineRunSummary of(PipelineRun r) {
            return new PipelineRunSummary(r.getId(), r.getCompany(), r.getFileName(), r.getStatus(), r.getCurrentStage(), r.getCreatedAt(), r.getCompletedAt());
        }
    }

    public record PipelineRunDetail(UUID id, String company, String fileName, String status,
                                    String currentStage, Object createdAt, Object completedAt,
                                    String errorMessage, String resultJson) {
        static PipelineRunDetail of(PipelineRun r) {
            return new PipelineRunDetail(r.getId(), r.getCompany(), r.getFileName(), r.getStatus(), r.getCurrentStage(),
                    r.getCreatedAt(), r.getCompletedAt(), r.getErrorMessage(), r.getResultJson());
        }
    }
}
