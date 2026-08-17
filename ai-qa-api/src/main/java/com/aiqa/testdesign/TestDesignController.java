package com.aiqa.testdesign;

import com.aiqa.requirement.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-design")
public class TestDesignController {
    private final RequirementRepository requirements;
    private final AiRequirementService aiRequirementService;
    private final TestDesignService testDesignService;

    public TestDesignController(RequirementRepository requirements,
                                AiRequirementService aiRequirementService,
                                TestDesignService testDesignService) {
        this.requirements = requirements;
        this.aiRequirementService = aiRequirementService;
        this.testDesignService = testDesignService;
    }

    @PostMapping("/generate")
    public ResponseEntity<TestDesignResponse> generate(@Valid @RequestBody RequirementRequest request) {
        Requirement requirement = new Requirement();
        requirement.setTitle(request.title());
        requirement.setDescription(request.description());
        requirement.setAcceptanceCriteria(request.acceptanceCriteria());
        Requirement saved = requirements.save(requirement);

        RequirementAnalysis analysis = aiRequirementService.analyze(saved);
        return ResponseEntity.ok(testDesignService.design(saved, analysis));
    }
}
