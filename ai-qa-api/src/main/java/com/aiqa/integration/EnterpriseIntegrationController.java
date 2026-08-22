package com.aiqa.integration;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only enterprise integration capability discovery for M35.
 *
 * @author Tejas Shah
 */
@RestController
@RequestMapping("/api/integrations/enterprise")
public class EnterpriseIntegrationController {

    private final EnterpriseIntegrationCatalog catalog;

    public EnterpriseIntegrationController(EnterpriseIntegrationCatalog catalog) {
        this.catalog = catalog;
    }

    /** Returns safe metadata for supported enterprise providers. */
    @GetMapping("/catalog")
    public List<EnterpriseIntegrationCatalog.ProviderDefinition> catalog() {
        return catalog.providers();
    }
}
