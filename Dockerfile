FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Cache Maven dependencies by copying pom.xml first
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source code and package executable JAR (skipping tests for speed; tests run in CI pipeline)
COPY src ./src
RUN mvn clean package -DskipTests -B

# Extract Spring Boot layered jar for optimal container caching
RUN java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a dedicated non-root user and group for security hardening
RUN addgroup -S ledgergroup && adduser -S ledgeruser -G ledgergroup

# Copy layered jar components from builder stage to leverage Docker layer caching
COPY --from=builder /app/target/extracted/dependencies/ ./
COPY --from=builder /app/target/extracted/spring-boot-loader/ ./
COPY --from=builder /app/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /app/target/extracted/application/ ./

# Change ownership to non-root application user
RUN chown -R ledgeruser:ledgergroup /app

USER ledgeruser

# Expose HTTP service port
EXPOSE 8080

# Configure production JVM arguments (Container awareness, Virtual Threads, and memory limits)
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

# Launch Spring Boot using JarLauncher for fast startup
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]