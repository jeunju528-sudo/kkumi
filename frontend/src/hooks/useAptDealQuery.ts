import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "./queryKeys";
import { getAptDeal } from "../api/aptApi";

export function useAptDealQuery(dealId: number){
    return useQuery({
        queryKey: queryKeys.aptDeal(dealId),
        queryFn: () => getAptDeal(dealId),
        retry: false,
    })
}