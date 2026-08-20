package com.aiqa.pipeline;

import com.aiqa.application.ApplicationTarget;
import com.aiqa.application.ApplicationTargetRepository;
import com.aiqa.credential.RuntimeCredentialResolver;
import com.aiqa.credential.RuntimeCredentialResolver.ResolvedCredential;
import com.aiqa.security.AppUser;
import com.aiqa.security.AppUserRepository;
import com.aiqa.security.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.UUID;

/** M18/M19 tenant-authorized UAT launch. Tenant, target URL and runtime credentials are resolved server-side. */
@Service
public class TenantUatLaunchService {
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;

    private final RequirementFileExtractor extractor;
    private final FullPipelineService pipeline;
    private final PipelineRunRepository runs;
    private final ApplicationTargetRepository targets;
    private final AppUserRepository users;
    private final RuntimeCredentialResolver credentials;

    /** Compatibility constructor for focused unit tests and NONE-auth targets. */
    public TenantUatLaunchService(RequirementFileExtractor extractor,
                                  FullPipelineService pipeline,
                                  PipelineRunRepository runs,
                                  ApplicationTargetRepository targets,
                                  AppUserRepository users) {
        this(extractor, pipeline, runs, targets, users, null);
    }

    @Autowired
    public TenantUatLaunchService(RequirementFileExtractor extractor,
                                  FullPipelineService pipeline,
                                  PipelineRunRepository runs,
                                  ApplicationTargetRepository targets,
                                  AppUserRepository users,
                                  RuntimeCredentialResolver credentials) {
        this.extractor = extractor;
        this.pipeline = pipeline;
        this.runs = runs;
        this.targets = targets;
        this.users = users;
        this.credentials = credentials;
    }

    public PipelineRun launch(String actorEmail, UUID targetId, MultipartFile file) {
        AppUser actor = requireExecutor(actorEmail);
        if (targetId == null) throw new IllegalArgumentException("product environment is required");
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("A business requirement file is required");
        if (file.getSize() > MAX_FILE_SIZE) throw new IllegalArgumentException("File exceeds the 10 MB limit");

        ApplicationTarget target = targets.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("Product environment not found"));
        if (!actor.getCompanyId().equals(target.getCompanyId())) throw new SecurityException("Cross-tenant UAT launch denied");
        if (!target.isActive()) throw new IllegalStateException("Product environment is inactive");

        ResolvedCredential runtimeCredential = resolveCredential(actor.getCompanyId(), target);

        String rawText;
        try {
            rawText = extractor.extract(file);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read file: " + e.getMessage(), e);
        }
        if (rawText == null || rawText.isBlank()) throw new IllegalArgumentException("No readable text found in the uploaded file");

        String fileName = file.getOriginalFilename() == null ? "Uploaded requirement" : file.getOriginalFilename();
        PipelineRun run = runs.save(new PipelineRun(actor.getCompanyId().toString(), fileName));
        if (runtimeCredential == null) {
            pipeline.runInBackground(run.getId(), rawText, fileName, target.getBaseUrl(), true);
        } else {
            pipeline.runInBackground(run.getId(), rawText, fileName, target.getBaseUrl(), true, runtimeCredential);
        }
        return run;
    }

    private ResolvedCredential resolveCredential(UUID companyId, ApplicationTarget target) {
        String authType = target.getAuthType();
        if (authType == null || authType.isBlank() || "NONE".equalsIgnoreCase(authType)) return null;
        if (credentials == null) throw new IllegalStateException("Runtime credential resolver is unavailable");
        RuntimeCredentialResolver.CredentialReadiness readiness = credentials.readiness(companyId, target.getId());
        if (!readiness.runtimeReady()) throw new IllegalStateException(readiness.status());
        return credentials.resolve(companyId, target.getId());
    }

    private AppUser requireExecutor(String email) {
        if (email == null || email.isBlank()) throw new SecurityException("Authentication required");
        AppUser actor = users.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new SecurityException("Authenticated user not found"));
        if (!actor.isActive()) throw new IllegalStateException("User is inactive");
        if (actor.getRole() != UserRole.COMPANY_ADMIN
                && actor.getRole() != UserRole.QA_MANAGER
                && actor.getRole() != UserRole.TESTER) {
            throw new SecurityException("UAT execution permission required");
        }
        return actor;
    }
}
