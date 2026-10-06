# 트러블슈팅

증상 → 원인 → 해결 → 배운 점

---

## 템플릿

### [날짜] 제목
- 증상:
- 원인:
- 해결:
- 배운 점:

---

## [2026-10-03] git push 거부: email privacy restrictions
- 증상: 새 환경에서 커밋 후 push 하니 `! [remote rejected] ... (push declined due to email privacy restrictions)`
- 원인: GitHub "Block command line pushes that expose my email" 설정이 켜져 있는데, 커밋 author 이메일이 개인 이메일로 들어감
- 해결: author를 GitHub noreply 이메일(`{id}+{username}@users.noreply.github.com`)로 바꿔 `git commit --amend --reset-author` 후 다시 push. 레포에 `git config user.email`을 noreply로 고정
- 배운 점: 새 PC·컨테이너에서 작업할 땐 첫 커밋 전에 `git config user.email` 확인. 개인 이메일이 공개 레포 커밋 기록에 남는 걸 막아주는 설정이라 끄지 않는 게 맞음

## [2026-10-06] RDS 보안 그룹 규칙 수정 에러 (CIDR → 보안 그룹 참조)
- 증상: RDS 보안 그룹(`kkumi-rds-sg`)에서 소스가 IP 대역(CIDR)인 3306 규칙을 EC2 보안 그룹 참조로 편집할 수 없다는 에러
- 원인: 규칙의 소스 유형은 편집으로 바꿀 수 없음
- 해결: 기존 규칙을 삭제하고, 소스를 `kkumi-ec2-sg`로 지정해 새로 추가. 이후 EC2에서 RDS 접속 확인
- 배운 점: DB는 IP가 아니라 EC2 보안 그룹을 허용 대상으로 두면 EC2 IP가 바뀌어도 규칙이 유지됨. 소스 유형을 바꿀 땐 규칙을 수정하지 말고 삭제 후 새로 추가
