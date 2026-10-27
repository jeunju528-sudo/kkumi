# 진행 현황

새 세션 시작 시 이 파일부터 확인. 작업 끝날 때마다 갱신

## 기준일: 2026-10-27

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
  - `/ws`는 WebSocket 만들 때 결정
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
- 가상 시세 엔진 (2026-10-27, 시세 PR 2/3, `feat/virtual-price-engine`): `decisions/042`(엔진 내부 반올림 전 가격은 `double`, 밖으로는 `long`)
  - `stock/PriceRules`: 호가 단위(가격대별 표), 호가 반올림, 상한가(+30% 내림)·하한가(-30% 올림). `PriceRulesTest` 23개(가격대 경계, 1원 미만 예외, 반올림, 상·하한)
  - `stock/VirtualPriceProvider`(`StockPriceProvider` 구현체): 종목별 상태 `PriceState` record를 `ConcurrentHashMap`에 보관, `@Scheduled(fixedRate = 5000)`로 전 종목 갱신, `ApplicationReadyEvent`에서 DB 시작가로 메모리 채움
  - 변동 공식(ADR 029 초기값 상수): 랜덤(√0.3 시장 + √0.7 종목, 5초 변동성 = 하루 변동성 ÷ √17,280) + 평균 회귀(반감기 0.5일) + 한방형 급등락(하루 0.3회, 15~40%) → `exp` → ±30% 자르기 → 호가 반올림
  - `global/config/SchedulingConfig`(`@EnableScheduling`), `FixedPriceProvider` 삭제
  - 이번 PR 범위 밖: 오늘 시작가(`dayOpenPrice`)는 시작가와 같은 값으로 고정, 날짜 갱신·DB 저장·복구·상수 yaml 이동은 PR 3. 엔진 결과 분포는 자동 테스트 없음(랜덤, 플레이 테스트로 확인)
  - 로컬 확인: 로그 `가상 시세 시작: 20개 종목`, KK0001을 10초 간격 6회 매수 시 체결가 51,600 → 51,800 → 51,900 → 51,800 → 52,000 → 52,200 (100원 호가 단위로 변동)
- 일정 재조정 (2026-10-25, `decisions/039`): 10/16~10/25 개발 공백(코스콤 필기 준비)으로 런칭 10/29 → 11/14, 기능 축소 없음

### 아직 안 한 것
- 회원·매매 외 도메인의 Service·Controller·DTO
- 시세 PR 3: 현재가·오늘 시작가 1분 주기 DB 저장과 재시작 복구(`decisions/040`), 날짜(KST) 바뀌면 오늘 시작가 갱신, 변동 상수 `application.yaml` + `@ConfigurationProperties`, 종목 조회 API(`GET /api/stocks`)
- 실거래가 배치 (`@Scheduled`)
- WebSocket 평가액 푸시
- 프론트 데이터 연결: 시드 선택 외 화면은 목업 상수 (홈의 현금·평가액 등)
- 화면 가드(`docs/ui-spec.md`): 회원 조회 API(`/api/members/me`) + `useMemberQuery` 만든 뒤 적용. 그 전까지 보호는 서버 API(SecurityConfig, signupToken)가 담당
- 거래내역 조회 API: 기간·구분 필터 목록(페이징), 기간 합계(실현손익·매수·매도), 종목별 실현손익 집계 (ADR 032). 11/1, 화면은 이미 있음(목업 → 연결)
- `@Valid` 실패 응답 형식: `GlobalExceptionHandler`가 `BusinessException`만 처리해서 Spring 기본 바디(`timestamp`, `status`, `error` 등)로 나감. `{code, message}`로 맞출지는 종목·매매 화면 연결(10/30~31) 때 결정

### 외부 대기 · 결정 필요
- 주식 시세: KIS와 공공데이터 주식시세정보 모두 서비스 사용 불가 확인(2026-10-06) → 서버가 만드는 가상 시세로 결정 (`decisions/020`, `failures/001`, `failures/002`). 개발 중에는 임시 `FixedPriceProvider`(고정가) 사용
  - 변동 방식은 `decisions/029`로 확정, 초기값도 029에 기록(2026-10-07). 구현 후 플레이 테스트로 조정
  - 현재가 저장·복구(`decisions/040`), 종목 유형 배치·시작가·호가 단위·`market` 삭제(`decisions/041`) 확정 (2026-10-27)
  - 정리 필요: `application.yaml`, `docker-compose.yml`의 KIS 키 설정 (별도 PR)
