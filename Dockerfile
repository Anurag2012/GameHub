# Use a base JDK image
FROM openjdk:17-jdk-slim

# Set work directory
WORKDIR /app

# Copy the built jar (we'll build it before deployment)
COPY target/GameHub.jar app.jar

# Expose the port Spring Boot runs on
EXPOSE 8181

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
