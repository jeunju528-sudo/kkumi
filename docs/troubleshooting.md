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

## [2026-10-07] CI 테스트 실패: contextLoads NoSuchBeanDefinitionException (카카오 로그인 연결 후)
- 증상: `oauth2Login()`을 추가한 PR의 CI backend 잡에서 `BackendApplicationTests > contextLoads()` 실패. 로컬 앱 기동은 정상
- 원인: `src/test/resources/application.yaml`이 main `application.yaml`을 대체해서 로드됨. 테스트 설정에 카카오 registration이 없어 `oauth2Login()`이 필요로 하는 클라이언트 등록 정보 빈이 만들어지지 않음. 로컬 앱은 main yaml을 읽어서 문제가 없었음
- 해결: 테스트 yaml에 카카오 registration과 provider를 더미 값(`test`)으로 추가. CI에 환경변수를 넣는 방법은 테스트 설정에 값을 받을 항목이 없어서 효과가 없음
- 배운 점: main 설정을 바꾸면 테스트 설정(`src/test/resources`)도 같이 확인할 것. 로컬 실행 성공과 테스트 컨텍스트 로딩 성공은 서로 다른 설정을 읽음. 에러 원인 체인은 가장 아래 `Caused by`부터 읽기