- 국토부 실거래가 API: 활용신청 완료 (키는 `.env`의 `MOLIT_SERVICE_KEY`). 이용허락범위 제한 없음 확인 (2026-10-06)
- AWS: EC2·RDS 운영 중. 후속 과제로 런칭 후 SSM 전환 검토 (22번 포트 닫기, `decisions/027`)
- 무중단 배포(blue-green): 지금은 EC2 1대·backend 컨테이너 1개라 배포 때 Spring 기동 시간(수십 초) 동안 `/api` 중단. 런칭 후 과제로 검토. 메모리 시세(`decisions/029`, `040`)가 서버마다 따로 생기지 않도록 시세 생성을 한 곳으로 모으는 설계가 먼저 필요 (2026-10-27)
- 자연어 아파트 검색: 조건부 포함 (`decisions/038`, `039`). 11/11 게이트 통과 시 11/13에 추가 (`decisions/023` 필터 JSON)
- 쿠키 없는 401 응답에 `JSESSIONID` 쿠키 발급 (2026-10-27 로컬에서 발견). 원인 기록 없음, 런칭 전 확인
- 운영 메모
  - EC2를 꺼 둔 동안 main에 merge하면 `deploy` 잡이 "서버 믿는다고 등록"(`ssh-keyscan`)에서 실패. EC2를 켠 뒤 마지막 deploy 실행의 실패한 잡만 다시 실행 (10/8~10/27 배포 공백 사례)
  - 실서버 접속은 `http://`를 붙여서. HTTPS 전환 전에는 Chrome이 https(443)로 먼저 시도해 타임아웃

### 다음 할 일 (순서)
1. (10/27 완료) 매수·매도 (심장 ①): PR #38 merge, 실서버 확인
2. 10/27~29 시세 제공자 (PR 3개): PR 1 종목 카탈로그(#39, 10/27 완료) → PR 2 가상 시세 엔진(10/27 로컬 확인) → PR 3 현재가 저장·복구(ADR 040) + 종목 조회 API
3. 10/30 회원 조회 API(`/api/members/me`) + `useMemberQuery` + 화면 가드
4. 10/30~31 종목·매매 화면 연결
5. 11/1 거래내역 조회 API·내 거래 화면 연결 → 중간 점검 (매수·매도 실서버 화면 동작)
6. 11/2 평가액 계산 (심장 ②) + 홈 화면 연결
7. 11/3 WebSocket 평가액 푸시
8. 11/4~5 실시간 평가액 차트 (심장 ④) + 손익 그래프
9. 11/6~7 실거래가 배치 (심장 ③), 11/7 오후 도메인·HTTPS (Secure 쿠키 켜기)
10. 11/8~9 추천 API + 아파트 화면 연결
11. 11/10~11 집 사기(계약서) → 내 마을, 변신 단계 → 11/11 게이트 체크
12. 11/12~13 통합 테스트·버그 수정 (13일은 버퍼 겸용), 11/13 자연어 아파트 검색(게이트 통과 시)
13. 11/14 캡처 화면 다듬기 + README + 최종 점검 → 런칭
- 버퍼 약 5일 포함 (1·2·4·8·10번에 하루씩). 11/1 중간 점검에서 밀렸으면 그때 일정 다시 논의 (`decisions/039`)
- 배포는 마지막 주에 몰지 않음. 기능은 PR merge 때마다 실서버에 바로 반영

## 참고
- 노션 일정표(간트): FRTR 페이지 > "6. 개발 일정" DB (10/25 재조정 반영)
- Figma 시안 사본: 데스크톱 10개, 모바일 7개 (모바일 사기·팔기, 계약서, 내 마을은 Figma 무료 플랜 MCP 호출 한도로 미완). 기준은 Claude Design 시안 v0.2
- 화면 ↔ 파일 대응표: `frontend/README.md`
- 결정 이유·실패 기록·용어: `docs/decisions/`, `docs/failures/`, `docs/domain/` (목록은 `docs/README.md`)
