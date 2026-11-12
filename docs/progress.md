# 진행 현황

새 세션 시작 시 이 파일부터 확인. 작업 끝날 때마다 갱신

## 기준일: 2026-11-12

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
- `SecurityConfig` 인증 규칙 (2026-10-13): 공개 경로(`/oauth2/**`, `/login/oauth2/**`, `/api/members/seed`, `/actuator/health/**`, `/error`) 외 `authenticated()`. 미인증은 `HttpStatusEntryPoint`로 401 (oauth2Login 기본값 302 대신)
  - 로컬 curl 확인: 막힌 경로 401, 쿠키 없는 시드 선택 400(`/error` 공개로 401에 안 덮임), health 200, 로그인 시작 302 → kauth
  - `/ws`는 인증 필요(`anyRequest().authenticated()`), handshake에 실린 accessToken 쿠키로 기존 필터가 인증 (2026-11-06, `decisions/046`)
- 테스트 범위 조정 (2026-10-13, `decisions/036`): 돈 계산(심장 ①②③)·도메인 규칙만 자동 테스트, 단순 Service·Controller는 화면 연결 때 수동 확인
- 지식 저장소 정비: `docs/`를 `decisions/`(ADR 29개), `conventions/`, `domain/`, `failures/`로 재구성. 구조·작성 규칙은 `docs/README.md`, CLAUDE.md "참고 문서"에 연결
- 시세 출처 확정 (2026-10-06): KIS·공공데이터 모두 서비스 사용 불가 → 서버가 만드는 가상 시세와 가상 종목. `decisions/020`, `029`, `failures/001`, `002` (#17)
- 프론트 목업 종목을 가상 종목 20개로 교체 (#18). 목록은 `domain/stocks.md`
- 3-3 내 거래 화면 추가 (2026-10-07, ADR 032): 시안 보드 + `TradeHistoryPage`(목업), 투자·홈에서 진입 링크, `TradeHistoryResponse` 타입
- 가상 시세 초기값 확정 (2026-10-07): 변동 배율 4.0, 평균 회귀 반감기 0.5일. 시뮬레이션 근거는 `decisions/029` (#20)
- 카카오 로그인 연결: Spring Security oauth2-client + `oauth2Login()` (`decisions/030`)
  - nginx에 `/oauth2/`, `/login/oauth2/` 전달 추가, `forward-headers-strategy: framework`, compose에 `KAKAO_CLIENT_ID`·`KAKAO_CLIENT_SECRET` 전달
  - 로컬과 실서버(EC2)에서 카카오 로그인 후 홈 리다이렉트까지 확인 (2026-10-07)
- JWT 토큰 생성·검증: `global/jwt/JwtProvider` (Nimbus, HS256), 정식 토큰(memberId)·임시 토큰(kakaoId, 닉네임) 구분은 payload `type` claim (`decisions/031`)
  - `JwtProviderTest` 6개: 왕복, 만료, 위조, 토큰 종류 불일치
  - `JWT_SECRET` 환경변수 필수 (로컬 IntelliJ·`.env`, EC2 `.env`, compose `:?`)
  - 만료 시간: 정식 3시간, 임시 10분
- 로그인 성공 핸들러 (2026-10-12, `decisions/033`): `member/LoginSuccessHandler`, `global/jwt/AuthCookie`, `MemberService.findMemberIdByKakaoId`(테스트 2개)
  - 신규 회원은 `signupToken` 쿠키(Path `/api/members/seed`) 발급 후 `/seed`, 기존 회원은 `accessToken` 쿠키 발급 후 `/home`
  - 로컬에서 신규 회원 분기 확인(쿠키 속성 HttpOnly·Lax·10분). 기존 회원 분기(`accessToken` 삭제 후 재로그인 → 바로 `/home`) 확인 (2026-10-14)
- 쿠키 인증 필터 (2026-10-13, `decisions/034`): `global/security/CookieAuthenticationFilter`가 `accessToken` 쿠키로 memberId를 `SecurityContextHolder`에 등록, `SecurityConfig`에 `addFilterBefore`로 등록
  - 쿠키 없음·잘못된 토큰·만료·signupToken은 인증 없이 통과. `CookieAuthenticationFilterTest` 6개
  - 인증 규칙은 `SecurityConfig`에서 적용 (아래)
- 시드 선택 API (2026-10-13): `POST /api/members/seed`. `MemberController`가 `signupToken` 쿠키에서 kakaoId·닉네임을 꺼내 `MemberService.join`으로 가입, `accessToken` 쿠키 발급 + `signupToken` 쿠키 삭제(`AuthCookie.expireSignupCookie`)
  - 이미 가입한 kakaoId는 `ALREADY_JOINED`(409)
  - `MemberServiceTest`에 `join` 성공·중복(`ALREADY_JOINED`) 테스트 2개 (2026-10-13). 409 응답·쿠키는 시드 선택 화면 연결 때 수동 확인 (`decisions/035`, `036`)
- 예외 → HTTP 응답 변환 (2026-10-13): `global/error/GlobalExceptionHandler`(`@RestControllerAdvice`)가 `BusinessException`을 `ErrorCode.status` + `ErrorResponse`(`code`, `message`)로 변환
  - `code`는 enum 이름(`ALREADY_JOINED` 등). 상태 코드는 HTTP 응답에만 두고 바디에는 넣지 않음
  - 필터에서 던진 예외는 `DispatcherServlet` 앞이라 대상 아님
  - 409 응답 확인은 시드 선택 API 테스트(MockMvc)에서
- 개발 프록시 (2026-10-14): `vite.config.ts`에서 `/api`, `/oauth2`, `/login/oauth2` → 8080, `changeOrigin: false`로 Host 유지. curl로 redirect_uri 5173 확인. 카카오 Redirect URI에 5173 주소 추가
- TanStack Query 준비 (2026-10-14): `@tanstack/react-query` 설치, `main.tsx`에서 `QueryClient`(컴포넌트 밖 1회 생성) + `QueryClientProvider`로 앱 감쌈. 옵션은 기본값
- 작성 방식 변경 (2026-10-14): 프론트 동작은 Claude가 대화창에 소스 제시 → 개발자 직접 입력 → 검토. 심장은 기존대로 (CLAUDE.md "절대 금지"·"프론트 분담")
- 시드 선택 화면 연결 (2026-10-14): `api/apiError.ts`(실패 응답 → `ApiError{status, code}`, 타입 가드), `api/memberApi.ts`(`selectSeed`), `hooks/useSelectSeedMutation.ts`, `SeedPickPage`(`mutate`, 요청 중 버튼 잠금), `WelcomePage` 카카오 버튼 → `/oauth2/authorization/kakao`
  - 실패 처리: 성공·`ALREADY_JOINED` → `/home`, 400·401(signupToken 없음·만료) → `/`, 그 외 → 안내 문구
  - 브라우저 수동 확인: 신규 로그인 → `/seed` → 가입 200 → `accessToken` 발급·`signupToken` 삭제 → `/home`, DB `member` 행(initial_cash·cash 500만) 확인, 같은 signupToken 재제출 curl → 409 `ALREADY_JOINED` (`decisions/035`, `036`)
- 매수·매도 (심장 ①) 1부 (2026-10-15, `feat/stock-buy-sell` 브랜치, PR 전): `Member.withdraw`·`deposit`, `StockHolding` 매수·매도 계산, `MemberTest`·`StockHoldingTest`, `BuyStockRequest`·`SellStockRequest`
  - 전량 매도 시 보유 행 삭제, 거래 수량 1 이상 (`decisions/037`)
  - `TradeServiceTest`는 메서드 이름 뼈대 6개만(`@Disabled("작성 전")`)
- 매수·매도 (심장 ①) 2부 (2026-10-26~27, PR #38 merge, 실서버 배포·확인 10/27)
  - `TradeController`: `POST /api/trades/buy`, `/sell`. `@Valid @RequestBody` + `@AuthenticationPrincipal Long memberId`(쿠키 필터의 principal). 응답 바디 없음(화면은 성공 후 쿼리 invalidate)
  - `TradeService`: 회원 비관적 락(`MemberRepository.findByIdForUpdate`, `decisions/025`) → 종목 → 보유 → 현재가 → 금액(`Math.multiplyExact`) → 현금 차감·입금 → 보유 갱신(매수: 있으면 `buy`, 없으면 `create` + save / 매도: 수량 0이면 delete, `decisions/037`) → 이력 save
  - 매수 순서: 현금 차감 먼저, 보유 갱신 나중. 매도 순서: 보유 차감 먼저(INSUFFICIENT_HOLDING 검사), 입금 나중. 입금액은 현재가 × 수량(실현손익은 이력에만)
  - 추가: `TradeHistory.ofBuy`·`ofSell`(거래 종류·시각은 팩토리 안에서), `StockHoldingRepository.findByMemberAndStock`, `ErrorCode.MEMBER_NOT_FOUND`·`STOCK_NOT_FOUND`(404). 보유 없는 종목 매도는 INSUFFICIENT_HOLDING(400)
  - `TradeServiceTest` 6개 통과 (Mockito): 매수 성공(현금·수량·평균 매입가·이력), 현금 부족(상태 그대로, 이력 never), 매도 성공(현금·수량, 실현손익은 ArgumentCaptor), 전량 매도 delete, 보유 없음, 락 조회(findByIdForUpdate times(2), findById never). 전체 50개 통과
  - 임시 시세 `stock/FixedPriceProvider`(모든 종목 10,000원, `@Component`): 구현체가 없어 `contextLoads`가 StockPriceProvider 빈 없음으로 실패해서 추가. 가상 시세 구현 때 이 파일 삭제(같은 타입 빈 2개면 기동 실패)
  - 로컬 수동 확인 (2026-10-27): 브라우저 콘솔 fetch(로그인 쿠키)로 매수·매도·전량 매도 200(보유 행 삭제), 현금 부족 400 `INSUFFICIENT_CASH`, 없는 종목 404 `STOCK_NOT_FOUND`, 보유 없음 400 `INSUFFICIENT_HOLDING`, 수량 0 400(`@Min`), 쿠키 없음 401. DB 현금 원복, 이력은 성공 3건만(실패 요청은 롤백). 쿼리 로그에서 회원 조회 `for update of m1_0` 확인
  - 확인용 종목 2개(KK0001·KK0002)는 로컬 DB에 SQL로 직접 넣었다가 카탈로그 작업 때 정리 (10/27)
  - 실서버 (10/27): 카카오 로그인 → 시드 선택 → 홈 확인, 수량 0 매수 400 응답에 `trace` 없음(로컬 `trace`는 devtools 영향)
- 가상 종목 카탈로그 (2026-10-27, 시세 PR 1/3, PR #39 merge, 실서버 20개 등록 확인): `decisions/040`, `041`
  - `StockType`(STABLE, ROLLER_COASTER, JACKPOT), `Stock.type`·`basePrice`(시작가 = 평균 회귀 기준가), `Stock.of(code, name, type, basePrice)`
  - `Market` enum·`Stock.market` 삭제. `ddl-auto: update`가 컬럼을 지우지 않아 로컬·RDS에서 `ALTER TABLE stock DROP COLUMN market` 직접 실행
  - `StockDataInitializer`(`ApplicationRunner`): 서버 시작 시 20개 중 DB에 없는 코드만 등록. 로컬 확인: 첫 실행 20개 추가, 재시작 0개 추가
  - 테스트의 `Stock.of` 호출 17곳 시그니처만 변경 (계산·검증 그대로)
  - 메모: MySQL에서 `type` 컬럼이 `varchar`가 아니라 `enum('JACKPOT','ROLLER_COASTER','STABLE')`로 생성됨. 나중에 enum 상수를 추가하면 `ddl-auto: update`가 컬럼을 바꾸지 않으므로 직접 ALTER 필요
- 가상 시세 엔진 (2026-10-27, 시세 PR 2/3, PR #40 merge, 실서버 확인 10/27): `decisions/042`(엔진 내부 반올림 전 가격은 `double`, 밖으로는 `long`)
  - `stock/PriceRules`: 호가 단위(가격대별 표), 호가 반올림, 상한가(+30% 내림)·하한가(-30% 올림). `PriceRulesTest` 23개(가격대 경계, 1원 미만 예외, 반올림, 상·하한)
  - `stock/VirtualPriceProvider`(`StockPriceProvider` 구현체): 종목별 상태 `PriceState` record를 `ConcurrentHashMap`에 보관, `@Scheduled(fixedRate = 5000)`로 전 종목 갱신, `ApplicationReadyEvent`에서 DB 시작가로 메모리 채움
  - 변동 공식(ADR 029 초기값 상수): 랜덤(√0.3 시장 + √0.7 종목, 5초 변동성 = 하루 변동성 ÷ √17,280) + 평균 회귀(반감기 0.5일) + 한방형 급등락(하루 0.3회, 15~40%) → `exp` → ±30% 자르기 → 호가 반올림
  - `global/config/SchedulingConfig`(`@EnableScheduling`), `FixedPriceProvider` 삭제
  - 이번 PR 범위 밖: 오늘 시작가(`dayOpenPrice`)는 시작가와 같은 값으로 고정, 날짜 갱신·DB 저장·복구·상수 yaml 이동은 PR 3. 엔진 결과 분포는 자동 테스트 없음(랜덤, 플레이 테스트로 확인)
  - 로컬 확인: 로그 `가상 시세 시작: 20개 종목`, KK0001을 10초 간격 6회 매수 시 체결가 51,600 → 51,800 → 51,900 → 51,800 → 52,000 → 52,200 (100원 호가 단위로 변동)
  - 실서버 (10/27): 로그 `가상 시세 시작: 20개 종목`, KK0001 체결가 50,100~50,400 100원 단위로 변동
- 시세 저장·복구·종목 조회 API (2026-10-28~29, 시세 PR 3/3, PR #41 merge, 실서버 배포 10/29): `decisions/040`(보강)
  - `Stock`에 `currentPrice`·`dayOpenPrice`(`Long`, null = 저장 전)·`priceDate` 컬럼, `updatePrice`. `ddl-auto: update`로 nullable 컬럼 3개 추가 확인(`DESC stock`)
  - 변동 상수 → `application.yaml` `virtual-price.*` + `VirtualPriceProperties` record. 갱신 주기는 `fixedRateString`, 하루 갱신 횟수는 `ticksPerDay()`로 계산. 테스트 yaml에도 같은 블록
  - 재시작 복구: 저장본이 있으면 그 값에서 시작(없으면 `basePrice`), 저장 날짜가 오늘(KST)이 아니면 저장된 현재가를 오늘 시작가로. `tick` 첫 단계에서 날짜가 바뀌면 전 종목 오늘 시작가 = 현재가
  - 1분 저장: `saveSnapshot` → `StockPriceSnapshotService.savePrices`(변경 감지 UPDATE). Service로 뗀 이유는 `StockService → StockPriceProvider`와의 순환 참조 방지. 종료 직전 저장(`ContextClosedEvent`)
  - `GET /api/stocks`: 코드순 + `getSnapshot`(현재가·오늘 시작가를 한 record에서) → `StockResponse{code, name, price, changeRate}`. `changeRate`는 `BigDecimal` 소수 4자리 HALF_UP, 오늘 시작가 대비
  - 로컬 확인 (10/29): 저장본 없을 때 `저장 시세 복구: 0개`, KK0001 어제 저장본 → `1개`, 1분 뒤 `[scheduling-1] 시세 저장: 20개 종목` + DB 값 채워짐, 재시작 → `20개` 복구. `jcmd`로 스케줄러 스레드 1개 확인. `GET /api/stocks` 20개 코드순, `changeRate` 숫자(예: KK0001 50,100원 -0.0176)
  - 실서버 (10/29): 로그 `저장 시세 복구: 0개 종목`(첫 배포라 저장본 없음), RDS `stock`에 컬럼 3개 null 허용으로 추가 확인
  - 실서버 (10/30): 자정 `[scheduling-1] 오늘 시작가 갱신: 2026-10-30`(00:00:03). `docker compose restart backend` 때 `[SpringApplicationShutdownHook] 시세 저장: 20개 종목`(종료 직전 저장) → `[main] 저장 시세 복구: 20개 종목, 기준일 2026-10-30`. 재시작 공백 약 17초
  - 기동 직후 `오늘 시작가 갱신` 오탐 (10/29 16:01 실서버 로그): `today`가 null인 채 첫 `tick`이 `loadStocks`(ApplicationReadyEvent)보다 먼저 돌아 날짜 변경으로 판단. 그 시점엔 종목 상태가 비어 있어 값 변화 없음. `today`를 선언 때 `LocalDate.now(KST)`로 초기화 (`fix/price-today-init`, `troubleshooting.md`)
  - 못 확인한 것: 쿠키 없는 `GET /api/stocks` 401 (기존 `SecurityConfig` 규칙 적용 대상)
- 회원 조회 API (2026-10-29, PR #42 merge 10/29): `GET /api/members/me` → `MemberResponse{id, nickname, cash}`(`MemberService.getMyInfo`, 없으면 `MEMBER_NOT_FOUND` 404). `decisions/043`
  - 화면 가드 규칙 변경: 회원 조회 실패면 `/`. 시드 미선택 상태는 회원 행이 없고 `signupToken`(Path `/api/members/seed`)이 `/me`에 실리지 않아 구분하지 않음. `types/api.ts` `MemberResponse`에서 `hasSelectedSeed` 삭제, `tier`·`houseCount`는 집 사기 작업 때 추가. `docs/ui-spec.md` 가드 줄 교체
  - `MemberController`의 안 쓰는 import(`ObservationProperties`) 삭제
  - 로컬 확인 (10/29): 로그인 상태 200 `{id, nickname, cash}`, 쿠키 없음 401. 프론트 `eslint`, `tsc -b` 통과
- 화면 가드 (2026-10-29, PR #43 merge 10/29): `decisions/043`
  - `hooks/queryKeys.ts`(`member: () => ['member', 'me'] as const`), `api/memberApi.getMyInfo`, `hooks/useMemberQuery`(`retry: false`, 401 재시도 대기 방지), `pages/MemberGuard`(로딩 null, 실패 `/` replace, 성공 `Outlet`)
  - `App.tsx`: `/`, `/seed`를 뺀 9개 라우트를 path 없는 `<Route element={<MemberGuard />}>`로 감쌈
  - 로컬 확인 (10/29): 로그인 상태 `/home` 직접 접속 → 홈, `me` 200 1번 / 시크릿 창 `/home` → `/`, `me` 401 1번(재시도 없음) / `/`, `/seed`는 시크릿 창에서 열림 / `npm run lint`, `npm run build` 통과
  - 실서버 (10/29): 시크릿 창 `/home` → `/` 이동 확인
- 보유 종목 조회 API (2026-10-30, `feat/holding-query-api`): `GET /api/holdings` → `HoldingResponse{code, name, quantity, avgPrice, price}`(price는 메모리 현재가, avgPrice만 `BigDecimal`)
  - `trade/HoldingService`(읽기 전용, `TradeService`와 분리), `HoldingController`, `StockHoldingRepository.findAllByMemberIdWithStock`(`join fetch h.stock`으로 N+1 방지, `member.id` 조건이라 회원 조회 없음, 코드순)
  - 로컬 확인 (10/30): 로그인 상태 KK0001 12주 1건(`avgPrice` 51550, `price` 55500). 보유 없으면 `[]`
- 종목 목록·검색 화면 연결 (2026-10-30, `feat/trade-page-connect`, 종목·매매 연결 PR A/B 중 A)
  - `api/stockApi.getStocks`, `api/holdingApi.getHoldings`, `queryKeys.stocks`(`['stocks']`)·`holdings`(`['holdings', 'me']`), `hooks/useStocksQuery`(`refetchInterval` 5초, 서버 시세 갱신 주기와 같음. WebSocket 전까지 폴링), `hooks/useHoldingsQuery`(폴링 없음, 매매 성공 후 invalidate 예정)
  - `TradePage`: 목업 상수·가짜 시세 `useEffect` 삭제. 현금은 `useMemberQuery`, 종목은 `useStocksQuery`, 보유 수량은 보유 목록을 코드 → 수량 `Map`으로 붙임. 등락률은 서버 `changeRate`(오늘 시작가 대비). "내 종목 평가액"은 수량 × 현재가 화면 합계로 임시 계산(11/2 포트폴리오 API로 교체)
  - `StockSearchPage`: 같은 `['stocks']` 캐시 사용(목록에서 들어오면 요청 없이 표시). "지금 인기"는 고정 코드 5개(순위 API 없음, 랭킹 MVP 밖), 최근 검색은 빈 상태로 시작(저장은 추후)
  - 종목 목록 데스크톱은 목록 영역만 스크롤(`max-height` 560px, 탭 고정), 모바일은 페이지 스크롤. 안내 문구 "샘플" → "가상"
  - 로컬 확인 (10/30): 상단 현금, 내 종목 KK0001 12주, 전체 20개, `stocks` 요청 5초 간격 + 가격·등락률 갱신. `eslint`, `tsc -b` 통과
- `@Valid` 실패 응답 형식 (2026-10-30, `fix/valid-error-response`): `GlobalExceptionHandler`가 `MethodArgumentNotValidException`도 받아 `ErrorCode.INVALID_REQUEST`(400) → `{code, message}`. 필드별 상세는 넣지 않음(프론트는 `code`로만 분기, 수량은 화면에서 먼저 막음). 도메인 검증 `INVALID_QUANTITY`(엔티티)와 요청 형식 검증은 층이 달라 분리. JSON 형식 오류는 범위 밖
- 사기·팔기 화면 연결 (2026-11-01, `feat/order-page-connect`, 종목·매매 연결 PR B)
  - `api/tradeApi.buyStock`·`sellStock`(성공 바디 없음 → `Promise<void>`), `hooks/useBuyStockMutation`·`useSellStockMutation`(`onSuccess`에서 `queryKeys.member()`·`holdings()` invalidate, 시세는 제외)
  - `OrderPage`: 종목 하나는 `useStocksQuery` 목록에서 URL code로 찾음(새 API 없음, 5초 폴링 그대로), 보유 수량 `useHoldingsQuery`, 현금 `useMemberQuery`. 없는 코드는 "그런 종목은 없어요"
  - `OrderPanel`: 최대 수량(매수 현금 ÷ 현재가 내림, 매도 보유 수량), 0이면 주문 버튼 잠금. `mutate(값, { onSuccess, onError })`로 말풍선 문구, 에러는 `ApiError.code`(`INSUFFICIENT_CASH`, `INSUFFICIENT_HOLDING`, `INVALID_REQUEST`)별 문구. 요청 중 버튼 잠금("주문 중...")
  - 시세 기록·깜빡임: `lastPrice` state와 비교해 렌더 중 갱신(react-hooks v7이 effect 안 setState 금지). 차트는 화면에 들어온 뒤 본 가격만(최대 30개), 점 간격 고정 + 세로 폭 최소 현재가 2%
  - 등락 표시는 "▲ +1.3% 오늘"(응답에 오늘 시작가가 없어 원 단위 생략)
  - `components/OrderHeader` 분리(현금 props만). `OrderPanel`은 매매 훅·`ApiError`를 써서 `pages`에 남김
  - 확인: `tsc -b`, `eslint` 통과. 못 확인한 것: 브라우저에서 매수·매도 후 현금·보유 즉시 갱신, 잠금 표시
- 중간 점검 (2026-11-02): 매수·매도 실서버 화면 동작 정상. 기동 직후 `오늘 시작가 갱신` 오탐 재현 없음 (10:24 기동 로그, #44)
- 거래내역 조회 API (2026-11-02, `feat/trade-history-api`, `decisions/032`)
  - `GET /api/trades/history`(기간·구분, `Slice` 20개, `tradedAt desc, id desc`), `/summary`(기간 실현손익·매도 원가·매수·매도 금액·건수, 구분 필터 없음), `/by-stock`(종목별 실현손익, 매도만, 실현손익 큰 순)
  - `trade/Period`(WEEK 7·MONTH 31·QUARTER 92일, ALL은 null = 제한 없음), `TradeHistoryService`(읽기 전용), `TradeHistoryController`(`type` 생략 = 전체, page `@Min(0)`), DTO 4개
  - 매도 원가는 저장하지 않고 `체결가 × 수량 − 실현손익`으로 쿼리에서 역산 (심장 ① 저장 로직 변경 없음)
  - 페이징은 `Slice`(화면에 전체 건수 불필요, count 쿼리 없음), size는 서버 상수 20
  - 로컬 확인 (11/2): 목록 12건 최신순, `tradedAt` ISO 문자열, 매수 합 618,600 검산 일치. KK0001 3주 매도 후 실현손익 −8,400, 매도 원가 154,650(51,550 × 3), 종목별 1줄. `page=-1` 400
- 내 거래 화면 연결 (2026-11-03, `feat/trade-history-connect`, 무한 스크롤 전 PR A)
  - `api/tradeHistoryApi.ts`(`getTradeHistory`·`getTradeHistorySummary`·`getStockProfits`, `URLSearchParams`로 쿼리스트링, `type` null이면 생략), `types/api.ts`(`Period`, `TradeHistoryPageResponse`, `TradeHistorySummaryResponse`, `StockProfitResponse`)
  - 쿼리 키에 선택값 포함: `['tradeHistory', 'list', period, type]`, `['tradeHistory', 'summary', period]`, `['tradeHistory', 'byStock', period]`. 앞부분 `'tradeHistory'`로 한 번에 무효화 가능. 훅 3개(`useTradeHistoryQuery`·`useTradeHistorySummaryQuery`·`useStockProfitsQuery`), 폴링·매매 후 invalidate 없음(화면 재진입 시 staleTime 0으로 재조회)
  - `TradeHistoryPage`: 목업 거래·기간 계산 삭제 → 요약·목록·종목별 응답 사용. 'ALL' → null 변환은 페이지에서. 보이는 탭만 로딩·에러 표시, 로딩 중엔 빈 상태 숨김. 목록은 첫 20건(page 0)
  - 총손익 구성 카드는 목업 유지(`TOTAL_PROFIT`, `ALL_REALIZED`), 평가액 API 연결 때 교체 → 11/4 교체 완료
  - 로컬 확인 (11/3): 진입 시 요청 3개, 현금 실제 값, 1주 실현손익 −8,400·수익률 −5.43%, 날짜별 목록, [매도] `type=SELL` 매도 1건만, [매수]·[전체] 전환, 기간 전환 재요청, 종목별 탭 1줄. `eslint`, `tsc -b` 통과
- 작성 분담 조정 (2026-11-03, `decisions/045`): 프론트(React) 집중. 백엔드는 새 개념이 있는 작업(심장 ③ 배치, WebSocket 푸시)만 개발자 직접, 심장 ②와 반복 로직(추천 API, 집 사기 등)은 Claude 작성 → 개발자 리뷰. 심장 ①③④·프론트 동작은 기존대로. CLAUDE.md "절대 금지"·"테스트", `domain/glossary.md` 반영
- 평가액 API (심장 ②) (2026-11-03, `feat/portfolio-api`, PR A, `decisions/044`)
  - `GET /api/portfolios/me` → `PortfolioResponse{cash, initialCash, stockValue, totalAsset, profit, profitRate, holdings}`
  - `PortfolioService`(조회만: 회원 + `HoldingService.findHoldings`) → `PortfolioCalculator.calculate`(final 클래스·static, DB·시세 모름, WebSocket 푸시에서 재사용 예정) → `PortfolioValuation`
  - 금액 `Math.multiplyExact`·`addExact`·`subtractExact`, 수익률 `BigDecimal` 소수 4자리 HALF_UP(`changeRate`와 같음). 시드머니 1원 미만이면 `IllegalArgumentException`
  - `PortfolioCalculatorTest` 9개 메서드(경계 포함 13케이스): 보유 없음, 합계, 이익(523만 → +4.6%), 손실, 반올림 경계(±0.00005), 시드 5,000만, 곱셈·덧셈 넘침, 시드 0·음수
  - 확인 (11/3): `./gradlew test` 통과, 로컬 응답 cash 3,666,550 + stockValue 1,300,400 = totalAsset 4,966,950, profit −33,050, profitRate −0.0066 (손 검산 일치)
  - 못 확인한 것: 쿠키 없는 요청 401 (기존 `SecurityConfig` 규칙 대상)
- 홈·평가액 화면 연결 (2026-11-04, `feat/home-portfolio-connect`, 평가액 PR B)
  - `api/portfolioApi.getMyPortfolio`, `queryKeys.portfolio`(`['portfolio', 'me']`), `hooks/usePortfolioQuery`(`refetchInterval` 5초, WebSocket 전까지 폴링). 탭이 백그라운드면 폴링 건너뜀(`refetchIntervalInBackground` 기본 false, query-core `queryObserver.js` 소스로 확인)
  - `types/api.ts` `PortfolioResponse`에 `initialCash` 추가
  - `HomePage`: 목업 `SEED_MONEY`·`CASH`·`NET_WORTH`·`HOLDINGS` 삭제 → 응답 사용(CASH 칩, 평가액, 손익·수익률, 목표 진행바, 차트 기준선, 보유 종목). 데이터가 한 번도 안 왔을 때만 로딩·에러 문구(폴링 실패 시 직전 값 유지), 보유 없으면 "아직 산 종목이 없어요."
  - 홈 손익 색·화살표를 부호별로: 수익 `#E5484D` 위, 손실 `#3B82F6` 아래(design.md 어두운 배경 `--up`/`--down`), 0원은 회색·화살표 없음. 기존 `#FF7A7E` 고정 빨강 제거
  - 매수·매도 성공 시 `queryKeys.portfolio()` invalidate 추가
  - `TradeHistoryPage` 총손익 구성 카드: 총손익 = `portfolio.profit`, 실현 = 전체 기간 요약(`useTradeHistorySummaryQuery('ALL')`, 기간 필터가 전체면 같은 키라 요청 1번), 평가손익 = 총손익 − 실현(역산, `decisions/044`)
  - `TradePage` "내 종목 평가액" 화면 합계 → `portfolio.stockValue`
  - 홈·내 거래 하단 "샘플" 안내 문구 삭제 (실제 데이터라 불필요). 아파트·계약서·내 마을은 연결 때 같은 방식
  - 남은 목업: 홈 목표 아파트(`GOAL`, 추천 API 때), 장 마감 시계, 손익 차트(심장 ④)
  - 로컬 확인 (11/4): 홈 평가액·손익 응답 값 표시, 손실 시 파란 아래 화살표, `portfolios/me` 5초 간격 요청
- WebSocket 평가액 푸시 백엔드 (2026-11-06, `feat/portfolio-websocket-push`, PR 1/2, `decisions/046`)
  - `global/config/WebSocketConfig`: `@EnableWebSocketMessageBroker`, STOMP 엔드포인트 `/ws`, 메모리 브로커 `/queue`(`/user` 접두사는 기본값). 허용 Origin 기본값(같은 출처)
  - `portfolio/PortfolioPushScheduler`: `@Scheduled(fixedRateString = "${virtual-price.tick-interval}")`(시세 주기 5초와 같음), `SimpUserRegistry` 접속자마다 `PortfolioService.getMyPortfolio` → `convertAndSendToUser(memberId, "/queue/portfolio", …)`. 한 명 실패(`BusinessException`)해도 나머지 계속
  - `vite.config.ts`: `/ws` 프록시 `ws: true`
  - `docs/ui-spec.md`: §6 구독 경로 `/user/queue/portfolio` + `PortfolioResponse`로 확정, §8 "가짜 돈" 안내는 시작·시드 선택·아파트 대출 안내에서만
  - 로컬 확인 (11/6): 브라우저 콘솔에서 STOMP 프레임 직접 전송(CONNECT → SUBSCRIBE `/user/queue/portfolio`) → `CONNECTED` 프레임 `user-name:1`(Principal 이름 = memberId 확인), 5초마다 `MESSAGE`에 평가액 JSON(cash 3,666,550 + stockValue 1,231,300 = totalAsset 4,897,850 일치)
- 일정 재조정 (2026-10-25, `decisions/039`): 10/16~10/25 개발 공백(코스콤 필기 준비)으로 런칭 10/29 → 11/14, 기능 축소 없음

- 아파트 추천 API (2026-11-06, `feat/apt-recommend-api`, `decisions/047`, 작성 분담 045: 에이전트 작성 → 개발자 리뷰)
  - `GET /api/apartments?q=&affordableOnly=&page=` → `AptRecommendPageResponse{items, page, hasNext, maxPrice}`. 카드 `AptRecommendResponse{dealId, complexName, region, area, floor, price, contractDate, ownAmount, loanAmount, affordability, shortage}`
  - `HousePriceRule`(내 돈 = 집값 × 40% 원 미만 올림, 대출 = 나머지, 평가액으로 살 수 있는 최대 집값), `Affordability`(OK·NEED_SELL·SHORT) + `AffordabilityCheck.of(내 돈, 현금, 평가액)` → 상태·부족액
  - `AptDealRepository`: 최근 6개월·해제 제외·단지 join fetch. 살 수 있는 집만 비싼 순 / 전체 싼 순, 검색어는 단지명·지역명·법정동
  - `AptRecommendService`: 평가액·현금은 `PortfolioService`(044), 판정은 `AffordabilityCheck`
  - `AptComplex.regionName` NOT NULL 컬럼 추가(배치가 시군구 코드표로 채움), `AptDeal.price` 원 단위로 확정(배치에서 만원 × 10,000)
  - 테스트: `HousePriceRuleTest`(40%·60%, 올림 경계, 최대 집값과 판정 경계 일치, 0·음수, 넘침), `AffordabilityCheckTest`(현금·평가액 = 내 돈 경계, 1원 부족, 돈 없음)
  - `types/api.ts`: `AptRecommendResponse`에 `floor`·`contractDate`, `AptRecommendPageResponse` 추가
  - 못 확인한 것: `./gradlew test`(작업 환경 Gradle 다운로드 차단), 실제 데이터 조회(배치 전이라 `apt_deal` 비어 있음, 배치 후 확인)

- WebSocket 평가액 프론트 연결 (2026-11-06, `feat/portfolio-websocket-connect`, PR 2/2, `decisions/046`)
  - `@stomp/stompjs` 7.5.0 추가 (STOMP 표준 클라이언트, 의존성 없음)
  - `ws/portfolioSocket.subscribePortfolio(onMessage)`: `Client`(brokerURL `ws(s)://{host}/ws`, `reconnectDelay` 5초) → `onConnect`에서 `/user/queue/portfolio` 구독(재연결 때 자동 재구독) → 끊는 함수 반환(`deactivate`)
  - `hooks/usePortfolioSocket(isEnabled)`: `useEffect`에서 구독, 메시지는 `setQueryData(queryKeys.portfolio())`, cleanup에서 연결 종료
  - `MemberGuard`: 회원 조회 성공(`isSuccess`) 후 연결. 로그인 화면 전체를 감싸서 페이지 이동해도 연결 1개 유지
  - `usePortfolioQuery`: `refetchInterval`(5초 폴링) 삭제 → 첫 값만 GET, 이후 WebSocket
  - 네트워크 단절(서버 재시작 등)은 stompjs 자동 재연결, 화면 이탈은 React cleanup이 담당
- 집 사기·내 마을 API (2026-11-06, `feat/house-purchase-api`, `decisions/048`, 작성 분담 045)
  - `POST /api/houses {dealId}` → `BuyHouseResponse{houseId, tier, isTierUp}`. `HouseService.buyHouse`: 회원 비관적 락(025) → 거래 조회 → `AptDeal.isAvailable`(해제 아님 + 계약일 ≥ 오늘 − 6개월) → 같은 회원 같은 거래 중복 확인 → `member.withdraw(HousePriceRule.ownAmount)` → `House.purchase` 저장 → 집 수로 `Tier`
  - `GET /api/houses` → `MyHousesResponse{houses, houseCount, tier}`(join fetch 거래·단지, 먼저 산 순)
  - `GET /api/apartments/{dealId}` → 계약서 화면용 거래 1건(`AptRecommendResponse`, 카드 상태 포함)
  - `house/Tier`(GEOLBAENGI·SEOMIN·JOKBU·GAPBU, 화면 타입과 같은 이름), `ErrorCode` `DEAL_NOT_FOUND`(404)·`DEAL_NOT_AVAILABLE`(400)·`ALREADY_OWNED_DEAL`(409)
  - `AptComplex.of`·`AptDeal.of` 정적 팩토리(배치·테스트용), `AptDeal.marketPriceSince`로 6개월 기준을 추천·구매가 공유
  - `MemberResponse`에 tier·houseCount는 넣지 않음(member ↔ house 순환 참조 방지, 홈은 내 마을 쿼리 사용)
  - 테스트: `TierTest`(0·1·2~4·5 이상·음수), `AptDealTest`(6개월 경계 당일 포함, 하루 전 불가, 해제), `HouseServiceTest`(40% 차감·집 저장·서민 변신, 현금 = 내 돈 경계, 변신 여부 2·3·5·6채째, 1원 부족 실패 시 현금·저장 그대로, 없는 거래, 기간 지난 거래, 중복 구매, 락 사용)
  - `types/api.ts`: `BuyHouseRequest`, `HouseResponse`, `MyHousesResponse`
  - 못 확인한 것: `./gradlew test`(작업 환경 Gradle 차단), 응답 JSON의 `isTierUp` 이름(Jackson record 직렬화, 로컬 curl로 확인 필요), 실제 거래 데이터(배치 전)
- 일별 평가액 기록 + 홈 손익 차트 (심장 ④) (2026-11-06, `feat/portfolio-snapshot-chart`, `decisions/049`, 작성 분담 049: 에이전트 작성 → 개발자 리뷰)
  - `portfolio/PortfolioSnapshot`(`portfolio_snapshot`, 회원·날짜 UK `uk_snapshot_member_date`, `total_asset`), `PortfolioSnapshotRepository`(`existsByMemberIdAndSnapshotDate`, `findAllByMemberIdSince` 날짜 오름차순)
  - `PortfolioSnapshotScheduler`: `@Scheduled(cron = "${portfolio-snapshot.cron:0 0 0 * * *}", zone = "Asia/Seoul")` → 어제 날짜로 회원별 기록. `BusinessException`·`DataIntegrityViolationException`은 그 회원만 건너뜀
  - `PortfolioSnapshotService.recordSnapshot`: 이미 있으면 false(멱등), 계산은 `PortfolioCalculator`(044) 재사용. 회원마다 트랜잭션 분리(스케줄러 → 서비스, 같은 클래스 호출 시 프록시 우회 방지)
  - `MemberRepository.findAllIds`, `ChartPeriod`(WEEK 6·MONTH 29일 전부터), `GET /api/portfolios/me/history?period=` → `PortfolioHistoryResponse{date, totalAsset}`
  - 프론트: `types/api.ts`(`ChartPeriod`, `PortfolioHistoryResponse`), `api/portfolioApi.getMyPortfolioHistory`, `queryKeys.portfolioHistory`(`['portfolio', 'history', period]`), `hooks/usePortfolioHistoryQuery`(staleTime 10분), `utils/chart.ts`(날짜 칸·축 라벨·좌표), `components/ValuationChart`
  - `HomePage`: 1일 탭 삭제, 기본 1주. 지난 날 = 기록, 마지막 칸 = WebSocket 평가액. 빈 날은 점 없이 앞뒤 직선 연결, 점선 = 시드머니, 선 색은 손익 부호. 기록 없으면 "매일 0시에 하루치씩 쌓여요"
  - 테스트: `PortfolioSnapshotServiceTest`(저장 값 450만 + 10주 × 52,000 = 502만, 이미 있으면 건너뜀, 회원 없음, 기간 시작일)
  - 확인: 프론트 `tsc -b`, `eslint` 통과
  - 못 확인한 것: `./gradlew test`(작업 환경 Gradle 차단), `portfolio_snapshot` 테이블 생성, 스케줄러 실행(로컬 `PORTFOLIO_SNAPSHOT_CRON=0 * * * * *`로 확인 예정: 첫 실행 1명 저장, 다음부터 0명), 화면 표시
- 플레이 점검 반영 (2026-11-06, `feat/play-feedback`, `decisions/050`)
  - HUD 시계 "장 마감까지" 목업 → "오늘 마감까지" 0시 카운트다운 (홈, 내 마을). `utils/time.ts` `toMidnightLeft`, `hooks/useMidnightCountdown`(1초 갱신, cleanup에서 해제)
  - 홈: 보유 종목 0개면 큰 버튼 "첫 종목 사러 가기", 일별 기록 없으면 차트 대신 "평가액이 쌓이면 그래프로 한눈에 볼 수 있어요"
  - HUD 현금 통일: 집 찾기 CASH·추천 계산을 실제 현금·평가액(`usePortfolioQuery`)으로, 내 마을 HUD TOTAL → CASH(`useMemberQuery`). 집 목록·마을 집은 아직 목업
  - 종목 유형·평소 가격 공개는 구현 후 화면 확인에서 포기 (스스로 파악하는 재미, 050 포기한 대안)
  - 확인: `tsc -b` 통과, 브라우저에서 홈 시계 카운트다운·차트 빈 상태 확인. 못 확인: ESLint(작업 환경 시간 초과, 커밋 훅에서 확인), 집 찾기·내 마을 HUD 값
- 실거래가 배치 PR A: 국토부 API 클라이언트 (2026-11-10, `feat/apt-deal-client`, `decisions/052`)
  - 국토부 "아파트 매매 실거래가 상세 자료" API (`RTMSDataSvcAptTradeDev`), `_type=json`
  - `apartment/molit/MolitClient.fetchDeals(시군구, YearMonth)`: 1,000건씩 페이지 반복(누적 ≥ totalCount 또는 빈 페이지면 종료), 연결 5초·읽기 30초 타임아웃, 응답은 `byte[]`로 받아 전용 `JsonMapper`로 파싱(빈 달 `"items":""` → null), 결과 코드 `000` 아니면 예외
  - Boot 4는 `RestClient.Builder` 자동 설정이 별도 모듈이라 `RestClient.builder()`로 직접 생성(의존성 추가 없음)
  - `MolitDealItem`: 응답 필드 전부 `String`, 변환 메서드(금액 만원 × 10,000, 면적 소수 2자리 HALF_UP, 계약일, 해제 `O`, 공백 trim)
  - 시군구 코드표 `resources/apartment/sigungu.csv` 256개 (법정동코드 전체자료에서 생성, 전남광주통합특별시 12xxx·화성 구 반영, 옛 코드 0건 확인)
  - 테스트: `MolitDealItemTest` 8개, `MolitResponseParseTest` 5개(1건 객체, 배열, 숫자 → 문자열, 빈 달, XML 에러). 전체 `./gradlew test` 통과
  - 심장 ③ 작성 방식 변경: 대화창 소스 제시 → 개발자 입력 (045 보강)
- 실거래가 배치 PR B: upsert 저장·매일 수집 (2026-11-11, `feat/apt-deal-batch`, `decisions/052`)
  - `apartment/SigunguCodes`: `sigungu.csv` → 읽기 전용 Map (코드 → 지역명)
  - `AptDealBatchService.saveDeals`(쓰기 트랜잭션 = 시군구·월 1단위): 단지 찾기 또는 저장(`orElseGet`) → 거래 찾기(유니크 키 5개) → 있으면 해제 여부만 갱신(`AptDeal.updateCanceled`, 변경 감지), 없으면 저장. 코드표에 없는 시군구·변환 실패 거래는 건너뜀
  - `AptDealBatchScheduler`: 매일 4시(KST) 이번 달 + 지난달, 비어 있으면 7개월. 첫 적재는 `ApplicationReadyEvent`에서 별도 스레드(`apt-deal-initial`), `AtomicBoolean`으로 동시 실행 방지. API 호출은 트랜잭션 밖, 단위별 실패는 로그 후 계속
  - `apt-deal-batch.enabled`(테스트 yaml false, `@ConditionalOnProperty`), `spring.task.scheduling.pool.size: 2`
  - 테스트 `AptDealBatchServiceTest` 8개(H2, `@SpringBootTest` + `@Transactional` 롤백): 신규 저장·지역명, 중복 1건, 면적 반올림 후 같은 거래, 해제 갱신, 처음부터 해제, 같은 단지 2건, 코드표에 없는 시군구, 변환 실패만 건너뜀. 전체 `./gradlew test` 통과
  - 로컬 첫 적재 (11/11): 248,133건, 시군구 255개(52800까지), 2026-05~11, 최저가 800만 원
  - 로컬 메모: `show-sql`·`format_sql`(local 프로필)로 쿼리 70만 개 이상이 콘솔에 찍혀 IntelliJ "Low memory" 경고. 서버 동작과는 무관, 마지막 집계 로그는 못 봄
  - 못 확인한 것: "시세 저장" 로그의 스레드 이름(풀 2개 적용 여부), 실서버 첫 적재(RDS) → 아래 fix에서 확인
- 스케줄러 분리·첫 적재 재실행 (2026-11-11, `fix/scheduler-and-batch-rerun`, `decisions/052` 정정)
  - 실서버 로그 `[MessageBroker-2] 시세 저장`: `@Scheduled`가 WebSocket 브로커 스케줄러를 쓰고 있었음. yaml `spring.task.scheduling.pool.size`는 효과 없어 삭제
  - `global/config/SchedulingConfig`: `SchedulingConfigurer` + `ScheduledExecutorService` 빈(2개, `scheduling-` 이름, 종료 시 `shutdown`)
  - 실서버 첫 적재가 배포 재시작으로 50,884건(시군구 59개, 26290까지)에서 중단 → 재시작 후 거래가 있어 첫 적재 생략
  - `AptDealBatchProperties`(`enabled`, `forceInitial`), `APT_DEAL_BATCH_FORCE_INITIAL`(yaml, compose)로 강제 재실행. 시작 로그 `실거래가 수집 시작: N개월, 시군구 256개`
  - 운영 순서: EC2 `.env`에 true → 배포 → 끝날 때까지 배포 금지 → false로 되돌리고 `docker compose up -d`

- 집 찾기 화면 연결 (2026-11-11, `feat/apt-list-connect`, 아파트·집 사기 연결 PR A)
  - `api/aptApi.getAptRecommendations`(q 빈 문자열이면 생략, `affordableOnly`는 항상 전송: 서버 기본값 true), `queryKeys.aptRecommendations`(`['apartments', 'list', q, isAffordableOnly]`), `hooks/useAptRecommendationsQuery`(첫 20건, page 0)
  - `ApartmentsPage`: 목업 6채·화면 40% 계산 삭제 → 서버 `affordability`·`shortage`로 버튼 3종, MAX는 응답 `maxPrice`(카드 판정과 같은 시점 값). 검색은 Enter 때 요청(`form onSubmit` + `preventDefault`, 24만 건 LIKE를 글자마다 보내지 않음)
  - "살 수 있는 집만" 토글: 기본 켜짐, `sessionStorage`(`apartments.isOnlyOk`)로 탭 안에서 유지(화면 이동 시 `useState` 초기화 문제). 켜짐이면 첫 카드에 "가장 가까워요"(11/12 "영끌"로 변경)
  - "가짜" 문구 정리 (`ui-spec.md` §8, `design.md` §11): 시작·시드 선택·계약서 연출에만 남김. LOAN 카드 "대출 60% 자동 적용", 투자·검색·변신·내 마을 하단 안내에서 삭제
  - 로컬 확인 (11/11): 전체 목록 응답 229ms, NEED_SELL 부족액 검산(내 돈 320만 − 현금 3,077,550 = 122,450), 버튼 3종, 검색 + Enter, 토글 유지(투자 화면 갔다 오기). `tsc -b` 통과
  - 못 확인한 것: ESLint(작업 환경 시간 초과, 커밋 훅에서 확인), 실서버

- 계약서·집 사기 화면 연결 (2026-11-12, `feat/house-purchase-connect`, 아파트·집 사기 연결 PR B1)
  - `api/aptApi.getAptDeal`(거래 1건 + 카드 상태), `api/houseApi.buyHouse`, `queryKeys` `apartments`(앞부분 무효화용)·`aptDeal`·`myHouses`, `hooks/useAptDealQuery`(`retry: false`, 없는 거래 재시도 방지), `hooks/useBuyHouseMutation`(회원·평가액·내 마을·집 목록 invalidate)
  - `ContractPage`: `useParams`로 dealId(문자열 → `Number`), 목업 삭제. 도장 칸 클릭 → 확인창(내 돈 40%, 계약 후 남은 현금, YES/NO) → YES면 `POST /api/houses` → 도장 + 변신 알림(`isTierUp`, 1·2·5채째). 계약 전 현금은 클릭 시점 값을 state에 저장(invalidate 후 회원 현금이 바뀌므로)
  - 현금 부족이면 도장 칸 잠김 + 부족액, 에러 코드별 문구(`INSUFFICIENT_CASH`, `ALREADY_OWNED_DEAL`, `DEAL_NOT_AVAILABLE`). "다시 해보기" → "다른 집 보러 가기", 도장 전에는 "집 목록으로"
  - 문구: 집 찾기 버튼 "계약하기" → "부동산 방문"(`ui-spec`, `design`, `domain/glossary`·`workflows`), 배지 "영끌", 카드에 층, 검색 안내 "서울, 강남구, 단지 이름", 매도인 "꿈이", 특약사항 교체
  - 대출 안내 표시: 주택담보대출 연 3.7%(고정), 30년 원리금균등 월 상환액(`utils/loan.ts`). 표시만, 현금 차감 없음(`domain/glossary`)
  - `utils/format.formatDotDate`, `ContractPage.css` 도장 칸 버튼(흔들림, reduced-motion이면 끔)·확인창
  - 로컬 확인 (11/12): 도장 칸 흔들림 → 확인창, NO 닫힘, YES 도장·서민 변신·CASH 감소, 응답 키 `isTierUp`, 같은 거래 재구매 "이미 산 집", 현금 부족 잠김. `tsc -b` 통과
  - 못 확인한 것: ESLint(작업 환경 시간 초과, 커밋 훅에서 확인), 실서버

- 순자산 카드 (2026-11-12, `feat/net-worth-card`, `decisions/054`)
  - 문제: 집을 사면 현금만 줄고 집은 안 잡혀 홈 카드가 손실처럼 보임(시드 500만 → -69.2%)
  - 백엔드: `PortfolioCalculator`에 내 집 지분(집마다 `HousePriceRule.ownAmount` 합)·순자산·오늘 증감(`DailyChange`), `PortfolioResponse`에 `houseEquity`·`netWorth`·`dailyChange`·`dailyChangeRate`, 총손익은 순자산 − 시드머니. `HouseService.findPurchasePrices`, `PortfolioSnapshot.houseEquity`(컬럼 추가)·`getNetWorth`, `PortfolioHistoryResponse.netWorth`. 평가액(`totalAsset`)은 추천·첫 집 목표용으로 유지
  - 테스트: `PortfolioCalculatorTest` 6개 추가(지분 올림 합, 집 사기 전후 순자산 동일, 순자산·손익, 오늘 증감 ±, 기준 0), `PortfolioSnapshotServiceTest` 1개 추가(지분 저장)
  - 프론트 모양: `types/api.ts`, `utils/portfolio.ts`(`toAssetShares` 최대 나머지 방식, `toSeedMultiple`), `utils/format.formatShortRate`, `components/NetWorthSummary`, `HomePage.css` 막대, `VillagePage` 자랑 줄(목업 상수 `MOCK_SEED_RESULT`)
  - 데이터 연결(개발자): `HomePage` 카드에 `NetWorthSummary` 연결·차트 값 `netWorth`로, 내 마을 자랑 줄은 내 마을 API 연결 때 `usePortfolioQuery`로
  - 홈 카드 데이터 연결 완료 (`NetWorthSummary`, 차트 값 `netWorth`, 첫 집 목표 변수 `totalAsset`)
  - 홈 차트 제목 "내 손익" → "순자산 추이" (주식만이 아니라 현금 + 주식 + 집 지분)
  - 변신 단계 그림·문구 `utils/tier.ts`(`TIER_INFO`). 홈 변신 카드·투자 응원 그림이 사용, 단계는 `api/houseApi.getMyHouses`(`GET /api/houses`) + `hooks/useMyHousesQuery`(`queryKeys.myHouses`, 집 사기 후 invalidate)로 연결, 불러오기 전·실패 시 걸뱅이
  - 투자 응원 문구: 보유 0개면 "뭐라도 담아야 집을 사지!", 1개 이상이면 "존버는 승리한다!"
  - 다음 PR: 목표 패널을 목업(태백 1,800만)에서 "아직 못 사는 집 중 가장 싼 집"으로 (0채 "첫 집 목표", 1채 이상 "다음 집 목표"), 백엔드 조회 1개 추가
  - 못 확인한 것: `./gradlew test`(작업 환경에서 Gradle·Maven 다운로드 차단), ESLint(작업 환경 시간 초과). `tsc -b` 통과
  - 운영: 배포 후 RDS `portfolio_snapshot.house_equity` 컬럼 추가·기존 행 0 확인
- 목표 패널 A (2026-11-12, `feat/goal-apartment`, `decisions/055`)
  - 백엔드: `AptDealRepository.findGoalCandidates`(집값 > maxPrice, 최근 6개월·해제 제외, 내가 산 거래 `not exists` 제외, 싼 순, `Limit`), `AptRecommendService.findGoal` → `Optional<AptRecommendResponse>`, `GET /api/apartments/goal`, 없으면 404 `GOAL_NOT_FOUND`
  - 테스트: `AptDealRepositoryTest` 4개(H2): 집값 = maxPrice 제외·+1원 목표(계약금 4,000,001원), 산 거래 제외, 다른 회원이 산 거래는 목표, 후보 없으면 빈 결과
  - 프론트 모양: `HomePage` 목업 `MOCK_GOAL`(응답 타입 그대로, null = 404), 0채 "첫 집 목표"·1채 이상 "다음 집 목표", 다 모으면 "계약금 모았어요!" + 부동산 링크(`.k-quest-link`)
  - 데이터 연결 완료: `aptApi.getAptGoal`(404 `GOAL_NOT_FOUND` → `null`), `useAptGoalQuery`, `queryKeys.aptGoal`(`['apartments', 'goal']`, 집 사기 무효화에 포함). `undefined`(로딩)와 `null`(목표 없음) 구분, 목업 `MOCK_GOAL` 삭제
  - 확인: `./gradlew test` 통과(로컬), `npm run lint`·`npm run build` 통과, 로컬 `GET /api/apartments/goal` 200(`/{dealId}`와 경로 충돌 없음)
- 종목 상세 분봉·일봉 차트 (2026-11-12, `feat/stock-price-history`, `decisions/056`)
  - 문제: 상세 차트가 화면 진입 후 가격만(최대 30개) 찍어 무엇을 살지 판단할 재료가 없음 (플레이 중 개발자 발견)
  - 백엔드: `StockPriceHistory`(1분 기록, 인덱스 (stock_code, recorded_at)), `StockDailyCandle`(일봉 OHLC, 유니크 (stock_code, trade_date), `open`·`update`), `StockPriceSnapshotService.savePrices`가 1분 저장 때 기록 1행 추가 + 오늘 일봉 upsert(날짜별 일봉 한 번에 조회), `GET /api/stocks/{code}/prices`(최근 24시간), `GET /api/stocks/{code}/candles`(최근 30일), `StockPriceHistoryScheduler`(매일 3:30 24시간 지난 1분 기록 벌크 delete, 일봉은 삭제 없음). 1분 기록은 분 단위 시각 + 같은 분 중복 생략, 시세 갱신·평가액 푸시는 `fixedDelay`(절전 후 밀린 실행 몰림 방지)
  - 테스트: `StockPriceHistoryTest` 7개(H2): 스냅샷 있는 종목만 기록, since 이후·오래된 순·다른 종목 제외, before 이전만 삭제(경계 남김), 같은 날 일봉 고가·저가·종가 갱신(시가 = 오늘 시작가), 날짜 바뀌면 새 일봉, 같은 분 두 번이면 기록 1행, 없는 종목 404. `StockDailyCandleTest` 2개: 첫 기록 고가·저가, 갱신은 넓히기만
  - 프론트(045 예외, 개발자 요청으로 에이전트 작성): `stockApi.getStockPrices`·`getStockCandles`, `useStockPricesQuery`·`useStockCandlesQuery`(1분마다), `queryKeys.stockPrices`·`stockCandles`, `components/CandleChart`(심지 + 몸통, 오늘 캔들에 실시간 가격), `OrderPage` 탭 "분봉 | 일봉", 칸 수는 데이터에 맞춤(분봉 최소 1시간·일봉 최소 7일 폭, 넘으면 꽉 채움, `utils/chart.toSlotCount`), 선 아래 옅은 채움, 축 3칸은 실제 시각·날짜(`toThreeAxisLabels`), 탭 기준 최고·최저가, 일봉 3개 미만이면 "일봉은 하루에 1개씩 쌓여요". 화면 진입 후 기록 state(`hist`)는 삭제
  - 운영: 배포 후 RDS에 `stock_price_history`·`stock_daily_candle` 테이블 생성(`ddl-auto: update`) 확인. 분봉은 24시간, 일봉은 30일 뒤에 꽉 참

### 아직 안 한 것
- 회원·매매 외 도메인의 Service·Controller·DTO
- 프론트 데이터 연결: 계약서·내 마을은 목업 상수
- JSON 형식 오류(`HttpMessageNotReadableException`, 예: 수량에 문자열)는 Spring 기본 바디 그대로. 화면은 숫자 스테퍼라 발생 경로 없음, 런칭 전 점검 → 11/12 처리
- `@RequestParam` 검증 실패(`HandlerMethodValidationException`, 예: `page=-1`)도 Spring 기본 바디. 화면은 음수 page를 보내지 않음, 런칭 전 같이 점검 → 11/12 처리

### 런칭 후 과제 (우선순위)
기준: 면접 소재(트레이드오프 설명) 강도 + 사용자 체감. 확장 기능 순서는 9월 확정 순서(필터 JSON → 골든셋 → ES) 유지
0. Kafka (`decisions/053`): 11/15~16. 매수·매도 체결 이벤트(커밋 후 발행, 키 = 회원 ID) → 컨슈머가 그 회원 평가액 즉시 푸시. 브로커 1대(KRaft) Compose 추가, 도입일에 EC2 t3.small → t3.medium. AWS Budgets 알림 설정(크레딧 $98.97, 2027-01-10 만료)
0-1. AI 자산 코치 (`decisions/051`, 노션 "9. AI 자산 코치 설계"): Kafka 다음 착수, 약 2주. Spring AI + Gemini, 조회 전용 tool 3개, Redis 호출 한도
1. 자연어 아파트 검색 (필터 JSON, `decisions/023`): 11/11 게이트 미통과(내 마을 연결 전) → 런칭 후로 확정 (2026-11-12 개발자 결정). LLM 연동이 AI 코치와 같은 Spring AI + Gemini라 0-1과 같은 시기에 진행. LLM은 필터 JSON만 추출, 쿼리는 코드(추천 API 조회 재사용)
2. 시세 실시간 방송 `/topic/stocks`: 종목 목록·검색·주문 화면은 `useStocksQuery` 5초 폴링 유지 중. 모두에게 같은 값이라 공용 채널 방송이 맞음(평가액은 개인 큐 `/user/queue`, `decisions/046`). 서버 `tick` 뒤 방송 + 브로커 `/topic` 추가 + `ws/` 구독 1개
3. AI 응답 평가 골든셋: 자연어 검색 질문 50~100개 + 기대 필터, 정확도 리포트. CI 포함 여부(PR마다 vs 수동) 결정 필요
4. Elasticsearch 자동완성: 단지 마스터만 인덱싱(거래 행 제외). `decisions/024` 기술 상한선 변경 ADR + EC2 메모리(t3.small 2GB) 검토 선행. "DB 내장 검색으로 충분하지만 오버킬임을 알고 붙였다"로 설명
5. (2026-11-12 `feat/stock-price-history`, `decisions/056`) 시세 기록 API: 1분 기록 하루 보관, 주문 화면 차트 최근 1시간
6. 무한 스크롤: 내 거래(PR B)와 집 찾기 목록(지금은 첫 20건만, 2026-11-12 개발자 결정으로 런칭 후). 바닥 도달 시 다음 20건. `useInfiniteQuery` + IntersectionObserver, 백엔드 `Slice` 그대로
7. SSM 전환: 22번 포트 닫기 (`decisions/027`)
8. 무중단 배포(blue-green): 메모리 시세(`decisions/029`, `040`)가 서버마다 따로 생기지 않도록 시세 생성을 한 곳으로 모으는 설계가 먼저
9. (2026-11-11 해결, `decisions/052`) 스케줄러 스레드 풀 분리: `SchedulingConfigurer`로 `@Scheduled` 전용 풀 2개 (yaml 설정은 WebSocket 브로커 스케줄러 때문에 효과 없었음)
10. 정리: 종목 검색 최근 검색 저장(지금은 빈 상태로 시작), `OrderPanel` components 분리(주문 동작을 `OrderPage`로 올리고 props `onSubmit`·`isSubmitting`·`message`로 내리기)
11. 대출 한도 조정: 지금은 60% 고정. 사용자가 대출 비율을 고르는 기능 (2026-11-11 개발자 아이디어, LOAN 카드 문구는 기능 도입 때 "대출 한도"로 변경)
- MVP 밖 (런칭 후에도 미정): 빌딩, 랭킹·친구 비교, 집 팔기, 집값 하락 표시, 네이티브 앱

### 외부 대기 · 결정 필요
- 주식 시세: KIS와 공공데이터 주식시세정보 모두 서비스 사용 불가 확인(2026-10-06) → 서버가 만드는 가상 시세로 결정 (`decisions/020`, `failures/001`, `failures/002`)
  - 변동 방식은 `decisions/029`로 확정, 초기값도 029에 기록(2026-10-07). 구현 후 플레이 테스트로 조정
  - 현재가 저장·복구(`decisions/040`), 종목 유형 배치·시작가·호가 단위·`market` 삭제(`decisions/041`) 확정 (2026-10-27)
  - 정리 필요: `application.yaml`, `docker-compose.yml`의 KIS 키 설정 (별도 PR) → 11/12 처리
- 국토부 실거래가 API: 활용신청 완료 (키는 `.env`의 `MOLIT_SERVICE_KEY`). 이용허락범위 제한 없음 확인 (2026-10-06)
- AWS: EC2·RDS 운영 중. 후속 과제로 런칭 후 SSM 전환 검토 (22번 포트 닫기, `decisions/027`) → 런칭 후 과제 7
- 무중단 배포(blue-green): 지금은 EC2 1대·backend 컨테이너 1개라 배포 때 Spring 기동 시간(수십 초) 동안 `/api` 중단. 런칭 후 과제로 검토. 메모리 시세(`decisions/029`, `040`)가 서버마다 따로 생기지 않도록 시세 생성을 한 곳으로 모으는 설계가 먼저 필요 (2026-10-27) → 런칭 후 과제 8
- 자연어 아파트 검색: 런칭 후로 확정 (2026-11-12, 게이트 미통과, 런칭 후 과제 1)
- 쿠키 없는 401 응답에 `JSESSIONID` 쿠키 발급 (2026-10-27 로컬에서 발견). 원인 기록 없음, 런칭 전 확인 → 11/12 처리
- 운영 메모
  - EC2를 꺼 둔 동안 main에 merge하면 `deploy` 잡이 "서버 믿는다고 등록"(`ssh-keyscan`)에서 실패. EC2를 켠 뒤 마지막 deploy 실행의 실패한 잡만 다시 실행 (10/8~10/27 배포 공백 사례)
  - 실서버 접속은 `http://`를 붙여서. HTTPS 전환 전에는 Chrome이 https(443)로 먼저 시도해 타임아웃

### 다음 할 일 (순서)
1. (10/27 완료) 매수·매도 (심장 ①): PR #38 merge, 실서버 확인
2. 10/27~29 시세 제공자 (PR 3개): PR 1 종목 카탈로그(#39, 10/27 완료) → PR 2 가상 시세 엔진(#40, 10/27 완료) → PR 3 현재가 저장·복구(ADR 040) + 종목 조회 API(#41, 10/29 완료)
3. (10/29 완료) 회원 조회 API(`/api/members/me`, #42) + `useMemberQuery` + 화면 가드
4. (11/1 완료) 종목·매매 화면 연결: 보유 종목 API(#45), 목록·검색(#46), @Valid 형식(#47), 사기·팔기(#48). 버퍼 포함 2일 계획에서 하루 초과
5. (11/3 오전 완료) 중간 점검 → 거래내역 조회 API(#49) → 내 거래 화면 연결(PR A). 무한 스크롤(PR B) 남음
6. (11/4 오전 완료) 평가액 계산 (심장 ②) API(PR A #51, 작성 분담 045) → 홈 화면 연결 + 내 거래 총손익 카드 + 종목 목록 "내 종목 평가액" 교체 (PR B). 계획보다 반나절 초과
7. (11/6 완료) WebSocket 평가액 푸시 (PR 1 #53 백엔드 → PR 2 프론트, `decisions/046`). 병렬: 추천 API 백엔드 (#54, Claude, `decisions/045`), 집 사기 API 백엔드 (Claude, 11/7 몫을 미리)
8. (11/6 앞당겨 작성) 일별 평가액 기록 + 손익 차트 (심장 ④, `decisions/049`). 집 사기 API + 변신 단계 + 내 마을 조회 백엔드는 11/6 완료(#56). 11/7: 로컬 확인 후 merge
9. (11/11 완료) 실거래가 배치 (심장 ③). 도메인·HTTPS (Secure 쿠키 켜기)는 11/13로 이동 (2026-11-12 개발자 결정)
10. 11/10 → 11/12 아파트 화면 연결(추천 API, 11/11 완료) + 집 사기(계약서, 11/12 완료) → 내 마을 화면 연결
11. 11/11 변신 단계 화면 → 게이트 체크 + 플레이 테스트 (`decisions/029`, `050`): 시드 500만·5,000만 각각 30분 이상 플레이. 확인할 것: 5분 안에 캡처할 순간이 나오는지, 첫 집까지 필요한 배수(실거래가 배치 후 최저가 분포), 오른 종목 따라 사기 손실, 종목 성격을 가격 흐름만 보고 파악할 수 있는지. 필요하면 `virtual-price.*` 값 조정 → 029에 결과 기록
12. 11/12 통합 테스트·버그 수정, 11/13 도메인·HTTPS + 버퍼 (자연어 검색은 런칭 후)
  - 11/12 런칭 전 점검 4개 (작은 PR로 묶음)
    1. JSON 형식 오류(`HttpMessageNotReadableException`) 응답을 `{code, message}`로 통일
    2. `@RequestParam` 검증 실패(`HandlerMethodValidationException`, `page=-1`) 응답 통일
    3. 쿠키 없는 401에 `JSESSIONID` 쿠키가 발급되는 원인 확인 (2026-10-27 발견)
    4. `application.yaml`, `docker-compose.yml`의 KIS 키 설정 정리 (가상 시세로 결정, `decisions/020`)
13. 11/14 캡처 화면 다듬기 + README + 최종 점검 → 런칭
- 11/6 점검: 11/4 오후~11/5 개발 공백으로 2일 밀림 → 10번 버퍼 하루 사용 + 추천·집 사기 백엔드를 Claude가 병렬 작성(045 범위)해 하루 확보. 기능 축소 없음 (2026-11-06 개발자와 합의). 남은 버퍼: 11/13(자연어 검색과 겸용). 더 밀리면 자연어 검색 포함 여부를 다시 논의
- 버퍼 약 5일 포함 (1·2·4·8·10번에 하루씩). 11/1 점검 결과 하루 밀림 → 5~8번을 하루씩 미루고 8번 버퍼로 흡수, 11/6부터 원래 일정 (2026-11-01 개발자와 합의, 기능 축소 없음). 남은 버퍼: 10번 하루 + 11/13. 더 밀리면 10번 버퍼 → 11/13 순, 그 이상은 다시 논의 (`decisions/039`)
- 배포는 마지막 주에 몰지 않음. 기능은 PR merge 때마다 실서버에 바로 반영

## 참고
- 노션 일정표(간트): FRTR 페이지 > "6. 개발 일정" DB (10/25 재조정 반영)
- Figma 시안 사본: 데스크톱 10개, 모바일 7개 (모바일 사기·팔기, 계약서, 내 마을은 Figma 무료 플랜 MCP 호출 한도로 미완). 기준은 Claude Design 시안 v0.2
- 화면 ↔ 파일 대응표: `frontend/README.md`
- 결정 이유·실패 기록·용어: `docs/decisions/`, `docs/failures/`, `docs/domain/` (목록은 `docs/README.md`)
