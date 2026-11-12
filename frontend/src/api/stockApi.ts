import type { StockCandleResponse, StockPriceResponse, StockResponse } from "../types/api";
import { toApiError } from "./apiError";

export async function getStocks(): Promise<StockResponse[]> {
    const res = await fetch('/api/stocks');
    if(!res.ok){
        throw await toApiError(res)
    }
    return res.json() as Promise<StockResponse[]>
}

// 분봉 차트용 최근 24시간 1분 기록
export async function getStockPrices(code: string): Promise<StockPriceResponse[]> {
    const res = await fetch(`/api/stocks/${encodeURIComponent(code)}/prices`);
    if(!res.ok){
        throw await toApiError(res)
    }
    return res.json() as Promise<StockPriceResponse[]>
}

// 일봉 차트용 최근 30일 시가·고가·저가·종가
export async function getStockCandles(code: string): Promise<StockCandleResponse[]> {
    const res = await fetch(`/api/stocks/${encodeURIComponent(code)}/candles`);
    if(!res.ok){
        throw await toApiError(res)
    }
    return res.json() as Promise<StockCandleResponse[]>
}
