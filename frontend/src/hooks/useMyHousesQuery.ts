import { queryKeys } from "./queryKeys";
import { getMyHouses } from "../api/houseApi";
import { useQuery } from "@tanstack/react-query";

export function useMyHousesQuery() {
    return useQuery({
        queryKey: queryKeys.myHouses(),
        queryFn: getMyHouses
    })
}