package com.aiqa.script;

import com.aiqa.automation.AutomationResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/** M12 managed automation-script API. */
@RestController
@RequestMapping("/api/automation-scripts")
public class AutomationScriptController {
    private final AutomationScriptService service;

    public AutomationScriptController(AutomationScriptService service) { this.service = service; }

    @PostMapping
    public AutomationScript create(@RequestBody AutomationScriptService.CreateScriptRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<AutomationScript> list(@RequestParam UUID companyId, @RequestParam UUID productId) {
        return service.list(companyId, productId);
    }

    @PatchMapping("/{id}/approve")
    public AutomationScript approve(@PathVariable UUID id) { return service.approve(id); }

    @PutMapping("/{id}")
    public AutomationScript revise(@PathVariable UUID id, @RequestBody AutomationScriptService.ReviseScriptRequest request) {
        return service.revise(id, request);
    }

    @PostMapping("/{id}/generate")
    public AutomationResponse generate(@PathVariable UUID id, @RequestBody(required = false) AutomationScriptService.GenerateScriptRequest request) {
        return service.generate(id, request);
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
