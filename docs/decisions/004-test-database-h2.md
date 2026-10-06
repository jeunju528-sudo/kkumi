# 004. 테스트 DB: Testcontainers 대신 H2(MySQL 모드) 사용

## 상태
확정 (2026-10-03)

## 배경
CI와 로컬에서 MySQL 없이 테스트를 돌려야 했음 (`./gradlew test`는 DB 없이 실행).

## 결정
H2를 MySQL 모드로 사용한다. 설정은 `backend/src/test/resources/application.yaml`.

## 이유
- 의존성이 하나뿐이고 CI에서 Docker가 필요 없음
- 지금은 컨텍스트 로딩 테스트 수준이라 운영 DB와의 차이가 문제되지 않음

## 포기한 대안
- Testcontainers: 운영과 같은 MySQL을 쓸 수 있음 / Docker 필요

## 결과
MySQL과 동작이 다를 수 있음 (감수). 네이티브 쿼리가 생기면 다시 검토한다.
이 결정을 번복하려면 먼저 개발자와 논의 필요. 에이전트는 네이티브 쿼리를 추가할 때 H2와의 동작 차이를 먼저 알린다.
