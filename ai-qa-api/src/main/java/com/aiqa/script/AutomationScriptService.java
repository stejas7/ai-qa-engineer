package com.aiqa.script;

import com.aiqa.application.ApplicationTarget;
import com.aiqa.application.ApplicationTargetRepository;
import com.aiqa.automation.AutomationRequest;
import com.aiqa.automation.AutomationResponse;
import com.aiqa.automation.PlaywrightAutomationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** M12 script lifecycle service. Only the existing controlled UAT step language is accepted. */
@Service
public class AutomationScriptService {
    private final AutomationScriptRepository scripts;
    private final ApplicationTargetRepository products;
    private final PlaywrightAutomationService generator;

    @Autowired
    public AutomationScriptService(AutomationScriptRepository scripts, ApplicationTargetRepository products,
                                   PlaywrightAutomationService generator) {
        this.scripts = scripts;
        this.products = products;
        this.generator = generator;
    }

    /** Test-friendly constructor for lifecycle unit tests that do not generate code. */
    AutomationScriptService(AutomationScriptRepository scripts, ApplicationTargetRepository products) {
        this(scripts, products, null);
    }

    public AutomationScript create(CreateScriptRequest request) {
        if (request == null || request.companyId() == null || request.productId() == null)
            throw new IllegalArgumentException("companyId and productId are required");
        if (request.name() == null || request.name().isBlank()) throw new IllegalArgumentException("name is required");
        List<String> normalized = normalizeAndValidate(request.steps());

        ApplicationTarget product = requireOwnedActiveProduct(request.companyId(), request.productId());
        if (scripts.existsByCompanyIdAndProductIdAndNameIgnoreCase(request.companyId(), request.productId(), request.name().trim()))
            throw new IllegalStateException("script name already exists for product");
        return scripts.save(new AutomationScript(product.getCompanyId(), request.productId(), request.name().trim(), normalized));
    }

    public List<AutomationScript> list(UUID companyId, UUID productId) {
        if (companyId == null || productId == null) throw new IllegalArgumentException("companyId and productId are required");
        requireOwnedActiveProduct(companyId, productId);
        return scripts.findByCompanyIdAndProductIdOrderByCreatedAtDesc(companyId, productId);
    }

    public AutomationScript approve(UUID id) {
        AutomationScript script = requireScript(id);
        script.approve();
        return scripts.save(script);
    }

    public AutomationScript revise(UUID id, ReviseScriptRequest request) {
        AutomationScript script = requireScript(id);
        List<String> normalized = normalizeAndValidate(request == null ? null : request.steps());
        script.revise(normalized);
        return scripts.save(script);
    }

    /** Generates inspectable Java/Playwright testware only after explicit approval. */
    public AutomationResponse generate(UUID id, GenerateScriptRequest request) {
        AutomationScript script = requireScript(id);
        if (!"APPROVED".equals(script.getStatus())) throw new IllegalStateException("script must be approved before generation");
        if (generator == null) throw new IllegalStateException("automation generator unavailable");
        ApplicationTarget product = requireOwnedActiveProduct(script.getCompanyId(), script.getProductId());
        String url = request != null && request.url() != null && !request.url().isBlank() ? request.url().trim() : product.getBaseUrl();
        if (url == null || url.isBlank()) throw new IllegalArgumentException("target URL is required");
        String expected = request == null || request.expectedResult() == null || request.expectedResult().isBlank()
                ? "Complete the approved UAT procedure without violating expected business behavior"
                : request.expectedResult().trim();
        return generator.generate(new AutomationRequest("AT-" + id, script.getName() + " v" + script.getVersion(), url,
                script.getSteps(), expected));
    }

    private AutomationScript requireScript(UUID id) {
        if (id == null) throw new IllegalArgumentException("script id is required");
        return scripts.findById(id).orElseThrow(() -> new IllegalArgumentException("script not found"));
    }

    private ApplicationTarget requireOwnedActiveProduct(UUID companyId, UUID productId) {
        ApplicationTarget product = products.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("product not found"));
        if (!product.isActive()) throw new IllegalStateException("product is inactive");
        if (product.getCompanyId() == null || !product.getCompanyId().equals(companyId))
            throw new IllegalArgumentException("product does not belong to company");
        return product;
    }

    private List<String> normalizeAndValidate(List<String> steps) {
        if (steps == null || steps.isEmpty()) throw new IllegalArgumentException("at least one step is required");
        return steps.stream().map(step -> step == null ? "" : step.trim()).peek(this::validateStep).toList();
    }

    private void validateStep(String step) {
        String value = step == null ? "" : step.trim().toLowerCase(Locale.ROOT);
        if (value.isBlank()) throw new IllegalArgumentException("script step cannot be blank");
        boolean supported = value.startsWith("open ") || value.equals("open the application") || value.startsWith("enter ")
                || value.startsWith("fill ") || value.startsWith("select ") || value.startsWith("check ")
                || value.startsWith("tick ") || value.startsWith("click ") || value.startsWith("verify ");
        if (!supported) throw new IllegalArgumentException("unsupported automation step: " + step);
    }

    public record CreateScriptRequest(UUID companyId, UUID productId, String name, List<String> steps) {}
    public record ReviseScriptRequest(List<String> steps) {}
    public record GenerateScriptRequest(String url, String expectedResult) {}
}
