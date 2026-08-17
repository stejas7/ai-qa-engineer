package com.aiqa.requirement;
import java.util.List;
public record RequirementAnalysis(String summary,List<String> businessRules,List<String> questions,List<TestScenario> testScenarios){}
