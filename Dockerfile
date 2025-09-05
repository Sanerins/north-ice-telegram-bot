FROM openjdk:21-jdk-slim

WORKDIR /app

# Copy the built jar file (Gradle places it in build/libs/)
COPY build/libs/*.jar app.jar

# Create a non-root user to run the application
RUN adduser --system --group spring
USER spring:spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]