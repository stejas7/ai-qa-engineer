package com.aiqa.execution;

import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/** Stores browser evidence in PostgreSQL while retaining the existing filesystem copy as a fallback. */
@Service
public class ExecutionEvidenceStore {
    private final ExecutionEvidenceRepository repository;

    public ExecutionEvidenceStore(ExecutionEvidenceRepository repository) {
        this.repository = repository;
    }

    public void persist(Path path) {
        try {
            if (path == null || !Files.isRegularFile(path)) return;
            String fileName = path.getFileName().toString();
            repository.save(new ExecutionEvidence(fileName, Files.readAllBytes(path), "image/png"));
        } catch (Exception e) {
            throw new IllegalStateException("Could not persist execution evidence", e);
        }
    }

    public Optional<ExecutionEvidence> find(String fileName) {
        if (fileName == null || fileName.isBlank() || fileName.contains("/") || fileName.contains("\\")) {
            return Optional.empty();
        }
        return repository.findById(fileName);
    }
}
