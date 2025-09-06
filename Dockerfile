FROM openjdk:21-jdk-slim

WORKDIR /app

RUN apt update && apt install -y curl && rm -rf /var/lib/apt/lists/* && mkdir -p /data && chmod 777 /data

# Copy the built jar file (Gradle places it in build/libs/)
COPY build/libs/app.jar app.jar

# Create a non-root user to run the application
RUN adduser --system --group spring
USER spring:spring

ENTRYPOINT ["java", "-jar", "/app/app.jar"]