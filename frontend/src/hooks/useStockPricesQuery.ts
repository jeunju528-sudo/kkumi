import { useQuery } from "@tanstack/react-query";
import { getStockPrices } from "../api/stockApi";
import { queryKeys } from "./queryKeys";

// 서버가 1분마다 기록 → 1분마다 다시 받아서 분봉을 한 칸씩 밀기
const STOCK_PRICES_REFETCH_MS = 60_000

export function useStockPricesQuery(code: string) {
    return useQuery({
        queryKey: queryKeys.stockPrices(code),
        queryFn: () => getStockPrices(code),
        refetchInterval: STOCK_PRICES_REFETCH_MS,
    })
}
