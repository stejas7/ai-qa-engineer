package com.aiqa.performance;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** API for bounded M11 performance validation. */
@RestController
@RequestMapping("/api/performance")
public class LoadTestController {
    private final LoadTestService service;

    public LoadTestController(LoadTestService service) { this.service = service; }

    @PostMapping("/load-test")
    public LoadTestService.LoadTestResult run(@RequestBody LoadTestService.LoadTestRequest request) {
        return service.run(request);
    }

    @ExceptionHandler({IllegalArgumentException.class})
    ResponseEntity<Map<String,String>> invalid(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }
}
