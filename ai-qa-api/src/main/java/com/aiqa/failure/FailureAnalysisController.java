package com.aiqa.failure;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/failure-analysis")
public class FailureAnalysisController {
    private final FailureAnalysisService service;
    public FailureAnalysisController(FailureAnalysisService service) { this.service = service; }
    @PostMapping("/analyze")
    public FailureAnalysisResponse analyze(@Valid @RequestBody FailureAnalysisRequest request) { return service.analyze(request); }
}
