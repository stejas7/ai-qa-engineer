package com.aiqa.requirement;
import java.util.List;
public record TestScenario(String id,String title,String type,String priority,List<String> steps,String expectedResult){}
