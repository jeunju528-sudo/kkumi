# CLAUDE.md

## 프로젝트 개요
- 서비스: 꿈이 — 가짜 시드머니로 실제 종목 투자 → 번 돈으로 실거래가 기준 아파트를 사는 시뮬레이션 웹게임
- 런칭 예정 일자: 2026-10-24
- Backend: Java 21, Spring Boot 4.1, Spring Data JPA, MySQL(RDS), WebSocket(STOMP)
- Frontend: React + TypeScript (Vite), TanStack Query, ESLint
- 인프라: Docker Compose, GitHub Actions, AWS(EC2 + RDS)
- MVP 4개
  - ① 카카오 로그인 → 시드 선택(500만/5,000만) + 매수·매도
  - ② 평가액·손익 실시간 갱신
  - ③ 내 돈 40%로 살 수 있는 아파트 추천 (나머지 60%는 가짜 대출)
  - ④ 집 사기(계약서) → 내 마을(도감) 컬렉션 + 변신 4단계
- 상세 설계: 노션 https://app.notion.com/p/From-Rags-To-Riches-FRTR-3db93c322ab780709c78f259c7ad4598
- UI 기준: `docs/design.md` (v0.2). 화면 구현·리뷰 전에 반드시 읽을 것
- 진행 현황: `docs/progress.md`. 세션 시작 시 먼저 확인, 작업 끝나면 갱신

## 참고 문서
작업 전 아래 문서를 반드시 확인할 것. 구조와 작성 규칙은 `docs/README.md`.
- 기술 결정: `docs/decisions/` — 왜 이 구조인지 이유가 담겨 있음. 확정된 결정을 번복하는 제안은 하지 않고, 필요하면 개발자와 먼저 논의
- 실패 기록: `docs/failures/` — 시도했다가 포기한 방법 목록. 같은 방법을 다시 제안하지 않음
- 용어 사전: `docs/domain/glossary.md` — 도메인 용어의 정확한 의미
- 업무 흐름: `docs/domain/workflows.md` — 가입, 매수·매도, 추천, 집 사기 흐름
- 코딩 규칙 상세: `docs/conventions/` — 올바른 패턴 / 금지 패턴

## 도메인 규칙 (확정)
- 로그인: 카카오 로그인만 사용. ID/비밀번호 로그인 없음
- 가입 흐름: 카카오 로그인 → 시드머니 선택(500만/5,000만) → 홈. 시드 선택은 가입 직후 1회
- 주식 매수: 현금으로만 산다
- 집 추천: 집값의 40%는 내 돈, 60%는 가짜 대출. 평가액(현금 + 주식 평가액)이 집값×40% 이상이면 살 수 있는 집으로 추천
- 변신 4단계: 보유 집 수로 결정. 걸뱅이(0채) / 서민(1채) / 졸부(2~4채) / 갑부(5채 이상)
- 계약서: 집 사기 확정 시 보여주는 연출. 별도 법적·금융 의미 없음
- 내 마을(도감): 산 집을 모아 보는 화면. 집 팔기는 없음
- 집 결제: 현금에서만 40%를 차감한다. 추천은 평가액 기준이지만 결제는 현금 기준. 현금이 모자라면 사용자가 직접 주식을 팔아 현금을 만든 뒤 결제한다 (자동 매도 없음)
- 주식은 집을 산 뒤에도 유지된다 (포트폴리오 초기화 없음)
- 상세 용어·흐름은 `docs/domain/`. 도메인 규칙을 바꿀 땐 여기와 `docs/domain/`을 같이 수정

## 디렉토리 구조
```
kkumi/
├── backend/src/main/java/com/kkumi/
│   ├── member/      # 회원(카카오 로그인), 시드머니
│   ├── stock/       # 종목, 시세 조회 (StockPriceProvider)
│   ├── trade/       # 매수·매도, 보유 종목
│   ├── portfolio/   # 평가액, WebSocket 푸시
│   ├── apartment/   # 실거래가, 배치, 추천
│   ├── house/       # 집 사기, 컬렉션, 변신 단계
│   └── global/      # config, error, 공통 응답만. 도메인 로직 금지
├── frontend/src/
│   ├── pages/       # 화면 단위. API 직접 호출 금지
│   ├── components/  # 재사용 UI. 상태는 props로만 받음
│   ├── api/         # REST 호출 함수만
│   ├── ws/          # WebSocket 연결·구독만
│   ├── hooks/       # 커스텀 훅, TanStack Query (useQuery/useMutation은 여기서만)
│   ├── types/       # API 요청·응답 타입
│   └── utils/       # 순수 함수만 (formatKRW 등)
└── docs/            # 지식 저장소 (구조·작성 규칙은 docs/README.md)
```
- 도메인 패키지 안 구조: `XxxController`, `XxxService`, `XxxRepository`, 엔티티는 패키지 루트, DTO는 `{도메인}/dto/`

