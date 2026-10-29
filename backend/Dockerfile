# ─────────────────────────────────────────────────────────────────────────────
# Hostel Complaint Management System — Backend Dockerfile
# Task 11: Docker Image and Container Lifecycle
#
# Multi-stage aware: expects a pre-built JAR at target/complaint-system-1.0.0.jar
# produced by:  mvn clean package -DskipTests
# ─────────────────────────────────────────────────────────────────────────────

# Base: Eclipse Temurin JRE 21 on Alpine (small footprint ~180 MB)
FROM eclipse-temurin:21-jre-alpine

# OCI standard labels (visible via docker inspect)
LABEL maintainer="23102A0008"
LABEL org.opencontainers.image.title="hostel-cms-backend"
LABEL org.opencontainers.image.description="Hostel Complaint Management System - Spring Boot API"
LABEL org.opencontainers.image.version="1.0.0"
LABEL org.opencontainers.image.source="https://github.com/Sahil946141/hostel-complaint-system"

# Working directory inside the container
WORKDIR /app

# Copy the executable JAR (built by Maven)
COPY target/complaint-system-1.0.0.jar app.jar

# Expose the embedded-Tomcat HTTP port
EXPOSE 8080

# Runtime environment variables — all can be overridden with -e or docker compose
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/hostel_db
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=postgres
ENV APP_JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
ENV APP_JWT_EXPIRATION=86400000

# Container health check — polls the /api/auth/health endpoint
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/auth/health || exit 1

# Start the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
