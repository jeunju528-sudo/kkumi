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

다음 번호: 032

## 미정 (아직 ADR로 만들지 않은 것)
결정이 나면 ADR로 만들고 이 목록에서 지운다.
- 가상 시세 현재가 저장·복구 방식 (메모리 vs DB 스냅샷) (ADR 029 후속)
- 가상 종목별 유형 배치와 시작가 (ADR 029 후속, `domain/stocks.md`)
- 시세 캐싱 TTL
- WebSocket: 전체 브로드캐스트 vs 보유 종목만 푸시
- 프론트 실시간: 메시지마다 리렌더 vs throttle
