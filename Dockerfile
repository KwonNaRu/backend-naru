# 빌드 단계
FROM gradle:7.6-jdk17 AS builder

WORKDIR /app
COPY . .
# gradlew 파일에 실행 권한 추가
RUN chmod +x ./gradlew  
# Gradle 빌드 실행
RUN SPRING_PROFILES_ACTIVE=docker ./gradlew clean build --no-daemon --info

# 실행 단계
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/build/libs/backend-0.0.1-SNAPSHOT.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
