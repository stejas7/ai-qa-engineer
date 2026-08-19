package com.aiqa.session;

import com.aiqa.application.ApplicationTarget;
import com.aiqa.application.ApplicationTargetRepository;
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

/** Unit tests for M10 UAT session company/product boundaries. */
class UatSessionControllerTest {

    @Test
    void rejectsCrossCompanyProductSession() {
        UatSessionRepository sessionRepository = mock(UatSessionRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        ApplicationTargetRepository applicationRepository = mock(ApplicationTargetRepository.class);
        UatSessionController controller = new UatSessionController(
                sessionRepository, companyRepository, applicationRepository);

        UUID selectedCompany = UUID.randomUUID();
        UUID otherCompany = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        when(companyRepository.findById(selectedCompany)).thenReturn(Optional.of(new Company("Acme", "acme")));
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(
                new ApplicationTarget("Checkout", "https://checkout.example.test", "UAT", "NONE", otherCompany)));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> controller.create(new UatSessionController.CreateUatSessionRequest(
                        selectedCompany, applicationId, "3.0.0", "Validate checkout release")));

        assertEquals(409, error.getStatusCode().value());
        verify(sessionRepository, never()).save(any(UatSession.class));
    }

    @Test
    void createsSessionForActiveCompanyOwnedProduct() {
        UatSessionRepository sessionRepository = mock(UatSessionRepository.class);
        CompanyRepository companyRepository = mock(CompanyRepository.class);
        ApplicationTargetRepository applicationRepository = mock(ApplicationTargetRepository.class);
        UatSessionController controller = new UatSessionController(
                sessionRepository, companyRepository, applicationRepository);

        UUID companyId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        when(companyRepository.findById(companyId)).thenReturn(Optional.of(new Company("Acme", "acme")));
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(
                new ApplicationTarget("Checkout", "https://checkout.example.test", "UAT", "NONE", companyId)));
        when(sessionRepository.save(any(UatSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UatSession result = controller.create(new UatSessionController.CreateUatSessionRequest(
                companyId, applicationId, "3.0.0", "Validate checkout release"));

        assertEquals(companyId, result.getCompanyId());
        assertEquals(applicationId, result.getApplicationId());
        assertEquals("3.0.0", result.getBuildVersion());
        assertEquals(UatSessionStatus.CREATED, result.getStatus());
    }
}
