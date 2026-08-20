package com.aiqa.pipeline;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;

/** Recreates a new autonomous UAT run from persisted normalized requirement output. */
@Service
public class PipelineRerunService {
    private final PipelineRunRepository runs;
    private final FullPipelineService pipeline;
    private final ObjectMapper objectMapper;

    public PipelineRerunService(PipelineRunRepository runs, FullPipelineService pipeline, ObjectMapper objectMapper) {
        this.runs = runs;
        this.pipeline = pipeline;
        this.objectMapper = objectMapper;
    }

    public PipelineRun rerun(UUID sourceId) {
        PipelineRun source = runs.findById(sourceId)
                .orElseThrow(() -> new IllegalArgumentException("Previous UAT run not found"));
        if (source.getResultJson() == null || source.getResultJson().isBlank()) {
            throw new IllegalStateException("This historical run does not contain reusable requirement content; upload the original file once more");
        }
        try {
            JsonNode root = objectMapper.readTree(source.getResultJson());
            String targetUrl = root.path("targetUrl").asText("https://example.com");
            StringBuilder requirementText = new StringBuilder();
            for (JsonNode requirement : root.path("requirements")) {
                append(requirementText, requirement.path("title").asText());
                append(requirementText, requirement.path("description").asText());
                for (JsonNode criterion : requirement.path("acceptanceCriteria")) append(requirementText, "Acceptance Criteria: " + criterion.asText());
                requirementText.append('\n');
            }
            if (requirementText.toString().isBlank()) {
                throw new IllegalStateException("No reusable requirement content was found in this historical result");
            }
            PipelineRun rerun = runs.save(new PipelineRun(source.getCompany(), source.getFileName()));
            boolean execute = targetUrl.startsWith("http://") || targetUrl.startsWith("https://");
            pipeline.runInBackground(rerun.getId(), requirementText.toString(), source.getFileName(), targetUrl, execute);
            return rerun;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Could not reconstruct the historical requirement for rerun", e);
        }
    }

    private void append(StringBuilder target, String value) {
        if (value != null && !value.isBlank()) target.append(value.trim()).append('\n');
    }
}
