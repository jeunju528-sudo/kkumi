import type { PortfolioResponse } from "../types/api";
import { toApiError } from "./apiError";

export async function getMyPortfolio(): Promise<PortfolioResponse> {
    const res = await fetch('/api/portfolios/me')
    if(!res.ok){
        throw await toApiError(res)
    }
    return res.json() as Promise<PortfolioResponse>
}