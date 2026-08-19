package com.aiqa.testmanagement;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** M13 API for ISTQB-aligned planning, traceability, execution status and completion reporting. */
@RestController
@RequestMapping("/api/test-management")
public class TestManagementController {
    private final TestManagementService service;

    public TestManagementController(TestManagementService service) { this.service = service; }

    @PostMapping("/traceability")
    public TestTraceability create(@RequestBody TestManagementService.CreateTraceabilityRequest request) {
        return service.create(request);
    }

    @GetMapping("/traceability")
    public List<TestTraceability> list(@RequestParam UUID companyId, @RequestParam UUID productId) {
        return service.list(companyId, productId);
    }

    @PatchMapping("/traceability/{id}/execution")
    public TestTraceability recordExecution(@PathVariable UUID id,
                                            @RequestBody TestManagementService.ExecutionResultRequest request) {
        return service.recordExecution(id, request);
    }

    @GetMapping("/summary")
    public TestManagementService.TestCompletionSummary summary(@RequestParam UUID companyId,
                                                               @RequestParam UUID productId) {
        return service.summary(companyId, productId);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String,String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    ResponseEntity<Map<String,String>> conflict(IllegalStateException e) {
        return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
    }
}
