FROM mcr.microsoft.com/playwright/java:v1.52.0-noble

WORKDIR /app

LABEL org.opencontainers.image.source="https://github.com/stejas7/ai-qa-engineer"

COPY ai-qa-api/target/ai-qa-api-0.1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
