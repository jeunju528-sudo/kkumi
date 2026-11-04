import { useMutation, useQueryClient } from "@tanstack/react-query";
import { sellStock } from "../api/tradeApi";
import { queryKeys } from "./queryKeys";

export function useSellStockMutation() {
    const queryClient = useQueryClient()
    return useMutation({
        mutationFn: sellStock,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: queryKeys.member() })
            queryClient.invalidateQueries({ queryKey: queryKeys.holdings() })
            queryClient.invalidateQueries({ queryKey: queryKeys.portfolio() })
        }
    })
}