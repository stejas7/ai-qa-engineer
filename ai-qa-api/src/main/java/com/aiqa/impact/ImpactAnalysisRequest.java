package com.aiqa.impact;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Input for V12 change-impact analysis.
 *
 * @param changedFiles files changed by the commit or pull request
 * @param diff unified diff text; optional, but useful for higher-confidence classification
 */
public record ImpactAnalysisRequest(
        @NotEmpty List<String> changedFiles,
        String diff) {
}
