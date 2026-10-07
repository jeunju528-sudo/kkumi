# ui-spec.md — 꿈이 프론트엔드(React + TypeScript) UI 명세

> 화면 구조, 컴포넌트 계약, API 타입, 토큰 정의. 구현은 frontend/src/pages/*.tsx 참고.
> 디자인 규칙은 design.md, 도메인 규칙은 CLAUDE.md가 우선한다.
> 기준 시안: Figma "Desktop 1280" / "Mobile 390" 페이지 (900px 미만이면 모바일 레이아웃).

## 1. 폴더 구조

CLAUDE.md "디렉토리 구조"를 따른다: pages(화면) / components(재사용 UI) / api(REST) / ws(WebSocket) / hooks / types / utils.

## 2. 라우팅 (화면 ↔ 시안)

| 경로 | 화면 | 시안 이름 |
|---|---|---|
| / | Welcome | 1 · 시작 |
| /seed | SeedPick | 1-1 · 시드 선택 |
| /home | Home | 2 · 홈 |
| /trade | TradeList | 3 · 종목 목록 |
| /trade/search | StockSearch | 3-1 · 종목 검색 |
| /trade/:code | Order | 3-2 · 사기·팔기 |
| /trade/history | TradeHistory | 3-3 · 내 거래 |
| /homes | Homes | 4 · 부동산 |
| /contract/:dealId | Contract | 5 · 계약서 |
| /village | Village | 6 · 내 마을 |
| (모달/섹션) | Tiers | ★ 변신 4단계 |

가드: 미로그인 → /, 로그인했지만 시드 미선택 → /seed.

## 3. 화면별 컴포넌트 트리

### 공통 레이아웃
```
AppShell
  TopBar            # 데스크톱: 로고 + 메뉴 링크 + CASH 칩
  HudChips          # 모바일 홈/마을: 장면 위에 얹음
  <Outlet />
  BottomTabBar      # 모바일 전용(<900px)
```

### Welcome
```
Welcome
  Title             # 인생역전 / VS / 인생여전 (세로 3덩어리, 간격 10px)
  Tagline
  SeedGiftBadge
  KakaoLoginButton
  PixelScene        # 데스크톱 전용 장식(k-wide 포함), 말풍선
```

### SeedPick
```
SeedPick
  Mascot + SpeechBubble
  SeedOptionCard x2   # 500 | 5000, 선택 시 골드 배경
  StartButton         # "{N}원으로 시작하기"
```

### Home
```
Home
  PixelTownScene
  HudChips            # CASH, 장 마감 시계
  ValuationCard       # 내 평가액(롤링), 손익, 내 거래 링크(→/trade/history)
  GoalPanel           # 첫 집 목표 + 진행 바
  ValuationChart
  HoldingList > HoldingRow*
```

### TradeList / StockSearch / Order
```
TradeList    : SearchBar(→/trade/search), Tabs(내 종목|전체), StockRow*, 내 거래 링크(→/trade/history)
StockSearch  : SearchInput, RecentSearches, PopularStocks, ResultList, EmptyState
Order        : SideToggle(담기|팔기), QuantityStepper, BuyableInfo, TotalRow, SubmitButton
TradeHistory : RealizedSummary(실현손익, 매수·매도 금액, 건수), ProfitBreakdown(총손익 = 실현 + 평가),
               Tabs(거래내역|종목별 손익), PeriodFilter(1주|1달|3달|전체), KindFilter(전체|매수|매도),
               DayGroup > TradeRow*, StockProfitRow*, EmptyState
```

### Homes
```
Homes
  AffordBanner        # "내 평가액으로 살 수 있는 집"
  LoanNotice
  FilterBar           # 검색 + "살 수 있는 집만"
  HomeCard*           # 상태 3가지 (아래 5번)
```

### Contract / Village
```
Contract : ContractPaper, StampArea, TierUpToast, ShareButton, RetryButton
Village  : TierProgress, HouseCollection > HouseSlot*, EmptySlot*
```

## 4. API 타입

데이터 흐름: `api/xxxApi.ts`(fetch) → `hooks/useXxxQuery`(TanStack Query) → pages → components(props). 실시간 값은 `ws/` → `queryClient.setQueryData()`.

실제 타입은 `frontend/src/types/api.ts`가 기준이다 (백엔드 DTO 이름과 동일하게 유지). 금액은 모두 원 단위 정수.

## 5. 부동산 카드 상태 → UI 매핑

| affordability | 버튼 | 동작 |
|---|---|---|
| OK | 계약하기 | /contract/:dealId |
| NEED_SELL | 현금 {shortage}원 부족 · 주식 팔러 가기 | /trade |
| SHORT | {shortage}원 모자라요 (disabled) | 없음 |

## 6. 실시간 평가액 (WebSocket / STOMP)

```ts
// 구독 경로는 백엔드와 확정. 아래는 가칭.
export interface ValuationMessage {
  totalValue: number;
  profit: number;
  profitRate: number;
  at: string;              // ISO
}
// subscribe: /user/queue/valuation
```

UI 규칙: 값이 오르면 숫자가 위로, 내리면 아래로 롤링(0.4초 이내), 상승 --up, 하락 --down, 부호와 화살표 병기. prefers-reduced-motion이면 페이드.

## 7. 디자인 토큰 (src/index.css)

| 토큰 | 값 | 용도 |
|---|---|---|
| --bg | #F7F8FC | 모던 영역 배경 |
| --surface | #FFFFFF | 카드 |
| --text / --text-sub | #1B1B2F / #6B6F80 | 본문 / 보조 |
| --line | #E6E8F0 | 구분선 |
| --sky | #1E90E6 | 픽셀 영역 하늘 |
| --night | #231B3A | HUD 칩, 헤더 |
| --ink | #0F0B1F | 픽셀 테두리 |
| --grass / --soil | #6CC24A / #7A4B3A | 땅 |
| --point | #FFC93C | CTA, 평가액 |
| --up | #D32F36 (어두운 배경 #E5484D) | 상승/수익 |
| --down | #2563EB (어두운 배경 #3B82F6) | 하락/손실 |
| --success | #2FBF71 | 구매 성공 |

폰트: 본문 Noto Sans KR(또는 Pretendard), 라벨 Press Start 2P, 한글 도트 제목 Galmuri11 Bold. 숫자는 tabular-nums.
모서리: 모던 카드 16px, 픽셀 요소 0px + 2~3px 잉크 테두리 + 하단 오프셋 그림자.
브레이크포인트: 900px. 검증 폭 390 / 1280.

## 8. 상태 처리 체크리스트

- 모든 목록: 로딩 / 빈 상태(픽셀 캐릭터 + 한 줄) / 에러
- 주문: 잔액 부족, 보유 수량 초과, 장 마감
- 계약: 현금 부족(서버 검증이 최종), 이미 계약한 단지
- 모든 화면에 "가짜 돈입니다" 안내 유지
