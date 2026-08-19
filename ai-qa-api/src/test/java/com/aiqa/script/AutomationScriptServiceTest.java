package com.aiqa.script;

import com.aiqa.application.ApplicationTarget;
import com.aiqa.application.ApplicationTargetRepository;
import com.aiqa.automation.AutomationResponse;
import com.aiqa.automation.PlaywrightAutomationService;
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
    private final PlaywrightAutomationService generator = mock(PlaywrightAutomationService.class);
    private final AutomationScriptService service = new AutomationScriptService(scripts, products, generator);

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

    @Test void revisionIncrementsVersionAndReturnsToDraft() {
        UUID id=UUID.randomUUID(); AutomationScript script=new AutomationScript(UUID.randomUUID(),UUID.randomUUID(),"Flow",List.of("open the application")); script.approve();
        when(scripts.findById(id)).thenReturn(Optional.of(script)); when(scripts.save(script)).thenReturn(script);
        AutomationScript revised=service.revise(id,new AutomationScriptService.ReviseScriptRequest(List.of("open the application","verify \"Ready\"")));
        assertEquals(2,revised.getVersion()); assertEquals("DRAFT",revised.getStatus()); assertEquals(2,revised.getSteps().size());
    }

    @Test void generationRequiresApprovalAndUsesProductTarget() {
        UUID id=UUID.randomUUID(), companyId=UUID.randomUUID(), productId=UUID.randomUUID();
        AutomationScript script=new AutomationScript(companyId,productId,"Flow",List.of("open the application")); script.approve();
        when(scripts.findById(id)).thenReturn(Optional.of(script));
        when(products.findById(productId)).thenReturn(Optional.of(new ApplicationTarget("App","https://example.test","UAT","NONE",companyId)));
        when(generator.generate(any())).thenReturn(new AutomationResponse("AT-"+id,"Playwright","Java","Flow.java","class Flow {}"));
        AutomationResponse response=service.generate(id,new AutomationScriptService.GenerateScriptRequest(null,"Expected"));
        assertEquals("Playwright",response.framework()); verify(generator).generate(argThat(r->r.url().equals("https://example.test")&&r.steps().size()==1));
    }

    @Test void draftCannotGenerate() {
        UUID id=UUID.randomUUID();
        when(scripts.findById(id)).thenReturn(Optional.of(new AutomationScript(UUID.randomUUID(),UUID.randomUUID(),"Draft",List.of("open the application"))));
        assertThrows(IllegalStateException.class,()->service.generate(id,null));
        verifyNoInteractions(generator);
    }
}
