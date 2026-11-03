// 타입만 가져오는 import
import type {
  Period,
  StockProfitResponse,
  TradeHistoryPageResponse,
  TradeHistorySummaryResponse,
  TradeType,
} from '../types/api'
import { toApiError } from './apiError'

// type null이면 전체 (백엔드 type 생략 = 전체), 'ALL' → null 변환은 페이지에서
export async function getTradeHistory(period: Period, type: TradeType | null, page: number): Promise<TradeHistoryPageResponse> {
    // URLSearchParams : 쿼리스트링 생성기, ?·&·= 직접 안 붙여도 되고 인코딩도 자동
    // 값은 문자열만 받아서 page는 String()으로 변환
    const params = new URLSearchParams({period, page: String(page)})
    // 값 있을 때만 type 추가 ('type=null' 글자가 서버로 가지 않게)
    if(type !== null){
        params.set('type', type)
    }
    // 템플릿 문자열에 params를 넣으면 'period=MONTH&page=0&type=SELL' 형태로 바뀜
    const res = await fetch(`/api/trades/history?${params}`)
    if(!res.ok){
        throw await toApiError(res)
    }
    return res.json() as Promise<TradeHistoryPageResponse>
}

export async function getTradeHistorySummary(period: Period): Promise<TradeHistorySummaryResponse> {
    const params = new URLSearchParams({period})
    const res = await fetch(`/api/trades/history/summary?${params}`)
    if(!res.ok) {
        throw await toApiError(res)
    }
    return res.json() as Promise<TradeHistorySummaryResponse>
}

export async function getStockProfits(period: Period): Promise<StockProfitResponse[]> {
    const params = new URLSearchParams({period})
    const res = await fetch(`/api/trades/history/by-stock?${params}`)
    if(!res.ok) {
        throw await toApiError(res)
    }
    return res.json() as Promise<StockProfitResponse[]>
}