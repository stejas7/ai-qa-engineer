package com.aiqa.application;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApplicationTargetTest {

    @Test
    void keepsLegacyRegistrationCompatibleWithoutCompany() {
        ApplicationTarget target = new ApplicationTarget("Portal", "https://example.test", null, null);

        assertEquals("UAT", target.getEnvironment());
        assertEquals("NONE", target.getAuthType());
        assertNull(target.getCompanyId());
    }

    @Test
    void associatesRegisteredProductWithCompany() {
        UUID companyId = UUID.randomUUID();
        ApplicationTarget target = new ApplicationTarget(
                "Portal", "https://example.test", "QA", "NONE", companyId);

        assertEquals(companyId, target.getCompanyId());
    }
}
