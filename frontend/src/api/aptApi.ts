import { toApiError } from "./apiError";
import type { AptRecommendPageResponse } from "../types/api";

export async function getAptRecommendations(q: string, isAffordableOnly: boolean, page: number): Promise<AptRecommendPageResponse> {
    // q: 검색어(빈 문자열이면 생략), isAffordableOnly: 살 수 있는 집만, page: 0부터
    const params = new URLSearchParams({affordableOnly: String(isAffordableOnly), page: String(page) })
    if (q !== '') {
        params.set('q', q)
    }   
    const res = await fetch(`/api/apartments?${params}`)
    if(!res.ok) {
        throw await toApiError(res)
    }
    return res.json() as Promise<AptRecommendPageResponse>
}