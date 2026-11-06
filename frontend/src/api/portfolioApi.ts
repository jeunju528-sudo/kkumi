import type { ChartPeriod, PortfolioHistoryResponse, PortfolioResponse } from "../types/api";
import { toApiError } from "./apiError";

export async function getMyPortfolio(): Promise<PortfolioResponse> {
    const res = await fetch('/api/portfolios/me')
    if(!res.ok){
        throw await toApiError(res)
    }
    return res.json() as Promise<PortfolioResponse>
}

// 홈 손익 차트 일별 기록
export async function getMyPortfolioHistory(period: ChartPeriod): Promise<PortfolioHistoryResponse[]> {
    const res = await fetch(`/api/portfolios/me/history?period=${period}`)
    if(!res.ok){
        throw await toApiError(res)
    }
    return res.json() as Promise<PortfolioHistoryResponse[]>
}
