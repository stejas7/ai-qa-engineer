# Scorpion 2.0 — V5 Autonomous QA Mission

V5 is the product baseline: one business requirement and one UAT target become one autonomous QA mission.

## Flow

```text
Business Requirement + UAT URL
            |
            v
     Requirement Analysis
            |
            v
       Test Generation
            |
            v
   Automation Generation
            |
            v
       UAT Execution
            |
            v
      Failure Analysis
            |
            v
      Final QA Decision
```

The user does not manually move between stages. `ScorpionMissionOrchestrator` coordinates the existing requirement, test-design, automation, execution and failure-analysis services.

## Product rule

Version pages are implementation/history views. The primary user experience is a Scorpion mission.

## Exit criteria

- Requirement accepted as mission input.
- Test cases generated automatically.
- Automation generated automatically.
- UAT execution starts automatically.
- Failures are analyzed automatically.
- Mission ends with a business-readable QA decision.
