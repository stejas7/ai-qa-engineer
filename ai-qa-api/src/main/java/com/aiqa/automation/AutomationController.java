package com.aiqa.automation;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/automation")
public class AutomationController {
    private final PlaywrightAutomationService service;

    public AutomationController(PlaywrightAutomationService service) {
        this.service = service;
    }

    @PostMapping("/generate")
    public AutomationResponse generate(@Valid @RequestBody AutomationRequest request) {
        return service.generate(request);
    }
}
