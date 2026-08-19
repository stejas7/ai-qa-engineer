package com.aiqa.script;

import com.aiqa.application.ApplicationTarget;
import com.aiqa.application.ApplicationTargetRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AutomationScriptServiceTest {
    private final AutomationScriptRepository scripts = mock(AutomationScriptRepository.class);
    private final ApplicationTargetRepository products = mock(ApplicationTargetRepository.class);
    private final AutomationScriptService service = new AutomationScriptService(scripts, products);

    @Test void createsDraftScriptForActiveOwnedProduct() {
        UUID companyId = UUID.randomUUID(); UUID productId = UUID.randomUUID();
        ApplicationTarget product = new ApplicationTarget("Checkout", "https://example.test", "UAT", "NONE", companyId);
        when(products.findById(productId)).thenReturn(Optional.of(product));
        when(scripts.existsByCompanyIdAndProductIdAndNameIgnoreCase(companyId, productId, "Checkout flow")).thenReturn(false);
        when(scripts.save(any())).thenAnswer(inv -> inv.getArgument(0));
        AutomationScript result = service.create(new AutomationScriptService.CreateScriptRequest(companyId, productId, " Checkout flow ", List.of("open the application", "click \"Buy\"", "verify \"Success\"")));
        assertEquals("DRAFT", result.getStatus()); assertEquals("Checkout flow", result.getName()); assertEquals(3, result.getSteps().size());
    }

    @Test void rejectsUnsupportedStepBeforePersistence() {
        UUID companyId=UUID.randomUUID(), productId=UUID.randomUUID();
        when(products.findById(productId)).thenReturn(Optional.of(new ApplicationTarget("App","https://example.test","UAT","NONE",companyId)));
        assertThrows(IllegalArgumentException.class, () -> service.create(new AutomationScriptService.CreateScriptRequest(companyId, productId, "Bad", List.of("run shell command"))));
        verify(scripts, never()).save(any());
    }

    @Test void rejectsCrossCompanyProduct() {
        UUID companyId=UUID.randomUUID(), productId=UUID.randomUUID();
        when(products.findById(productId)).thenReturn(Optional.of(new ApplicationTarget("App","https://example.test","UAT","NONE",UUID.randomUUID())));
        assertThrows(IllegalArgumentException.class, () -> service.create(new AutomationScriptService.CreateScriptRequest(companyId, productId, "Flow", List.of("open the application"))));
    }

    @Test void rejectsDuplicateName() {
        UUID companyId=UUID.randomUUID(), productId=UUID.randomUUID();
        when(products.findById(productId)).thenReturn(Optional.of(new ApplicationTarget("App","https://example.test","UAT","NONE",companyId)));
        when(scripts.existsByCompanyIdAndProductIdAndNameIgnoreCase(companyId, productId, "Flow")).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> service.create(new AutomationScriptService.CreateScriptRequest(companyId, productId, "Flow", List.of("open the application"))));
    }

    @Test void approvesExistingScript() {
        UUID id=UUID.randomUUID(), companyId=UUID.randomUUID(), productId=UUID.randomUUID();
        AutomationScript script=new AutomationScript(companyId,productId,"Flow",List.of("open the application"));
        when(scripts.findById(id)).thenReturn(Optional.of(script)); when(scripts.save(script)).thenReturn(script);
        assertEquals("APPROVED",service.approve(id).getStatus());
    }
}
