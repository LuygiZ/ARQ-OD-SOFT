# ========================================
# STAGE 1: Build
# ========================================
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /build

# Copy shared-kernel first (dependency)
COPY ../shared-kernel/pom.xml /build/shared-kernel/pom.xml
COPY ../shared-kernel/src /build/shared-kernel/src

# Install shared-kernel to local Maven repo
WORKDIR /build/shared-kernel
RUN mvn clean install -DskipTests

# Copy book-service
WORKDIR /build
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# ========================================
# STAGE 2: Runtime
# ========================================
FROM eclipse-temurin:21-jre-alpine

# Install utilities for health checks and debugging
RUN apk add --no-cache \
    wget \
    curl \
    netcat-openbsd

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /build/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R spring:spring /app

USER spring

# Expose port
EXPOSE 8083

# JVM optimizations for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]