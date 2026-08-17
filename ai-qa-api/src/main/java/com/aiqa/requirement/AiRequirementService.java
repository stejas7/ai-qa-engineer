package com.aiqa.requirement;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.*;
import java.util.List;

@Service
public class AiRequirementService {
 private final ObjectMapper mapper=new ObjectMapper();
 @Value("${openai.api-key:}") private String apiKey;
 @Value("${openai.model:gpt-4.1-mini}") private String model;
 public RequirementAnalysis analyze(Requirement r){
  if(apiKey==null||apiKey.isBlank()) return demo(r);
  try{
   String prompt="Analyze this software requirement as a senior QA architect. Return ONLY valid JSON with fields summary, businessRules, questions, testScenarios. Each testScenario must have id,title,type,priority,steps,expectedResult. Title: "+r.getTitle()+" Description: "+r.getDescription()+" Acceptance criteria: "+r.getAcceptanceCriteria();
   String body=mapper.writeValueAsString(java.util.Map.of("model",model,"input",prompt));
   HttpRequest req=HttpRequest.newBuilder().uri(URI.create("https://api.openai.com/v1/responses")).header("Authorization","Bearer "+apiKey).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(body)).build();
   HttpResponse<String> res=HttpClient.newHttpClient().send(req,HttpResponse.BodyHandlers.ofString());
   if(res.statusCode()<200||res.statusCode()>=300) throw new IllegalStateException("AI HTTP "+res.statusCode());
   JsonNode root=mapper.readTree(res.body());
   for(JsonNode item:root.path("output")) for(JsonNode content:item.path("content")) if(content.has("text")) return mapper.readValue(content.get("text").asText(),RequirementAnalysis.class);
   throw new IllegalStateException("No AI text output");
  }catch(Exception e){return demo(r);}
 }
 private RequirementAnalysis demo(Requirement r){return new RequirementAnalysis(r.getDescription(),r.getAcceptanceCriteria(),List.of("Confirm all business rules and error messages with the product owner."),List.of(
  new TestScenario("TC-001","Happy path","FUNCTIONAL","HIGH",List.of("Prepare valid data","Execute business flow","Verify result"),"Business flow completes successfully."),
  new TestScenario("TC-002","Invalid input","NEGATIVE","HIGH",List.of("Prepare invalid data","Execute business flow"),"Invalid input is rejected with a clear error."),
  new TestScenario("TC-003","Boundary validation","BOUNDARY","MEDIUM",List.of("Use minimum and maximum allowed values","Execute business flow"),"Boundary values are handled according to the requirement.")));}
}
