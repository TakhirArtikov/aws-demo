# Stage 1: Build (optional if you already have JAR)
FROM maven:3.9.2-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM amazoncorretto:17-alpine
WORKDIR /app
COPY target/aws-demo.jar aws-demo.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "aws-demo.jar"]
