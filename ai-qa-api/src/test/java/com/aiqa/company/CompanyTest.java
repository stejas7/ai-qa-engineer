package com.aiqa.company;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompanyTest {

    @Test
    void derivesStableSlugFromCompanyName() {
        Company company = new Company("  Acme Quality Labs  ", null);

        assertEquals("Acme Quality Labs", company.getName());
        assertEquals("acme-quality-labs", company.getSlug());
        assertTrue(company.isActive());
    }

    @Test
    void rejectsSlugWithoutLettersOrNumbers() {
        assertThrows(IllegalArgumentException.class, () -> new Company("Acme", "---"));
    }
}
