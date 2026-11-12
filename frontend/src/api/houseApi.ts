import type { BuyHouseRequest, BuyHouseResponse } from "../types/api";
import { toApiError } from "./apiError";

export async function buyHouse(request: BuyHouseRequest): Promise<BuyHouseResponse> {
    const res = await fetch('/api/houses', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json'},
        body: JSON.stringify(request)
    })

    if(!res.ok) {
        throw await toApiError(res)
    }
    
    return res.json() as Promise<BuyHouseResponse>
}