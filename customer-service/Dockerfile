FROM maven:3.8-eclipse-temurin-21-alpine AS build

# Set working directory
WORKDIR /app

# Copy parent POM and all module POMs first to leverage Docker cache
COPY pom.xml .
COPY common/pom.xml ./common/
COPY eureka-server/pom.xml ./eureka-server/
COPY customer-service/pom.xml ./customer-service/
COPY account-service/pom.xml ./account-service/
COPY card-service/pom.xml ./card-service/
COPY gateway-server/pom.xml ./gateway-server/

# Download all dependencies for submodules to leverage Docker cache
RUN mvn dependency:go-offline -B

# Copy the common module source code first since all services depend on it
COPY common/src ./common/src

# Build common module first
RUN mvn -pl common -am clean install

# Copy customer-service module source code
COPY customer-service/src ./customer-service/src

# Build customer-service
RUN mvn -pl customer-service clean package

# Runtime stage - use smaller JRE image
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Install necessary packages
RUN apk update && \
    apk add --no-cache \
    bash \
    curl \
    tzdata

# Add non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Create necessary directories
RUN mkdir -p /app/uploads /app/logs && \
    chown -R appuser:appgroup /app

# Copy application jar from build stage
COPY --from=build /app/customer-service/target/*.jar /app/app.jar

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
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

# Start the application
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app/app.jar"]