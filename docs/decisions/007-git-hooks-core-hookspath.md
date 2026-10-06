# 007. Git 훅 관리: core.hooksPath 사용

## 상태
확정 (2026-10-03)

## 배경
Java + Node 모노레포에서 커밋 전 검사(훅)를 관리할 방법이 필요했음.

## 결정
`.githooks/` 디렉토리를 만들고 `core.hooksPath`로 지정한다. 클론 후 최초 1회 `git config core.hooksPath .githooks`가 필요하다.

## 이유
- Java + Node 모노레포라 특정 언어 도구에 묶이지 않는 편이 나음
- 추가 도구 없이 셸 스크립트만으로 구성 가능
- 훅은 `--no-verify`로 우회할 수 있어서 CI에서 같은 검사를 한 번 더 돌림

## 포기한 대안
- pre-commit(Python): 훅 생태계가 다양함 / Python 설치 필요
- husky: 프론트에서 많이 씀 / 루트 `package.json` 필요, Node 의존

## 결과
클론 후 설정 1회가 필요함 (감수).
이 결정을 번복하려면 먼저 개발자와 논의 필요. 에이전트는 pre-commit, husky 도입을 제안하지 않는다. 관련: 006, 009, 010.
