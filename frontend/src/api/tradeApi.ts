import type { BuyStockRequest, SellStockRequest } from "../types/api";
import { toApiError } from "./apiError";

// Promise<void> : 서버에서 넘어오는 값에 body 없을 때 void로 받음
export async function buyStock(request: BuyStockRequest): Promise<void> {
    const res = await fetch('/api/trades/buy', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request),
    })
    if(!res.ok){
        throw await toApiError(res)
    }
}

export async function sellStock(request: SellStockRequest): Promise<void> {
    const res = await fetch('/api/trades/sell', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(request),
    })
    if(!res.ok){
        throw await toApiError(res)
    }
    // 서버가 바디없이 200을 주기 때문에 빈 바디에 .json을 하면 에러가 나기 때문에 안함
}