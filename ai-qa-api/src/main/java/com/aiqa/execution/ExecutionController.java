package com.aiqa.execution;

import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/execution")
public class ExecutionController {
    private final ExecutionService service;
    private final ExecutionRecordRepository records;

    public ExecutionController(ExecutionService service, ExecutionRecordRepository records) {
        this.service = service;
        this.records = records;
    }

    @PostMapping("/run")
    public ExecutionResponse run(@Valid @RequestBody ExecutionRequest request) {
        return service.run(request);
    }

    @GetMapping("/history")
    public List<ExecutionRecord> history() {
        return records.findTop100ByOrderByExecutedAtDesc();
    }

    @GetMapping("/stats")
    public ExecutionStats stats() {
        long total = records.count();
        long passed = records.countByStatusIgnoreCase("PASS");
        long failed = records.countByStatusIgnoreCase("FAIL");
        double passRate = total == 0 ? 0 : Math.round((passed * 1000.0 / total)) / 10.0;
        return new ExecutionStats(total, passed, failed, passRate);
    }

    @GetMapping("/evidence/{file}")
    public ResponseEntity<Resource> evidence(@PathVariable String file) throws Exception {
        Path root = Path.of("evidence").toAbsolutePath().normalize();
        Path path = root.resolve(file).normalize();
        if (!path.startsWith(root)) return ResponseEntity.badRequest().build();
        Resource resource = new UrlResource(path.toUri());
        return resource.exists() ? ResponseEntity.ok().body(resource) : ResponseEntity.notFound().build();
    }

    public record ExecutionStats(long total, long passed, long failed, double passRate) {}
}
