package com.aiqa.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
public class DemoCatalogController {
    private final DemoCatalogService service;

    public DemoCatalogController(DemoCatalogService service) {
        this.service = service;
    }

    @GetMapping("/catalog")
    public DemoCatalogService.DemoCatalog catalog() {
        return service.catalog();
    }
}
