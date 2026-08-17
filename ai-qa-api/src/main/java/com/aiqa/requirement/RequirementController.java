package com.aiqa.requirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/requirements")
public class RequirementController {
    private final RequirementRepository repository; private final AiRequirementService ai;
    public RequirementController(RequirementRepository repository,AiRequirementService ai){this.repository=repository;this.ai=ai;}
    @PostMapping public Requirement create(@Valid @RequestBody RequirementRequest req){
        Requirement r=new Requirement(); r.setTitle(req.title()); r.setDescription(req.description()); r.setAcceptanceCriteria(req.acceptanceCriteria()); return repository.save(r);
    }
    @GetMapping public List<Requirement> list(){return repository.findAll();}
    @PostMapping("/analyze") public ResponseEntity<RequirementAnalysis> analyze(@Valid @RequestBody RequirementRequest req){
        Requirement r=new Requirement(); r.setTitle(req.title()); r.setDescription(req.description()); r.setAcceptanceCriteria(req.acceptanceCriteria()); return ResponseEntity.ok(ai.analyze(repository.save(r)));
    }
}
