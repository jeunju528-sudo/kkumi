import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "./queryKeys";
import type { Period } from "../types/api";
import { getTradeHistorySummary } from "../api/tradeHistoryApi";

export function useTradeHistorySummaryQuery(period: Period){
    return useQuery({
        queryKey: queryKeys.tradeHistorySummary(period),
        queryFn: () => getTradeHistorySummary(period),
    })
}