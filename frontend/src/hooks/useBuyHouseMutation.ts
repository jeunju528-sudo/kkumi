import { useMutation, useQueryClient } from "@tanstack/react-query";
import { queryKeys } from "./queryKeys";
import { buyHouse } from "../api/houseApi";

export function useBuyHouseMutation() {
    const queryClient = useQueryClient()
    return useMutation({
        mutationFn: buyHouse,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: queryKeys.member() })
            queryClient.invalidateQueries({ queryKey: queryKeys.portfolio() })
            queryClient.invalidateQueries({ queryKey: queryKeys.apartments() })
            queryClient.invalidateQueries({ queryKey: queryKeys.myHouses() })
        }
    })
}