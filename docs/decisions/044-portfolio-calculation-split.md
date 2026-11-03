# 044. 평가액 계산: 조회(Service)와 계산(순수 클래스) 분리

## 상태
확정 (2026-11-03)

## 배경
평가액은 홈 조회 API(`GET /api/portfolios/me`)와 WebSocket 푸시(5초 주기, 11/4 예정) 두 곳에서 계산한다.
Service 안에서 조회와 계산을 함께 하면 테스트마다 Repository·시세 mock이 필요하고, 푸시 쪽에서 재사용하기 어렵다.

## 결정
`PortfolioService`는 회원(현금·시드머니)과 보유 종목(`HoldingService.findHoldings`, 현재가 포함) 조회만 하고, 계산은 DB·시세를 모르는 `PortfolioCalculator`(final 클래스, static 메서드)가 맡는다.
응답은 `PortfolioResponse`(현금, 시드머니, 주식 평가액, 평가액, 총손익, 총수익률, 보유 종목).

## 이유
- 입력(현금, 시드머니, 수량·현재가)만으로 결과가 정해져 mock 없이 숫자로 테스트 가능 (`PriceRules`와 같은 방식)
- WebSocket 푸시에서 같은 계산을 그대로 호출
- 보유 행은 `/api/holdings`와 같은 `HoldingResponse`를 재사용해 화면 코드도 공유
- 총손익은 용어 사전 정의대로 평가액 − 시드머니. 내 거래 카드의 평가손익은 총손익 − 실현손익으로 역산해 합이 항상 맞게 함 (평균 매입가 반올림 차이 흡수)

## 포기한 대안
- Service 안에서 계산: 파일 수는 적음 / 테스트에 mock 필요, 푸시 쪽 중복. 포기
- 총손익 = 실현손익 + 평가손익 따로 합산: 화면 의미는 같음 / 평균 매입가 반올림으로 평가액 − 시드머니와 몇 원 어긋날 수 있음. 포기
- 계산기를 `@Component` 빈으로: mock 교체 가능 / 상태 없는 계산이라 빈일 이유 없음. 포기

## 결과
금액은 `long` + `Math.multiplyExact`·`addExact`·`subtractExact`(넘침이면 예외), 수익률은 `BigDecimal` 소수 4자리 HALF_UP (`changeRate`와 같은 규칙).
종목별 현재가를 따로 조회해 5초 갱신 경계에서 종목 간 시세 시점이 다를 수 있음(게임 허용 범위).
이 결정을 번복하려면 먼저 개발자와 논의 필요. 에이전트는 계산 로직을 Service나 Controller로 옮기는 제안을 하지 않는다. 관련: 036, 045.