## 백엔드 코드 규칙
- 계층: Controller → Service → Repository. Controller에서 Repository 직접 호출 금지
- Controller: 요청 검증(`@Valid`)과 DTO 변환만. `if`로 비즈니스 분기 금지
- URL: `/api/{복수형 리소스}` (e.g. `POST /api/trades/buy`, `GET /api/houses`)
- Service: 클래스에 `@Transactional(readOnly = true)`, 쓰기 메서드만 `@Transactional`
  - `org.springframework.transaction.annotation.Transactional`만 사용 (`jakarta.transaction.Transactional` 금지)
- 의존성 주입: 생성자 주입(`@RequiredArgsConstructor` + `private final`)만 사용
- 엔티티
  - `@Setter` 금지 → 상태 변경은 의미 있는 메서드로 (e.g. `withdraw(amount)`, not `setCash()`)
  - `@NoArgsConstructor(access = AccessLevel.PROTECTED)` + 정적 팩토리(`of`, `create`)로 생성
  - 연관관계는 `FetchType.LAZY` 필수
  - Enum은 `@Enumerated(EnumType.STRING)` 필수
- DTO: Java `record` 사용. 이름은 `{행위/대상}{Request|Response}` (e.g. `BuyStockRequest`, `HoldingResponse`)
- API 응답에 엔티티 직접 반환 금지 → 항상 Response DTO로 변환
- 금액: 원 단위 `long`. 평균 매입가만 `BigDecimal`. `double`/`float` 금지
- 예외: `BusinessException(ErrorCode)` 사용, `@RestControllerAdvice`에서 일괄 처리
  - `ErrorCode`는 `global/error/`에 enum으로 관리 (e.g. `INSUFFICIENT_CASH`)
- 네이밍
  - 메서드는 동사로 시작 (`findHoldings`, `calculateTotalAsset`)
  - boolean은 `is`/`has` 접두사 (`isCanceled`, `hasEnoughCash`)
  - 조회: 없을 수 있으면 `findXxx` → `Optional` 반환, 없으면 예외를 던질 땐 `getXxx`
- 외부 API 키: `application.yaml`의 `external.*` + `@ConfigurationProperties`로 주입. `@Value` 남발 금지
- 시세 조회: `StockPriceProvider` 인터페이스로만 호출. KIS 클라이언트 직접 호출 금지
- Spring Boot 4: Jackson은 `tools.jackson.*` 패키지. 3.x 예제 코드는 import 확인 후 사용
- 주석: 짧은 명사형 (e.g. `// 수량 0이면 행 삭제`). "~한다." 서술형 금지

## 프론트 코드 규칙
- 컴포넌트: 함수형만. 파일명 PascalCase (`StockCard.tsx`), 컴포넌트 하나당 파일 하나
- 훅: `use` 접두사, `src/hooks/useXxx.ts`
- export: named export만 사용 (`export function StockCard`). `export default` 금지
- props 타입: `type StockCardProps = { ... }` 형태로 컴포넌트 위에 선언. `interface` 금지 → `type`
- 타입
  - `any` 금지 → `unknown` + 타입 가드
  - API 응답 타입은 `src/types/`에, 이름은 백엔드 DTO와 동일 (`HoldingResponse`)
- API 호출: `src/api/xxxApi.ts`의 함수로만. 컴포넌트·페이지에서 `fetch` 직접 호출 금지
- WebSocket: `src/ws/`에서만 연결. `useEffect` cleanup에서 구독 해제 필수
- 상태 관리
  - 서버 데이터(보유 종목, 시세, 집 목록 등): TanStack Query
  - 클라이언트 전용 상태(로그인 회원 등): `useContext`, 화면 내부 상태: `useState`
  - Redux, Zustand 등 전역 상태관리 라이브러리 금지
