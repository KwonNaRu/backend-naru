# Docker

### Dockerfile로 jar 파일 도커 이미지 생성

```
docker build -t naru-backend .
```

- -t your-docker-image-name: 생성될 Docker 이미지의 이름을 지정합니다.
- .: 현재 디렉토리를 빌드 컨텍스트로 사용한다는 의미입니다.
  Docker가 Dockerfile에 정의된 지침에 따라 이미지를 생성합니다.

### docker-compose.yml 에 실행할 도커 이미지 설정

```
docker-compose up
```
