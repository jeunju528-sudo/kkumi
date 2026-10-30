import { useQuery } from "@tanstack/react-query";
import { getHoldings } from "../api/holdingApi";
import { queryKeys } from "./queryKeys";

// refetchInterval 없는 이유 : 매매 때만 바뀌어서 폴링 없음 (매매 성공 후 invalidate)
export function useHoldingsQuery() {
    return useQuery({
        queryKey: queryKeys.holdings(),
        queryFn: getHoldings,
    })
}