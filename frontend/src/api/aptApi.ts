import { toApiError } from "./apiError";
import type { AptRecommendPageResponse, AptRecommendResponse } from "../types/api";

// 아파트 추천 내역 조회
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

// 거래 1건의 상세정보 및 살 수 있는지, 부족한 금액은 얼마인지 알려줌
export async function getAptDeal(dealId: number): Promise<AptRecommendResponse> {
    const res = await fetch(`/api/apartments/${dealId}`)
    if(!res.ok) {
        throw await toApiError(res)
    }
    return res.json() as Promise<AptRecommendResponse>
}