# 005. 운영 ddl-auto: update vs validate

## 상태
미정 (런칭 직전에 결정)

## 배경
운영(prod) 프로필의 `ddl-auto` 값을 정해야 함. 현재 `application.yaml`의 prod는 `update`이고 "런칭 전 validate 전환 검토" TODO가 있음.

## 후보
- update: 현재 prod 값
- validate: 런칭 전 전환을 검토 중 (`application.yaml` TODO)
- 두 후보의 구체적인 장단점은 기록 없음

## 결정
미정. 런칭 전까지는 `update`를 쓰고, 런칭 직전에 결정한다.

## 결과
확정 전까지 에이전트는 prod 프로필에 `create` / `create-drop`을 절대 쓰지 않는다 (CLAUDE.md 절대 금지). `validate` 전환은 런칭 직전에 개발자와 논의한다.
