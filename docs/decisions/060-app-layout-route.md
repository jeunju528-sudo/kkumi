# 060. 공통 헤더: 레이아웃 라우트(AppLayout)로 한 번만 렌더

## 상태
확정 (2026-11-17)

## 배경
- 로그인 화면 9개가 헤더(로고, 메뉴 4개, CASH 칩) 마크업을 각자 복붙. CSS도 페이지 CSS 10개에 같은 규칙 반복(`.page-xxx .k-top` 등)
- Kafka 하락 알림(059)으로 헤더에 종(안 읽은 수)과 오늘 마감 시계를 모든 화면에 추가해야 함
- 시계는 1초마다 갱신. 페이지가 시계 훅을 들고 있으면 페이지 전체가 1초마다 다시 렌더링됨 (주문 화면 캔들 차트 포함)

## 결정
`MemberGuard` 아래에 레이아웃 라우트 `pages/AppLayout`을 한 겹 두고, 헤더(`components/AppHeader`)를 여기서 한 번만 렌더한다. 페이지는 `<Outlet />` 자리에 들어간다.

## 이유
- 헤더 마크업·CSS 한 곳 (`components/AppHeader.tsx`, `AppHeader.css`)
- 1초 시계 리렌더 범위가 헤더로 한정. react-router 7 `useOutlet`이 라우터 context의 outlet element를 `useMemo`로 그대로 돌려줘서(소스 확인, `chunk-*.mjs` `useOutlet`), AppLayout이 다시 그려져도 페이지는 같은 element라 React가 건너뜀
- 화면 이동 시 헤더가 다시 마운트되지 않음
- 켜진 메뉴는 주소로 계산(`toActiveMenu`). 하위 화면도 부모 메뉴 (계약서 → 집 찾기, 변신 단계 → 내 마을)
- `AppHeader`·`BellButton`은 props만 받음 (CLAUDE.md "components는 props로만"). 현금 쿼리·시계 훅은 `AppLayout`(pages)에서 호출

## 포기한 대안
- 페이지마다 `<AppHeader active cash ... />`: 구조 단순 / 9곳에서 같은 훅 반복, 시계 때문에 페이지 전체 1초 리렌더
- 헤더 안에서 시계 훅 직접 호출: 리렌더 범위는 같음 / components는 props로만 받는 규칙 위반

## 결과
- 장면형 화면(홈, 내 마을)은 경로(`HERO_PATHS`)로 구분. 모바일에서 헤더 띠를 접고 칩(CASH, 시계, 종)만 장면 위에 얹음. 페이지가 따로 그리던 HUD 칩은 삭제
- 페이지 루트는 `min-height: 100vh` 대신 `flex: 1 0 auto` (헤더 64px만큼 늘 넘치는 것 방지)
- 로그인 전 화면(시작, 시드 선택)은 레이아웃 밖이라 기존 헤더 유지
- 새 로그인 화면은 `AppLayout` 아래 라우트로 추가. 헤더를 페이지에 다시 그리지 않음
이 결정을 번복하려면 먼저 개발자와 논의 필요. 에이전트는 페이지 안에 헤더 마크업을 다시 넣거나 시계 훅을 페이지로 옮기는 제안을 하지 않는다. 관련: 043, 050, 059.
