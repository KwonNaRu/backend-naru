# Naru Backend

## 프로젝트 설명

Naru Backend는 개인 블로그의 사용자 인증 및 데이터 관리를 위한 RESTful API를 제공하는 Spring Boot 기반의 백엔드 애플리케이션입니다. 이 프로젝트는 PostgreSQL 데이터베이스를 사용하며, JWT(Json Web Token)를 통한 보안 인증 기능을 포함하고 있습니다.

## 기술 스택

-   **프로그래밍 언어**: Java
-   **프레임워크**: Spring Boot
-   **데이터베이스**: PostgreSQL
-   **인증**: JWT
-   **설정정보**: Spring Cloud Config Server

## 설치 방법

1. 이 저장소를 클론합니다:

```bash
git clone https://github.com/KwonNaRu/backend-naru.git
```

2. 클론한 디렉토리로 이동합니다:

```bash
cd backend-naru
```

3. Gradle로 의존성을 설치하고 애플리케이션을 실행합니다:

```bash
./gradlew bootRun
```

## 빌드 및 실행

이 프로젝트는 Docker를 사용하여 애플리케이션을 빌드하고 실행합니다. Dockerfile은 두 단계로 구성되어 있습니다:

### 빌드 단계

1. **Gradle 빌드**: `gradle:7.6-jdk17` 이미지를 사용하여 Gradle 환경을 설정합니다.
2. **작업 디렉토리 설정**: `/app` 디렉토리에서 소스 코드를 복사합니다.
3. **Gradle 빌드 실행**: `SPRING_PROFILES_ACTIVE=docker ./gradlew clean build --no-daemon --info` 명령으로 애플리케이션을 빌드합니다.

### 실행 단계

1. **OpenJDK 실행 환경 설정**: `openjdk:17-jdk-slim` 이미지를 사용하여 실행 환경을 설정합니다.
2. **애플리케이션 실행**: 빌드된 JAR 파일을 `app.jar`로 복사한 후, `java -jar app.jar` 명령으로 애플리케이션을 실행합니다.

## Docker Compose 설정

이 프로젝트는 Docker Compose를 사용하여 애플리케이션 서비스를 정의합니다. 아래는 `docker-compose.yml`의 주요 구성 요소입니다:

### 서비스

-   **app**:
    -   `image`: `naru-backend` 이미지를 사용하여 애플리케이션을 실행합니다.
    -   `container_name`: 컨테이너 이름을 `naru-backend`로 설정합니다.
    -   `build`:
        -   `context`: 현재 디렉토리에서 Dockerfile을 사용하여 이미지를 빌드합니다.
        -   `dockerfile`: 사용할 Dockerfile의 경로를 지정합니다.
    -   `ports`: 호스트의 8080 포트를 컨테이너의 8080 포트와 매핑합니다.
    -   `environment`: Spring 프로파일을 `docker`로 설정합니다.
    -   `networks`: `naru-net`이라는 외부 네트워크에 연결됩니다.

### 네트워크

-   **naru-net**: 외부에서 정의된 네트워크를 사용하여 서비스 간의 통신을 가능하게 합니다.

## 실행 방법

Docker Compose를 사용하여 애플리케이션을 실행하려면 다음 단계를 따르세요:

1. **Docker Compose 실행**: 프로젝트 디렉토리에서 다음 명령어를 입력합니다:

```bash
docker-compose up --build -d
```

이 명령어는 이미지를 빌드하고 컨테이너를 실행합니다.

2. **애플리케이션 접근**: 웹 브라우저에서 http://localhost:8080으로 이동하여 애플리케이션에 접근할 수 있습니다.

3. **중지 방법**: 실행 중인 컨테이너를 중지하려면, 터미널에서 CTRL+C를 누르거나 아래 명령어를 사용합니다:

```bash
docker-compose down
```
