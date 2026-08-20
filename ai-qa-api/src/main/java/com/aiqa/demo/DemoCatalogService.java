package com.aiqa.demo;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Read-only deterministic demo dataset used to demonstrate the multi-tenant product flow
 * without inserting fake records into customer/production tables.
 */
@Service
public class DemoCatalogService {

    public DemoCatalog catalog() {
        List<DemoCompany> companies = new ArrayList<>();
        String[] companyNames = {
                "Northstar Retail Labs",
                "BluePeak Fintech",
                "Greenfield Health Systems",
                "Orbit Travel Technologies",
                "Summit Manufacturing Cloud"
        };
        String[][] products = {
                {"Customer Commerce Portal", "Store Operations Console"},
                {"Digital Wallet", "Merchant Settlement Portal"},
                {"Patient Services Portal", "Care Operations Dashboard"},
                {"Traveller Booking Portal", "Partner Operations Console"},
                {"Supplier Quality Portal", "Factory Operations Console"}
        };

        for (int c = 0; c < companyNames.length; c++) {
            String slug = slug(companyNames[c]);
            List<DemoUser> users = new ArrayList<>();
            for (int u = 1; u <= 10; u++) {
                String role = u == 1 ? "COMPANY_ADMIN" : u <= 3 ? "QA_MANAGER" : u <= 8 ? "TESTER" : "VIEWER";
                users.add(new DemoUser("demo.user" + u + "@" + slug + ".example", role, true));
            }

            List<DemoProduct> companyProducts = new ArrayList<>();
            for (int p = 0; p < 2; p++) {
                String productName = products[c][p];
                List<DemoRequirement> requirements = new ArrayList<>();
                for (int r = 1; r <= 10; r++) {
                    String id = "DEMO-REQ-" + (c + 1) + (p + 1) + String.format("%02d", r);
                    requirements.add(new DemoRequirement(
                            id,
                            requirementTitle(productName, r),
                            r % 5 == 0 ? "HIGH" : r % 2 == 0 ? "MEDIUM" : "LOW",
                            r % 4 == 0 ? "FAILED" : "COMPLETED",
                            6 + (r % 5),
                            r % 4 == 0 ? 1 : 0,
                            "Demo evidence and traceability are available for this simulated run."
                    ));
                }
                companyProducts.add(new DemoProduct(
                        "DEMO-PROD-" + (c + 1) + (p + 1),
                        productName,
                        "UAT",
                        "https://" + slug + "-product-" + (p + 1) + ".example.com",
                        p == 0 ? "USERNAME_PASSWORD" : "NONE",
                        requirements
                ));
            }
            companies.add(new DemoCompany("DEMO-COMP-" + (c + 1), companyNames[c], slug, users, companyProducts));
        }
        return new DemoCatalog(true, companies.size(), 10, 50, 100, companies);
    }

    private String requirementTitle(String productName, int index) {
        return switch (index) {
            case 1 -> productName + " login and session validation";
            case 2 -> productName + " authorized navigation";
            case 3 -> productName + " create business transaction";
            case 4 -> productName + " validation and error handling";
            case 5 -> productName + " role-based access control";
            case 6 -> productName + " search and filtering";
            case 7 -> productName + " update workflow";
            case 8 -> productName + " audit evidence";
            case 9 -> productName + " performance acceptance";
            default -> productName + " end-to-end release journey";
        };
    }

    private String slug(String value) {
        return value.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    public record DemoCatalog(boolean demo, int companyCount, int productCount, int userCount,
                              int requirementCount, List<DemoCompany> companies) {}
    public record DemoCompany(String id, String name, String slug, List<DemoUser> users,
                              List<DemoProduct> products) {}
    public record DemoUser(String email, String role, boolean active) {}
    public record DemoProduct(String id, String name, String environment, String baseUrl,
                              String authType, List<DemoRequirement> requirements) {}
    public record DemoRequirement(String id, String title, String risk, String status,
                                  int generatedTests, int failedTests, String evidenceSummary) {}
}
