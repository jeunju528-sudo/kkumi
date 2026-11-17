# 꿈이 frontend

React + TypeScript (Vite) · react-router-dom

## 실행

```bash
cd frontend
npm install
npm run dev      # http://localhost:5173
```

검증: `npm run lint && npm run build`

## 화면 (시안 v0.2)

| 경로 | 화면 | 파일 |
|---|---|---|
| / | 1 시작 | pages/WelcomePage.tsx |
| /seed | 1-1 시드 선택 | pages/SeedPickPage.tsx |
| /home | 2 홈 | pages/HomePage.tsx |
| /trade | 3 종목 목록 | pages/TradePage.tsx |
| /trade/search | 3-1 종목 검색 | pages/StockSearchPage.tsx |
| /trade/:code | 3-2 사기·팔기 | pages/OrderPage.tsx |
| /trade/history | 3-3 내 거래 | pages/TradeHistoryPage.tsx |
| /apartments | 4 부동산 | pages/ApartmentsPage.tsx |
| /contract/:dealId | 5 계약서 | pages/ContractPage.tsx |
| /village | 6 내 마을 | pages/VillagePage.tsx |
| /tiers | ★ 변신 4단계 | pages/TiersPage.tsx |

가드: `/`, `/seed`를 뺀 라우트는 `pages/MemberGuard.tsx`(레이아웃 라우트)로 감싼다. 회원 조회(`useMemberQuery`)가 실패하면 `/` (docs/decisions/043)

공통 헤더: 가드 안쪽 라우트는 `pages/AppLayout.tsx`가 `components/AppHeader.tsx`(로고, 메뉴, CASH, 오늘 마감 시계, 종 + 알림 드롭다운 `components/NotificationPanel.tsx`)를 한 번 그리고 `<Outlet />`에 화면을 넣는다. 페이지에 헤더를 다시 그리지 않는다 (docs/decisions/060)

## 현재 상태

- 시드 선택(가입)과 화면 가드는 API 연결 완료. 나머지 화면은 페이지 상단의 목업 상수로 그려진다. WebSocket 연동 전
- 공용: components/PixelDefs.tsx(픽셀 스프라이트 심볼), components/Sprite.tsx, utils/format.ts, types/api.ts
- 데이터: api/apiError.ts·memberApi.ts, hooks/queryKeys.ts(쿼리 키는 여기서만)·useMemberQuery.ts·useSelectSeedMutation.ts
- 스타일: 페이지별 CSS, 페이지 루트 클래스(.page-xxx)로 범위 제한. 색 토큰은 index.css
- 폰트: Noto Sans KR·Press Start 2P(Google Fonts), Galmuri11 Bold(OFL, assets/fonts)
- 브레이크포인트 900px (미만은 모바일 레이아웃 + 하단 탭 바, 탭 바는 `AppHeader.css`)
