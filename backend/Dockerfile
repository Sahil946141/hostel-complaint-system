# Use Eclipse Temurin Java 21 (slim)
FROM eclipse-temurin:21-jre-alpine

# Set working directory inside container
WORKDIR /app

# Add metadata
LABEL maintainer="23102A0008"
LABEL description="Hostel Complaint Management System - Backend"
LABEL version="1.0.0"

# Copy the built JAR file
COPY target/complaint-system-1.0.0.jar app.jar

# Expose port 8080
EXPOSE 8080

# Environment variables (can be overridden at runtime)
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/hostel_db
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=postgres

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/auth/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
