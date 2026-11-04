// 백엔드 DTO와 이름을 맞춰둔 타입 설명서
// 타입은 컴파일 시점 검사용이라 브라우저로 가는 js에는 생기지 않음
// 금액은 모두 원 단위 정수 (백엔드 long)

// backend의 enum SeedMoney {SMALL, LARGE} 와 맞춘 것 MEDIUM 같은거 들어가면 에러남
export type SeedMoney = 'SMALL' | 'LARGE' // 500만 / 5,000만
export type Tier = 'GEOLBAENGI' | 'SEOMIN' | 'JOKBU' | 'GAPBU' // 걸뱅이 / 서민 / 졸부 / 갑부
export type TradeType = 'BUY' | 'SELL'
export type Period = 'WEEK' | 'MONTH' | 'QUARTER' | 'ALL' // 내 거래 기간 필터 (백엔드 enum Period)
export type Affordability = 'OK' | 'NEED_SELL' | 'SHORT'

export type MemberResponse = {
  id: number
  nickname: string
  cash: number
}

// backend record SeedRequest(SeedMoney seedMoney)
// 요청 바디 모양 : JSON.stringify 하면 {"seedMoney":"SMALL"} 로 요청됨
export type SelectSeedRequest = {
  seedMoney: SeedMoney
}

// 실패 응답 body, code는 백엔드 ErrorCode의 enum 이름
// ≈ record ErrorResponse(String code, String message)
export type ErrorResponse = {
  code: string
  message: string
}

export type StockResponse = {
  code: string
  name: string
  price: number
  changeRate: number // 0.0123 = +1.23%
}

export type HoldingResponse = {
  code: string
  name: string
  quantity: number
  avgPrice: number
  price: number
}

export type PortfolioResponse = {
  cash: number
  initialCash: number // 시드머니 (500만 / 5,000만)
  stockValue: number
  totalAsset: number // 평가액 = 현금 + 주식 평가액
  profit: number // 평가액 − 시드머니
  profitRate: number // 0.0123 = +1.23%
  holdings: HoldingResponse[]
}

// POST /api/trades/buy, /api/trades/sell (엔드포인트 분리)
export type BuyStockRequest = {
  code: string
  quantity: number // 1주 이상
}

export type SellStockRequest = {
  code: string
  quantity: number // 1주 이상
}

export type AptRecommendResponse = {
  dealId: number
  complexName: string
  region: string
  area: number // ㎡
  price: number
  ownAmount: number // 집값 40%
  loanAmount: number // 집값 60%
  affordability: Affordability
  shortage: number // NEED_SELL: 현금 부족액, SHORT: 평가액 부족액
}

export type BuyHouseResponse = {
  houseId: number
  tier: Tier
  isTierUp: boolean
}

export type TradeHistoryResponse = {
  id: number
  code: string
  name: string
  tradeType: TradeType
  price: number // 체결가
  quantity: number
  realizedProfit: number | null // 매도만 값 있음
  tradedAt: string // ISO 8601, 시간대 표기 없음 (서버 KST 기준 LocalDateTime)
}

// GET /api/trades/history, 다음 묶음이 있으면 hasNext true
export type TradeHistoryPageResponse = {
  items: TradeHistoryResponse[]
  page: number // 0부터
  hasNext: boolean
}

// GET /api/trades/history/summary
export type TradeHistorySummaryResponse = {
  realizedProfit: number // 기간 실현손익 합
  soldCostBasis: number // 기간 매도 원가 합 (실현 수익률 분모)
  buyTotal: number
  sellTotal: number
  tradeCount: number
}

// GET /api/trades/history/by-stock
export type StockProfitResponse = {
  code: string
  name: string
  profit: number // 종목 실현손익 합
  costBasis: number // 판 수량의 매입 원가 합 (수익률 분모)
  sellCount: number
}
