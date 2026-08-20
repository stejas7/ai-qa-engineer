package com.aiqa.platform;

import com.aiqa.application.ApplicationTarget;
import com.aiqa.application.ApplicationTargetRepository;
import com.aiqa.company.Company;
import com.aiqa.company.CompanyRepository;
import com.aiqa.security.AppUser;
import com.aiqa.security.AppUserRepository;
import com.aiqa.security.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlatformOversightControllerTest {
    @Mock CompanyRepository companies;
    @Mock ApplicationTargetRepository products;
    @Mock AppUserRepository users;

    @Test
    void exposesOnlySafeReadOnlyPlatformProjections() {
        UUID companyId = UUID.randomUUID();
        Company company = new Company("Acme QA", "acme-qa");
        ApplicationTarget product = new ApplicationTarget("Portal", "https://uat.example.test", "UAT", "USERNAME_PASSWORD", companyId);
        AppUser user = new AppUser(companyId, "tester@example.test", "bcrypt-hash-must-never-leak", UserRole.TESTER);

        when(companies.findAll()).thenReturn(List.of(company));
        when(products.findByCompanyIdOrderByCreatedAtDesc(company.getId())).thenReturn(List.of(product));
        when(users.findByCompanyIdOrderByCreatedAtAsc(company.getId())).thenReturn(List.of(user));
        when(products.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(product));
        when(users.findAll()).thenReturn(List.of(user));

        PlatformOversightController controller = new PlatformOversightController(companies, products, users);

        assertEquals(1, controller.companies().size());
        assertEquals(1, controller.products().size());
        assertEquals(1, controller.users().size());

        List<String> userFields = Arrays.stream(PlatformOversightController.UserView.class.getRecordComponents())
                .map(component -> component.getName()).toList();
        List<String> productFields = Arrays.stream(PlatformOversightController.ProductView.class.getRecordComponents())
                .map(component -> component.getName()).toList();

        assertFalse(userFields.contains("passwordHash"));
        assertFalse(userFields.contains("password"));
        assertFalse(productFields.contains("baseUrl"));
        assertFalse(productFields.contains("credential"));
        assertFalse(productFields.contains("secret"));
    }
}