- TanStack Query
  - `useQuery` / `useMutation`은 `src/hooks/`에서만. 이름은 `useXxxQuery`, `useXxxMutation` (e.g. `useHoldingsQuery`, `useBuyHouseMutation`)
  - pages는 훅만 호출, components는 TanStack Query import 금지 (props로만)
  - 쿼리 키는 `src/hooks/queryKeys.ts`에서만 정의. 문자열 배열 직접 입력 금지
  - 쓰기 후 관련 쿼리 `invalidateQueries` (e.g. 집 사기 → 회원 현금, 내 마을)
  - WebSocket 실시간 값은 `queryClient.setQueryData()`로 캐시에 반영
- 금액 표시: `src/utils/format.ts`의 `formatKRW()` 사용. 컴포넌트에서 직접 포맷 금지
- boolean 변수·props: `is`/`has` 접두사 (`isLoading`, `hasHolding`)
- 이벤트 핸들러: `handle` 접두사 (`handleBuyClick`), props로 넘길 땐 `on` 접두사 (`onBuy`)
- 비교: `===` / `!==`만 사용
- 화면 구성·색·간격·폰트·카피는 `docs/design.md`와 시안(v0.2)을 따른다. 시안에 없는 화면은 임의로 추가하지 않는다

## 프론트 분담
- Claude 가능: 시안(v0.2)을 화면으로 옮기는 작업
  - pages·components의 JSX 레이아웃, `*.css`, `index.css` 색 토큰, 폰트·스프라이트 등 assets
  - 라우팅 뼈대, 페이지 상단 목업 상수, `types/`, `utils/` 포맷 함수
- 개발자 직접 (Claude는 방향 제시·리뷰만)
  - `src/api/`, `src/hooks/`(TanStack Query), `src/ws/`
  - pages의 목업 상수 → 실제 데이터 연결, 로딩·에러 처리
  - 상태(`useState`/Context), 입력 검증, 이벤트 핸들러 로직 (매수·매도, 집 사기 등)
  - 실시간 평가액 차트 (WebSocket 구독, cleanup, 리렌더 최적화)
- 경계가 애매하면 "화면 모양이면 Claude, 화면 동작이면 개발자"

## 작성 예시
- 코드 작성 전 아래 예시 패턴 확인 (올바른 패턴 / 금지 패턴)

@docs/conventions/backend.md
@docs/conventions/frontend.md
@docs/conventions/testing.md

## 절대 금지
- 아래 "직접 작성 영역"의 구현 코드 작성·수정 금지 (방향 제시 → 개발자 작성 → 교정만)
  - `trade/` 서비스의 매수·매도 잔액·보유량 계산
  - `portfolio/` 평가액 계산 로직
  - `apartment/` 실거래가 배치 수집 로직
  - 프론트 동작 구현 (아래 "프론트 분담" 참고)
  - 위 영역에서 허용: 인터페이스, DTO, TS 타입, 테스트 메서드 이름 뼈대
- 비밀값 커밋 금지 → `.env`(로컬), GitHub Secrets(CI), EC2 `.env`(운영). 카카오 클라이언트 시크릿 포함
- `main` 브랜치 직접 push 금지 → 브랜치 + PR
- 운영(prod) 프로필에 `ddl-auto: create` / `create-drop` 금지
- 새 기술 추가 금지: Kafka, Kubernetes, Redis, Elasticsearch, MSA, Next.js, 전역 상태관리 라이브러리(Redux, Zustand 등)
- MVP 범위 밖 기능 구현 금지: 빌딩, 랭킹, 친구 비교, 집 팔기, 집값 하락 표시, 네이티브 모바일앱 (모바일 웹 반응형은 MVP 안)
- 커밋 메시지·PR·주석에 AI 작성 표시 금지 (Co-Authored-By, "Generated with" 등)
- `System.out.println` 금지 → `@Slf4j` + `log.info()`
- `console.log` 커밋 금지

## 자동 검사
- 클론 후 최초 1회: `git config core.hooksPath .githooks`
- pre-commit (`.githooks/pre-commit`)
  - `main` 브랜치에서 커밋 차단
  - `.env` 파일 커밋 차단 (`.env.example` 제외)
  - `backend/`의 소스·빌드 설정 변경 시 `./gradlew test` (ArchUnit 포함)
    - 대상: `src/`, `build.gradle`, `settings.gradle`, `gradle.properties`, `gradle/`, `gradlew*`
    - 제외: `Dockerfile`, `.dockerignore` 등 인프라 파일 (CI가 전체 검사)
  - `frontend/` 변경 시 `npm run lint` + `tsc -b`
