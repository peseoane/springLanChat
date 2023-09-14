FROM maven:3.9-eclipse-temurin-17-alpine as buildstage
WORKDIR /app
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn clean package -Prelease -DskipTests

FROM openjdk:17-alpine as runstage
WORKDIR /app
COPY --from=buildstage /app/target/*.jar ./app.jar
CMD ["java", "-jar", "app.jar"]
