import { useQuery } from "@tanstack/react-query";
import { getTradeHistory } from "../api/tradeHistoryApi";
import type { Period, TradeType } from "../types/api";
import { queryKeys } from "./queryKeys";

export function useTradeHistoryQuery(period: Period, type: TradeType | null) {
    return useQuery({
        queryKey: queryKeys.tradeHistory(period, type),
        queryFn: () => getTradeHistory(period, type, 0),
    })
}