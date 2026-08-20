package com.aiqa.security;

import com.aiqa.company.Company;
import com.aiqa.company.CompanyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CompanyRegistrationServiceTest {
    private final CompanyRepository companies = mock(CompanyRepository.class);
    private final AppUserService users = mock(AppUserService.class);
    private final CompanyRegistrationService service = new CompanyRegistrationService(companies, users);

    @Test
    void registersCompanyAndFirstCompanyAdmin() {
        UUID companyId = UUID.randomUUID();
        when(companies.existsBySlugIgnoreCase("acme-labs")).thenReturn(false);
        when(companies.save(any())).thenAnswer(invocation -> {
            Company company = invocation.getArgument(0);
            ReflectionTestUtils.setField(company, "id", companyId);
            return company;
        });
        when(users.createCompanyAdmin(companyId, "Admin@Acme.test", "StrongPassword123!"))
                .thenReturn(new AppUser(companyId, "admin@acme.test", "hash", UserRole.COMPANY_ADMIN));

        var result = service.register(new CompanyRegistrationService.RegisterCompanyRequest(
                " Acme Labs ", null, "Admin@Acme.test", "StrongPassword123!"));

        assertEquals(companyId, result.companyId());
        assertEquals("Acme Labs", result.companyName());
        assertEquals("acme-labs", result.slug());
        assertEquals("admin@acme.test", result.adminEmail());
        assertEquals("COMPANY_ADMIN", result.role());
        verify(users).createCompanyAdmin(companyId, "Admin@Acme.test", "StrongPassword123!");
    }

    @Test
    void rejectsDuplicateCompanyBeforeCreatingAdmin() {
        when(companies.existsBySlugIgnoreCase("acme")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> service.register(
                new CompanyRegistrationService.RegisterCompanyRequest("Acme", "acme", "admin@acme.test", "StrongPassword123!")));

        verify(companies, never()).save(any());
        verifyNoInteractions(users);
    }

    @Test
    void rejectsInvalidRegistrationInput() {
        assertThrows(IllegalArgumentException.class, () -> service.register(null));
        assertThrows(IllegalArgumentException.class, () -> service.register(
                new CompanyRegistrationService.RegisterCompanyRequest(" ", null, "admin@acme.test", "StrongPassword123!")));
        verifyNoInteractions(users);
    }
}
