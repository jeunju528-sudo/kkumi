import { useQuery } from "@tanstack/react-query";
import { getMyPortfolioHistory } from "../api/portfolioApi";
import type { ChartPeriod } from "../types/api";
import { queryKeys } from "./queryKeys";

// 기록은 0시에 하루 1번 쌓여서 자주 다시 받을 필요 없음 (오늘 점은 WebSocket 평가액)
const HISTORY_STALE_MS = 10 * 60 * 1000

export function usePortfolioHistoryQuery(period: ChartPeriod) {
    return useQuery({
        queryKey: queryKeys.portfolioHistory(period),
        queryFn: () => getMyPortfolioHistory(period),
        staleTime: HISTORY_STALE_MS,
    })
}
