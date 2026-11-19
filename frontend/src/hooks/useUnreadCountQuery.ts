import { queryKeys } from "./queryKeys";
import { useQuery } from "@tanstack/react-query";
import { getUnreadCount } from "../api/notificationApi";

export function useUnreadCountQuery() {
    return useQuery({
        queryKey: queryKeys.notificationUnreadCount(),
        queryFn: async () => (await getUnreadCount()).count, // 캐시에는 숫자만 넣음
    })
}