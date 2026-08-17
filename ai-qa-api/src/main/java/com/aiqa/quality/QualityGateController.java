package com.aiqa.quality;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quality-gate")
public class QualityGateController {
    private final QualityGateService service;

    public QualityGateController(QualityGateService service) { this.service = service; }

    @PostMapping("/evaluate")
    public QualityGateResponse evaluate(@RequestBody QualityGateRequest request) {
        return service.evaluate(request);
    }
}
