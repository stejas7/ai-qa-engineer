package com.aiqa.script;

import com.aiqa.application.ApplicationTarget;
import com.aiqa.application.ApplicationTargetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** M12 script lifecycle service. Only the existing controlled UAT step language is accepted. */
@Service
public class AutomationScriptService {
    private final AutomationScriptRepository scripts;
    private final ApplicationTargetRepository products;

    public AutomationScriptService(AutomationScriptRepository scripts, ApplicationTargetRepository products) {
        this.scripts = scripts;
        this.products = products;
    }

    public AutomationScript create(CreateScriptRequest request) {
        if (request == null || request.companyId() == null || request.productId() == null)
            throw new IllegalArgumentException("companyId and productId are required");
        if (request.name() == null || request.name().isBlank()) throw new IllegalArgumentException("name is required");
        if (request.steps() == null || request.steps().isEmpty()) throw new IllegalArgumentException("at least one step is required");

        ApplicationTarget product = products.findById(request.productId())
                .orElseThrow(() -> new IllegalArgumentException("product not found"));
        if (!product.isActive()) throw new IllegalStateException("product is inactive");
        if (product.getCompanyId() == null || !product.getCompanyId().equals(request.companyId()))
            throw new IllegalArgumentException("product does not belong to company");
        if (scripts.existsByCompanyIdAndProductIdAndNameIgnoreCase(request.companyId(), request.productId(), request.name().trim()))
            throw new IllegalStateException("script name already exists for product");

        List<String> normalized = request.steps().stream().map(String::trim).peek(this::validateStep).toList();
        return scripts.save(new AutomationScript(request.companyId(), request.productId(), request.name().trim(), normalized));
    }

    public List<AutomationScript> list(UUID companyId, UUID productId) {
        return scripts.findByCompanyIdAndProductIdOrderByCreatedAtDesc(companyId, productId);
    }

    public AutomationScript approve(UUID id) {
        AutomationScript script = scripts.findById(id).orElseThrow(() -> new IllegalArgumentException("script not found"));
        script.approve();
        return scripts.save(script);
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
}
