FROM maven:3.8-eclipse-temurin-21-alpine AS build

# Set working directory
WORKDIR /app

# Copy POM file
COPY pom.xml .

# Download all required dependencies
# This is a separate step to leverage Docker cache
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn package

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Install only essential packages
RUN apk update && \
    apk add --no-cache \
    bash \
    wget \
    curl \
    tzdata

# Add non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create necessary directories
RUN mkdir -p /app/uploads /app/logs && \
    chown -R appuser:appgroup /app

# Copy application jar from build stage
COPY --from=build /app/target/*.jar /app/app.jar

# Set proper permissions
RUN chmod -R 755 /app && \
    chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Create volume mount points
VOLUME ["/app/uploads", "/app/logs"]

# Expose the application port
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8081/api/actuator/health || exit 1

# Start the application
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]