FROM maven:3.8-eclipse-temurin-21-alpine AS build

# Set working directory
WORKDIR /app

# Copy the entire project
COPY . .

# Build the eureka-server project
RUN mvn clean package -pl eureka-server

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Install necessary packages
RUN apk update && \
    apk add --no-cache \
    bash \
    curl

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create necessary directories
RUN mkdir -p /app/logs && \
    chown -R appuser:appgroup /app

# Copy built jar file from build stage
COPY --from=build /app/eureka-server/target/*.jar /app/app.jar

# Set proper permissions
RUN chmod 755 /app/app.jar && \
    chown appuser:appgroup /app/app.jar

# Switch to non-root user
USER appuser

# Expose the application port
EXPOSE 8761

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://eureka-server:8761/actuator/health || exit 1

# Start the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
