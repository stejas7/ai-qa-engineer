package com.aiqa.application;

import com.aiqa.company.Company;
import com.aiqa.company.CompanyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for M9 company/product registration boundaries. */
class ApplicationTargetControllerTest {

    @Test
    void rejectsProductRegistrationForInactiveCompany() {
        ApplicationTargetRepository applicationRepository = mock(ApplicationTargetRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        ApplicationTargetController controller = new ApplicationTargetController(applicationRepository, companyRepository);
        UUID companyId = UUID.randomUUID();
        Company company = new Company("Inactive Co", "inactive-co");
        company.setActive(false);
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(company));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.create(new ApplicationTargetController.CreateApplicationRequest(
                        "Product A", "https://example.test", "UAT", "NONE", companyId)));

        assertEquals(409, error.getStatusCode().value());
        verify(applicationRepository, never()).save(any(ApplicationTarget.class));
    }

    @Test
    void rejectsUnknownCompanyBeforeSavingProduct() {
        ApplicationTargetRepository applicationRepository = mock(ApplicationTargetRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        ApplicationTargetController controller = new ApplicationTargetController(applicationRepository, companyRepository);
        UUID companyId = UUID.randomUUID();
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.create(new ApplicationTargetController.CreateApplicationRequest(
                        "Product A", "https://example.test", "UAT", "NONE", companyId)));

        assertEquals(400, error.getStatusCode().value());
        verify(applicationRepository, never()).save(any(ApplicationTarget.class));
    }

    @Test
    void rejectsDuplicateProductNameWithinCompany() {
        ApplicationTargetRepository applicationRepository = mock(ApplicationTargetRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        ApplicationTargetController controller = new ApplicationTargetController(applicationRepository, companyRepository);
        UUID companyId = UUID.randomUUID();
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(new Company("Acme", "acme")));
        when(applicationRepository.existsByCompanyIdAndNameIgnoreCase(companyId, "Checkout"))
                .thenReturn(true);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.create(new ApplicationTargetController.CreateApplicationRequest(
                        " Checkout ", "https://checkout.example.test", "UAT", "NONE", companyId)));

        assertEquals(409, error.getStatusCode().value());
        verify(applicationRepository, never()).save(any(ApplicationTarget.class));
    }
}
