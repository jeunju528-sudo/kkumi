# 010. pre-commit 백엔드 검사 범위: backend/ 전체 대신 소스·빌드 설정만

## 상태
확정 (2026-10-03)

## 배경
`backend/Dockerfile`을 커밋하려는데 pre-commit 훅이 `./gradlew test`를 돌렸음. Gradle 다운로드가 막힌 환경(클라우드 세션)에서는 이 훅을 통과할 수 없어 커밋이 불가능했음.

## 결정
`backend/src/`, `build.gradle`, `settings.gradle`, `gradle.properties`, `gradle/`, `gradlew*` 변경이 있을 때만 `./gradlew test`를 돌린다. `Dockerfile`, `.dockerignore` 같은 인프라 파일은 제외한다.

## 이유
- 훅은 로컬 1차 방어선일 뿐이고, CI가 PR마다 전체를 다시 검사함
- 테스트 결과에 영향이 없는 파일 때문에 커밋이 막히는 비용이 더 큼

## 포기한 대안
- backend/ 전체 검사: 규칙이 단순하고 놓치는 파일이 없음 / Java와 무관한 파일 커밋에도 `gradlew test`가 돌고, Gradle이 안 되는 환경에서는 커밋 자체가 막힘

## 결과
대상 경로 목록을 관리해야 함. 새 빌드 설정 파일이 생기면 `.githooks/pre-commit`에 추가한다 (감수). Gradle이 안 되는 환경에서는 `backend/src` 변경을 커밋할 수 없으므로 Gradle이 되는 환경(은주 로컬)에서 커밋한다.
이 결정을 번복하려면 먼저 은주와 논의 필요. 에이전트는 `--no-verify`로 훅을 우회하지 않는다. 관련: 006, 007, 009.
