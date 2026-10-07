// 백엔드 DTO와 이름 맞춤. 필드는 API 확정 후 갱신
// 금액은 모두 원 단위 정수 (백엔드 long)

export type SeedMoney = 'SMALL' | 'LARGE' // 500만 / 5,000만
export type Tier = 'GEOLBAENGI' | 'SEOMIN' | 'JOKBU' | 'GAPBU' // 걸뱅이 / 서민 / 졸부 / 갑부
export type TradeType = 'BUY' | 'SELL'
export type Affordability = 'OK' | 'NEED_SELL' | 'SHORT'

export type MemberResponse = {
  id: number
  nickname: string
  hasSelectedSeed: boolean
  cash: number
  tier: Tier
  houseCount: number
}

export type SelectSeedRequest = {
  seedMoney: SeedMoney
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
  stockValue: number
  totalAsset: number // 평가액 = 현금 + 주식 평가액
  profit: number
  profitRate: number
  holdings: HoldingResponse[]
}

export type TradeRequest = {
  code: string
  type: TradeType
  quantity: number
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
  tradedAt: string // ISO 8601
}
