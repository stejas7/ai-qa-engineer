package com.aiqa.impact;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** HTTP boundary for V12 change-impact analysis. */
@RestController
@RequestMapping("/api/impact-analysis")
public class ImpactAnalysisController {
    private final ImpactAnalysisService service;

    public ImpactAnalysisController(ImpactAnalysisService service) {
        this.service = service;
    }

    /**
     * Analyse changed files before selecting a regression scope.
     *
     * @param request changed files and optional diff
     * @return explainable impact assessment
     */
    @PostMapping("/analyze")
    public ImpactAnalysisResponse analyze(@Valid @RequestBody ImpactAnalysisRequest request) {
        return service.analyze(request);
    }
}
