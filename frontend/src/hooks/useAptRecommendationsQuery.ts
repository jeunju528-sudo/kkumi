import { queryKeys } from "./queryKeys";
import { useQuery } from "@tanstack/react-query";
import { getAptRecommendations } from "../api/aptApi";

export function useAptRecommendationsQuery(q: string, isAffordableOnly: boolean) {
    return useQuery({
        queryKey: queryKeys.aptRecommendations(q, isAffordableOnly),
        queryFn: () => getAptRecommendations(q, isAffordableOnly, 0)
    })
}