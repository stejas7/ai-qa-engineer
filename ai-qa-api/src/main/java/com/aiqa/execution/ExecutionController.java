package com.aiqa.execution;

import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/execution")
public class ExecutionController {
    private final ExecutionService service;
    public ExecutionController(ExecutionService service) { this.service = service; }

    @PostMapping("/run")
    public ExecutionResponse run(@Valid @RequestBody ExecutionRequest request) { return service.run(request); }

    @GetMapping("/evidence/{file}")
    public ResponseEntity<Resource> evidence(@PathVariable String file) throws Exception {
        Path root = Path.of("evidence").toAbsolutePath().normalize();
        Path path = root.resolve(file).normalize();
        if (!path.startsWith(root)) return ResponseEntity.badRequest().build();
        Resource resource = new UrlResource(path.toUri());
        return resource.exists() ? ResponseEntity.ok().body(resource) : ResponseEntity.notFound().build();
    }
}
