# ===== Stage 1: Build =====
FROM maven:3.9.2-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy Maven files first (for caching)
COPY pom.xml .

# Copy source code
COPY src ./src

# Build the JAR without running tests
RUN mvn clean package -DskipTests

# ===== Stage 2: Runtime =====
FROM amazoncorretto:17-alpine

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /app/target/aws-demo.jar ./aws-demo.jar

# Expose port your app listens on
EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "aws-demo.jar"]
