import type { BuyHouseRequest, BuyHouseResponse, MyHousesResponse } from "../types/api";
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

export async function getMyHouses(): Promise<MyHousesResponse> {
    const res = await fetch('/api/houses')
    if(!res.ok) {
        throw await toApiError(res)
    }
    return res.json() as Promise<MyHousesResponse>
}