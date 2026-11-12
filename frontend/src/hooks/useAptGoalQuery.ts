import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "./queryKeys";
import { getAptGoal } from "../api/aptApi";

export function useAptGoalQuery(){
    return useQuery({
        queryKey: queryKeys.aptGoal(),
        queryFn: getAptGoal,
    })
}