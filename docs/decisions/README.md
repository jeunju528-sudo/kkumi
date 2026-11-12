# 기술 결정 기록 (ADR)

왜 이 방식을 선택했는지 남기는 곳. 작업 전에 관련 결정을 확인하고, 확정된 결정을 번복하는 제안은 하지 않는다.
양식은 `_template.md`, 작성 규칙은 `docs/README.md` 참고.

## 목록

| 번호 | 제목 | 상태 |
| --- | --- | --- |
| 001 | [CI/CD: Jenkins 대신 GitHub Actions](001-ci-cd-github-actions.md) | 확정 |
| 002 | [운영 DB: EC2 컨테이너 대신 RDS](002-database-rds.md) | 확정 |
| 003 | [실거래가 배치: Spring Batch 대신 @Scheduled](003-batch-scheduled.md) | 확정 |
| 004 | [테스트 DB: Testcontainers 대신 H2](004-test-database-h2.md) | 확정 |
| 005 | [운영 ddl-auto: update vs validate](005-production-ddl-auto.md) | 미정 |
| 006 | [규칙 강제: 문서만 두지 않고 코드로](006-rule-enforcement-by-code.md) | 확정 |
| 007 | [Git 훅 관리: core.hooksPath](007-git-hooks-core-hookspath.md) | 확정 |
| 008 | [백엔드 아키텍처 검사: ArchUnit](008-architecture-test-archunit.md) | 확정 |
| 009 | [CI 실행 범위: 항상 전체](009-ci-run-scope.md) | 확정 |
| 010 | [pre-commit 백엔드 검사 범위](010-precommit-backend-scope.md) | 확정 |
| 011 | [보유 현황: 현황 + 거래 이력 둘 다 저장](011-holding-and-trade-history.md) | 확정 |
| 012 | [매수/매도 테이블 통합](012-trade-table-unified.md) | 확정 |
| 013 | [회원/자금 통합](013-member-cash-unified.md) | 확정 |
| 014 | [아파트 구매 단위: 최근 거래 목록](014-apartment-purchase-unit.md) | 확정 |
| 015 | [아파트 시세 기준: 최근 6개월 실거래가](015-market-price-basis.md) | 확정 |
| 016 | [같은 집: 공유 구매](016-shared-house-purchase.md) | 확정 |
| 017 | [실거래가 배치: 증분(upsert)](017-batch-incremental-upsert.md) | 확정 |
| 018 | [거래 유니크 키](018-deal-unique-key.md) | 확정 |
| 019 | [금액 타입: long, 평균 매입가만 BigDecimal](019-money-type-long.md) | 확정 |
| 020 | [주식 시세 출처: 서버가 만드는 가상 시세 (KIS·공공데이터 포기)](020-stock-price-source.md) | 확정 |
| 021 | [범위: 프론트 직접 구현 우선](021-scope-frontend-first.md) | 확정 |
| 022 | [프론트 상태 관리: TanStack Query + Context](022-frontend-state-management.md) | 확정 |
| 023 | [자연어 검색: 필터 JSON](023-natural-language-search-filter-json.md) | 확정 |
| 024 | [새 기술 상한선](024-technology-ceiling.md) | 확정 |
| 025 | [매수 동시성: 비관적 락(Member 행)](025-buy-concurrency-pessimistic-lock.md) | 확정 |
| 026 | [이미지 저장소: GHCR(public)](026-image-registry-ghcr-public.md) | 확정 |
| 027 | [EC2 SSH 접근: 22번 공개 + 키 인증만](027-ec2-ssh-port-open-key-only.md) | 확정 |
| 028 | [SSH 배포 실행: 직접 ssh 명령](028-ssh-deploy-direct-ssh.md) | 확정 |
| 029 | [가상 시세 변동 방식: 랜덤 워크 + 평균 회귀 + 시장 공통 요인](029-virtual-price-movement.md) | 확정 |
| 030 | [카카오 로그인: oauth2-client 사용](030-kakao-login-oauth2-client.md) | 확정 |
| 031 | [인증 방식: JWT를 HttpOnly 쿠키로 전달](031-auth-jwt-http-only-cookie.md) | 확정 |
| 032 | [거래내역 화면: 별도 화면 (증권사 앱 형태)](032-trade-history-screen.md) | 확정 |
| 033 | [인증 쿠키 구성: 쿠키 2개 분리, Path 제한](033-auth-cookie-composition.md) | 확정 |
| 034 | [쿠키 인증 필터: 잘못된 토큰은 막지 않고 통과](034-cookie-auth-filter.md) | 확정 |
| 035 | [시드 선택 API: 중복 가입 409, signupToken 쿠키 삭제](035-seed-select-api.md) | 확정 |
| 036 | [테스트 범위: 돈 계산·도메인 규칙만 자동 테스트](036-test-scope.md) | 확정 |
| 037 | [보유 종목: 전량 매도 시 행 삭제, 거래 수량 1 이상](037-holding-delete-on-sell-all.md) | 확정 |
| 038 | [일정 재조정: 런칭 10/29, 범위 유지](038-schedule-launch-1029.md) | 폐기 (대체: 039) |
| 039 | [일정 재조정: 런칭 11/14, 범위 유지](039-schedule-launch-1114.md) | 확정 |
| 040 | [가상 시세 현재가: 메모리 + 1분 주기 DB 저장](040-virtual-price-snapshot.md) | 확정 |
| 041 | [가상 종목 카탈로그: 유형 배치, 시작가, 호가 단위, market 삭제](041-virtual-stock-catalog.md) | 확정 |
| 042 | [가상 시세 내부 가격: 반올림 전 값은 double, 밖으로는 long](042-virtual-price-internal-double.md) | 확정 |
| 043 | [화면 가드: 회원 조회 실패면 시작 화면](043-screen-guard-member-me.md) | 확정 |
| 044 | [평가액 계산: 조회(Service)와 계산(순수 클래스) 분리](044-portfolio-calculation-split.md) | 확정 |
| 045 | [작성 분담 조정: 백엔드는 새 개념만 직접, 반복 로직은 에이전트](045-work-split-react-focus.md) | 확정 |
| 046 | [WebSocket 푸시: 사용자별 평가액을 서버가 계산해서 개인 큐로](046-websocket-push-per-user-portfolio.md) | 확정 |
| 047 | [아파트 추천 조회: 원 단위 저장, 지역명 컬럼, 살 수 있는 집 비싼 순](047-apartment-recommend-query.md) | 확정 |
| 048 | [집 사기: 같은 회원 같은 거래 1번, 살 때 거래 재검증, 변신 단계는 내 마을 API](048-house-purchase-rules.md) | 확정 |
| 049 | [손익 차트: 1일 탭 없이 일별 평가액 기록 + 오늘 현재값, 심장 ④는 에이전트 작성](049-portfolio-daily-snapshot-chart.md) | 확정 |
| 050 | [플레이 점검 반영: 종목 정보는 숨기고 "오늘 마감" 시계·신규 CTA·HUD 현금 통일](050-player-hints-day-close.md) | 확정 |
| 051 | [AI 자산 코치: 런칭 후 추가, Spring AI + Gemini, 호출 한도는 Redis](051-ai-asset-coach.md) | 확정 |
| 052 | [실거래가 배치 수집: 전국 시군구 코드표, 매일 2개월 재수집, JSON 응답](052-apt-deal-batch-collection.md) | 확정 |
| 053 | [Kafka를 AI 코치보다 먼저 도입: 체결 이벤트 → 즉시 평가액 푸시](053-kafka-before-ai-coach.md) | 확정 |
| 054 | [순자산: 평가액 + 내 집 지분(구매가 40%), 홈 카드는 오늘 증감과 자산 구성](054-net-worth-house-equity.md) | 확정 |
| 055 | [홈 목표 아파트: 아직 못 사는 집 중 가장 싼 집](055-home-goal-apartment.md) | 확정 |

다음 번호: 056

## 미정 (아직 ADR로 만들지 않은 것)
결정이 나면 ADR로 만들고 이 목록에서 지운다.
