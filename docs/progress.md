# 진행 현황

새 세션 시작 시 이 파일부터 확인. 작업 끝날 때마다 갱신

## 기준일: 2026-10-07

### 끝난 것
- 기획: MVP 4개, 도메인 규칙 확정 (CLAUDE.md "도메인 규칙")
- 디자인: 시안 v0.2 화면 10개, `docs/design.md`, `docs/ui-spec.md`
- 백엔드: 엔티티 + Repository (member, stock, trade, apartment, house), SecurityConfig 뼈대
- 프론트: 시안 v0.2 화면 10개 (#4)
  - 라우팅, pages(목업 상수), 픽셀 스프라이트, 색 토큰, Galmuri 폰트, `utils/format.ts`, `types/api.ts`
  - pages 안의 `useState`·핸들러는 시안 재현용 임시 로직. 실제 데이터 연결 때 교체
- 규칙 자동 검사: ESLint 레이어 규칙, ArchUnit, Git 훅 (#3)
- CI: GitHub Actions `.github/workflows/ci.yml` (main 대상 PR마다 backend `./gradlew test` + frontend `npm ci`, lint, build)
  - GitHub 브랜치 보호 규칙에 필수 체크 `backend`, `frontend` 등록 완료
- 배포 준비: `backend/Dockerfile`, `frontend/Dockerfile` + `nginx.conf`, 운영용 `docker-compose.yml` (앱 + nginx, DB는 RDS), CI `docker` 잡
- CD 1단계: `.github/workflows/deploy.yml` (main push 시 backend·nginx 이미지를 빌드해 GHCR에 push, 이미지 public. `decisions/026`, PR #12). main push 시 Actions 성공 확인
- AWS 인프라: EC2(Ubuntu, t3.small, Elastic IP 연결) + RDS MySQL(퍼블릭 접근 없음, 3306은 EC2 보안 그룹에서만 허용)
  - EC2에서 RDS 접속 확인, Docker·Compose 설치 확인(`hello-world` 실행)
  - EC2에 레포 clone + `.env`(DB 정보, 이미지 이름) 작성, `docker compose config` 정상 확인
  - 절차는 노션 "7. AWS + CI/CD 진행 절차"
- CD 2단계: `deploy.yml`에 `deploy` 잡 추가 (build-and-push 성공 후 EC2에 ssh 접속 → `git pull` → `docker compose pull` → `docker compose up -d`, PR #15)
  - 22번 포트 공개 + 키 인증만, 배포 전용 키 분리 (`decisions/027`), 외부 액션 없이 직접 ssh 명령 (`decisions/028`)
  - Secrets: `EC2_HOST`, `EC2_USER`, `EC2_SSH_KEY`
- Hello World: 외부에서 `/actuator/health` 가 `UP` 확인 (2026-10-06)
- 지식 저장소 정비: `docs/`를 `decisions/`(ADR 29개), `conventions/`, `domain/`, `failures/`로 재구성. 구조·작성 규칙은 `docs/README.md`, CLAUDE.md "참고 문서"에 연결
- 카카오 로그인 연결: Spring Security oauth2-client + `oauth2Login()` (`decisions/030`)
  - nginx에 `/oauth2/`, `/login/oauth2/` 전달 추가, `forward-headers-strategy: framework`, compose에 `KAKAO_CLIENT_ID`·`KAKAO_CLIENT_SECRET` 전달
  - 로컬에서 카카오 동의 화면과 로그인 후 리다이렉트까지 확인. 실서버 확인은 아직 (EC2 `.env`에 두 값 필요)

### 아직 안 한 것
- 백엔드 Service·Controller·DTO 전부 (Controller/Service 없음)
- 카카오 로그인 후속: 로그인 성공 처리(JWT 발급), 신규·기존 회원 구분
- 시세 연동 (`StockPriceProvider` 구현체: 가상 시세. 지금은 `MockPriceProvider`)
- 실거래가 배치 (`@Scheduled`)
- WebSocket 평가액 푸시
- 프론트 데이터 연결: TanStack Query 미설치, `src/api`·`src/hooks`·`src/ws` 비어 있음

### 외부 대기 · 결정 필요
- 주식 시세: KIS와 공공데이터 주식시세정보 모두 서비스 사용 불가 확인(2026-10-06) → 서버가 만드는 가상 시세로 결정 (`decisions/020`, `failures/001`, `failures/002`). 개발 중에는 `MockPriceProvider` 사용
  - 변동 방식은 `decisions/029`로 확정, 숫자는 구현 후 플레이 테스트로 조정
  - 정할 것: 게임 소개 문구(실제 종목 → 가상 종목), 현재가 저장·복구 방식 (`decisions/README.md` "미정")
  - 정리 필요: `application.yaml`, `docker-compose.yml`의 KIS 키 설정 (별도 PR)
- 국토부 실거래가 API: 활용신청 완료 (키는 `.env`의 `MOLIT_SERVICE_KEY`). 이용허락범위 제한 없음 확인 (2026-10-06)
- AWS: EC2·RDS 운영 중. 후속 과제로 런칭 후 SSM 전환 검토 (22번 포트 닫기, `decisions/027`)
- 자연어 아파트 검색: 10/17까지 MVP 4개가 실서버에서 동작하면 10/18~20에 추가, 아니면 런칭 후

### 다음 할 일 (순서)
1. 회원: 로그인 성공 후 JWT 발급 → 시드 선택 API (`POST /api/members/seed`)
2. 매수·매도 (심장 ①) — 테스트 먼저
3. 시세 제공자: 가상 시세 구현체 (ADR 029 기준)
4. 평가액 계산 (심장 ②) + WebSocket 푸시
5. 실거래가 배치 (심장 ③) → 추천 API
6. 집 사기 → 내 마을, 변신 단계
7. 프론트 데이터 연결 (TanStack Query, ws)
8. 10/17 게이트 체크 → 10/21~23 통합 테스트·버퍼 → 10/24 런칭
- 배포는 마지막 주에 몰지 않음. 기능은 PR merge 때마다 실서버에 바로 반영

## 참고
- 노션 일정표(간트): FRTR 페이지 > "꿈이 런칭 일정 (10/2 ~ 10/24)" DB
- Figma 시안 사본: 데스크톱 10개, 모바일 7개 (모바일 사기·팔기, 계약서, 내 마을은 Figma 무료 플랜 MCP 호출 한도로 미완). 기준은 Claude Design 시안 v0.2
- 화면 ↔ 파일 대응표: `frontend/README.md`
- 결정 이유·실패 기록·용어: `docs/decisions/`, `docs/failures/`, `docs/domain/` (목록은 `docs/README.md`)
