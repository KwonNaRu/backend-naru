# Base image
FROM openjdk:17-jdk-alpine

# Define version argument
ARG JAR_VERSION=0.0.1-SNAPSHOT

# Copy JAR file
COPY build/libs/backend-${JAR_VERSION}.jar /app/app.jar

# Run JAR file
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
