import type { StockResponse } from "../types/api";
import { toApiError } from "./apiError";

export async function getStocks(): Promise<StockResponse[]> {
    const res = await fetch('/api/stocks');
    if(!res.ok){
        throw await toApiError(res)
    }
    return res.json() as Promise<StockResponse[]>
}