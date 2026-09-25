# Multi-stage Dockerfile for DevOpsAI Spring Boot Backend

# Stage 1: Build Stage
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# Copy source code and package application jar
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Production Runtime Stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root system user for security
RUN addgroup -S devopsai && adduser -S devopsai -G devopsai

# Copy compiled jar from builder stage
COPY --from=builder /app/target/*.jar app.jar
RUN chown devopsai:devopsai app.jar

USER devopsai

# Expose Spring Boot HTTP port
EXPOSE 8080

# Configure JVM heap parameters & entrypoint
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
