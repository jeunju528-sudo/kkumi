# 진행 현황

새 세션 시작 시 이 파일부터 확인. 작업 끝날 때마다 갱신

## 기준일: 2026-10-03

### 끝난 것
- 기획: MVP 4개, 도메인 규칙 확정 (CLAUDE.md "도메인 규칙")
- 디자인: 시안 v0.2 화면 10개, `docs/design.md`, `docs/ui-spec.md`
- 백엔드: 엔티티 + Repository (member, stock, trade, apartment, house), SecurityConfig 뼈대
- 프론트: 시안 v0.2 화면 10개 (#4)
  - 라우팅, pages(목업 상수), 픽셀 스프라이트, 색 토큰, Galmuri 폰트, `utils/format.ts`, `types/api.ts`
  - pages 안의 `useState`·핸들러는 시안 재현용 임시 로직. 실제 데이터 연결 때 교체
- 규칙 자동 검사: ESLint 레이어 규칙, ArchUnit, Git 훅 (#3)

### 아직 안 한 것
- 백엔드 Service·Controller·DTO 전부 (Controller/Service 없음)
- 카카오 로그인 (OAuth)
- 시세 연동 (`StockPriceProvider` + KIS)
- 실거래가 배치 (`@Scheduled`)
- WebSocket 평가액 푸시
- 프론트 데이터 연결: TanStack Query 미설치, `src/api`·`src/hooks`·`src/ws` 비어 있음
- CI (GitHub Actions), 배포 (EC2 + RDS)

### 외부 대기 · 결정 필요
- 주식 시세: KIS에 시세 재배포 약관 문의 중 → 답변 전까지 `MockPriceProvider`로 개발, 답변 오면 KIS 구현체 or 공공데이터 종가로 결정
- 국토부 실거래가 API: 활용신청 완료 (키는 `.env`의 `MOLIT_SERVICE_KEY`)
- AWS 계정: 확인 필요 (CD 단계 전)
- CI: CLAUDE.md "자동 검사 > CI (TODO)" 내용대로 추가 예정. 노션 일정표 GitHub Actions 작업 메모에도 기록
- 자연어 아파트 검색: 10/17까지 MVP 4개가 실서버에서 동작하면 10/18~20에 추가, 아니면 런칭 후

### 다음 할 일 (순서)
1. CI (10/4): GitHub Actions — PR마다 backend `./gradlew test` + frontend `npm ci && npm run lint && npm run build`, 실패 시 merge 차단
2. CD + Hello World 배포 (10/4~10/5): Dockerfile, docker-compose, nginx → EC2 + RDS. main merge 시 자동 배포
3. 회원: 카카오 로그인 → 시드 선택 API (`POST /api/members/seed`)
4. 매수·매도 (심장 ①) — 테스트 먼저
5. 평가액 계산 (심장 ②) + WebSocket 푸시
6. 실거래가 배치 (심장 ③) → 추천 API
7. 집 사기 → 내 마을, 변신 단계
8. 프론트 데이터 연결 (TanStack Query, ws)
9. 10/17 게이트 체크 → 10/21~23 통합 테스트·버퍼 → 10/24 런칭
- 배포는 마지막 주에 몰지 않음. 기능은 PR merge 때마다 실서버에 바로 반영

## 참고
- 노션 일정표(간트): FRTR 페이지 > "꿈이 런칭 일정 (10/2 ~ 10/24)" DB
- Figma 시안 사본: 데스크톱 10개, 모바일 7개 (모바일 사기·팔기, 계약서, 내 마을은 Figma 무료 플랜 MCP 호출 한도로 미완). 기준은 Claude Design 시안 v0.2
- 화면 ↔ 파일 대응표: `frontend/README.md`
