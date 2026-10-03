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
