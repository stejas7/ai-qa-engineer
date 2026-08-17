FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY pom.xml .
COPY ai-qa-api/pom.xml ai-qa-api/pom.xml
COPY ai-qa-api/src ai-qa-api/src

RUN mvn -q -f ai-qa-api/pom.xml -DskipTests package

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/ai-qa-api/target/ai-qa-api-0.1.0-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
