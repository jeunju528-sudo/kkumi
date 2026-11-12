import { useQuery } from "@tanstack/react-query";
import { getStockCandles } from "../api/stockApi";
import { queryKeys } from "./queryKeys";

// 오늘 일봉도 1분마다 고가·저가·종가가 바뀜 → 분봉과 같은 주기
const STOCK_CANDLES_REFETCH_MS = 60_000

export function useStockCandlesQuery(code: string) {
    return useQuery({
        queryKey: queryKeys.stockCandles(code),
        queryFn: () => getStockCandles(code),
        refetchInterval: STOCK_CANDLES_REFETCH_MS,
    })
}
