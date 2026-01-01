FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the JAR file
COPY target/saga-orchestrator-1.0.0.jar app.jar

# Install wget for healthcheck
RUN apk add --no-cache wget

# Expose port
EXPOSE 8084

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]