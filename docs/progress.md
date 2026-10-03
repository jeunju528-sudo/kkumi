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

### 다음 할 일 (순서)
1. 회원: 카카오 로그인 → 시드 선택 API (`POST /api/members/seed`)
2. 매수·매도 (심장 ①) — 테스트 먼저
3. 평가액 계산 (심장 ②) + WebSocket 푸시
4. 실거래가 배치 (심장 ③) → 추천 API
5. 집 사기 → 내 마을, 변신 단계
6. 프론트 데이터 연결 (TanStack Query, ws)
7. CI/CD → 10-24 런칭

## 참고
- Figma 시안 사본: 데스크톱 10개, 모바일 7개 (모바일 사기·팔기, 계약서, 내 마을은 Figma 무료 플랜 MCP 호출 한도로 미완). 기준은 Claude Design 시안 v0.2
- 화면 ↔ 파일 대응표: `frontend/README.md`
