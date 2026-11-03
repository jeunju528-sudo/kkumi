import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "./queryKeys";
import { getStockProfits } from "../api/tradeHistoryApi";
import type { Period } from "../types/api";

export function useStockProfitsQuery(period: Period) {
    return useQuery({
        queryKey: queryKeys.stockProfits(period),
        queryFn: () => getStockProfits(period),
    })
}