# 028. SSH 배포 실행: SSH 전용 외부 액션 대신 직접 ssh 명령

## 상태
확정 (2026-10-06)

## 배경
CD 2단계에서 GitHub Actions가 EC2에 SSH로 접속해 `git pull`, `docker compose pull`, `docker compose up -d`를 실행한다 (026, 027). 이 접속을 SSH 전용 외부 액션(예: appleboy/ssh-action)으로 할지, 러너에서 `ssh` 명령을 직접 실행할지 정해야 했다.

## 결정
외부 액션 없이 러너에서 `ssh` 명령을 직접 실행한다. 개인키는 Secrets(`EC2_SSH_KEY`)에서 환경변수로 받아 `~/.ssh`에 파일로 쓰고(권한 600), `ssh-keyscan`으로 EC2 host key를 `known_hosts`에 등록한 뒤 `ssh -i`로 접속한다.

## 이유
- 배포 전용 개인키를 외부 개발자가 만든 액션에 넘기지 않음. 기존 워크플로는 공식 액션(`actions/*`, `docker/*`)만 사용
- 키 파일 생성, host key 등록, 원격 명령 실행의 동작 원리를 개발자가 직접 설명할 수 있음

## 포기한 대안
- SSH 전용 외부 액션(appleboy/ssh-action 등): 몇 줄로 끝남 / 개인키가 외부 액션에 전달됨. 쓴다면 버전 고정이 필요. 포기
- `StrictHostKeyChecking=no`로 host key 확인 끄기: 등록 스텝이 줄어듦 / 아무 서버나 신뢰하게 됨. 쓰지 않음

## 결과
키 파일 생성·host key 등록 스텝이 늘어 워크플로가 길어지고 실수할 지점이 늘어남 (감수). 스텝마다 셸이 새로 시작되므로 `cd`가 이어지지 않아 키 파일은 전체 경로(`~/.ssh/...`)로 쓰고 읽는다. `deploy.yml`은 main push에서만 동작해 PR 단계에서는 배포를 시험할 수 없음 (감수).
이 결정을 번복하려면 먼저 개발자와 논의 필요. 에이전트는 개인키를 외부 액션에 넘기는 방식과 host key 확인을 끄는 옵션(`StrictHostKeyChecking=no`)을 먼저 제안하지 않는다. 관련: 026, 027.
