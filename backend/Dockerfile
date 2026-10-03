# 빌드 스테이지
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 의존성 레이어 캐시 (소스 바뀌어도 재다운로드 X)
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN ./gradlew dependencies --no-daemon > /dev/null 2>&1 || true

COPY src src
# 테스트는 CI에서 돌림
RUN ./gradlew bootJar -x test --no-daemon \
    && cp "$(ls build/libs/*.jar | grep -v plain | head -n 1)" app.jar

# 실행 스테이지
FROM eclipse-temurin:21-jre
WORKDIR /app

# 컴포즈 healthcheck용
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# 루트 실행 금지
RUN useradd --system --no-create-home kkumi
USER kkumi

COPY --from=build /app/app.jar app.jar

ENV TZ=Asia/Seoul
# 작은 인스턴스 기준 메모리 비율
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
