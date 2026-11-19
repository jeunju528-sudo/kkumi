import { queryKeys } from "./queryKeys";
import { useQuery } from "@tanstack/react-query";
import { getNotifications } from "../api/notificationApi";

export function useNotificationsQuery(isEnabled: boolean) {
    return useQuery({
        queryKey: queryKeys.notificationList(),
        queryFn: getNotifications,
        enabled: isEnabled
    })
}