- commit-msg (`.githooks/commit-msg`): 접두사 형식, AI 작성 문구 검사
- 검사 실패 시 커밋 불가. `--no-verify` 우회 금지
- 코드로 강제되는 규칙
  - 백엔드 `ArchitectureTest`: Controller→Repository 참조, Controller 엔티티 반환, global→도메인 참조, Service `@Transactional` 누락, `jakarta.transaction.Transactional`, 엔티티 setter, 엔티티·DTO `double`/`float`, 필드 주입, `System.out`, `java.util.logging`
  - 프론트 `eslint.config.js`: `console`, `any`, `interface`, `export default`, `==`, 미사용 변수, 전역 상태관리 라이브러리, pages·components·api·ws·types·utils의 `@tanstack/react-query`, hooks의 쿼리 키 직접 입력, pages·components·hooks의 `fetch`/`WebSocket`, components→api·ws·pages, api·ws·types→UI 레이어, utils→React·다른 레이어
- 규칙 추가·변경 시 이 문서 + `ArchitectureTest` / `eslint.config.js` 같이 수정
- 코드로 못 잡는 규칙(네이밍, boolean 접두사, WebSocket cleanup 등)은 PR 리뷰에서 확인
- CI (GitHub Actions, `.github/workflows/ci.yml`)
  - 훅은 `--no-verify`로 우회 가능 → PR마다 CI에서 같은 검사 재실행
  - 트리거: `main` 대상 PR. 새 커밋 오면 이전 실행 취소
  - backend 잡: `./gradlew test` (ArchUnit 포함, Java 21)
  - frontend 잡: `npm ci && npm run lint && npm run build` (Node 22, `tsc -b` 포함)
  - docker 잡: backend·nginx 이미지 빌드 + `nginx -t` (Dockerfile·nginx.conf 검증용, 필수 체크 등록은 선택)
  - 실패하면 merge 불가: GitHub > Settings > Branches 보호 규칙에 필수 체크 `backend`, `frontend` 등록 (개발자가 직접 설정)
  - 잡 이름(`backend`, `frontend`) 변경 금지. 바꾸면 필수 체크 재등록 필요
  - 워크플로에 `paths` 필터 금지. 필수 체크가 skip되면 PR이 pending에서 안 풀림

## PR 규칙
- 브랜치명: `{type}/{kebab-case}` (e.g. `feat/stock-buy`, `fix/holding-avg-price`)
- 커밋 메시지: `feat:`, `fix:`, `refactor:`, `chore:`, `docs:`, `test:` 접두사 + 한국어 요약
- PR 하나에 기능 하나
- PR 올리기 전 `./gradlew build`, `npm run lint`, `npm run build` 통과 필수
- merge는 Squash and merge만

## 테스트
- 테스트가 기대 동작의 기준. 구현 전에 테스트 먼저 작성 → 테스트 통과하는 방향으로 구현
- 직접 작성 영역(심장)은 개발자가 테스트부터 작성. Claude는 테스트 메서드 이름 뼈대만 제안
- 백엔드 위치: `backend/src/test/java/com/kkumi/{도메인}/`
- 클래스명: `{대상}Test` (e.g. `TradeServiceTest`)
- 메서드: `@DisplayName("잔액 부족하면 매수 실패")` 한국어로 작성
- 구조: given / when / then 주석으로 구분
- 실행: `cd backend && ./gradlew test` (H2 MySQL 모드, DB 없이 실행)
- Service에 새 메서드 추가 시 성공 케이스 1개 + 실패 케이스 1개 이상 필수
- 프론트 검증: `cd frontend && npm run lint && npm run build`
- 경계값 필수: 규칙에 숫자 구간이 있으면 경계(1, 2, 4, 5)와 잘못된 값(음수)을 모두 테스트 (예시: `docs/conventions/testing.md`)

## 기록
- 결정 지점(A vs B)이 생기면 `docs/decisions/`에 ADR 추가 제안. 양식은 `docs/decisions/_template.md`, 번호는 `docs/decisions/README.md`의 "다음 번호". 목록도 같이 갱신
- 시도했다가 포기한 방법은 `docs/failures/`에 추가 제안. 양식은 `docs/failures/_template.md`
- 에러 해결 시 `docs/troubleshooting.md`에 추가 제안: `증상 / 원인 / 해결 / 배운 점`
- 도메인 규칙·용어가 바뀌면 `docs/domain/`도 같이 수정
- 문서 작성 시 `docs/README.md`의 구조와 작성 규칙(근거 없는 내용은 "기록 없음"으로 표기)을 따름
