FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY ai-qa-api/pom.xml ai-qa-api/pom.xml
COPY ai-qa-api/src ai-qa-api/src

RUN mvn -q -f ai-qa-api/pom.xml -DskipTests package

# Playwright's official Java image includes the browser binaries and the
# native OS dependencies required by Chromium. This keeps UAT execution
# deterministic inside the deployed container instead of trying to install
# browsers during every API request.
FROM mcr.microsoft.com/playwright/java:v1.52.0-noble

WORKDIR /app

COPY --from=build /workspace/ai-qa-api/target/ai-qa-api-0.1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
