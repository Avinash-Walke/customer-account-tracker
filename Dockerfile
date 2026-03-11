# Use a Official Eclipse temurin OpenJDK 17 base
FROM eclipse-temurin:17-jdk

LABEL authors="avinashwalke@outlook.com" \
      application="customer-account-tracker" \
      java.version="17" \
      description="Spring Boot URL Shortener Application"

# Set the working directory
WORKDIR /app

# Copy the application's JAR file into the container and normalize its name
COPY target/customer-account-tracker-1.0-SNAPSHOT.jar /app/customer-account-tracker.jar

# Expose application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "/app/customer-account-tracker.jar"]
