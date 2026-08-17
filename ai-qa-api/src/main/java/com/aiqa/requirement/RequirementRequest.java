package com.aiqa.requirement;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
public record RequirementRequest(@NotBlank String title,@NotBlank String description,List<String> acceptanceCriteria){}
