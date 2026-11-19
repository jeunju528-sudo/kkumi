import { queryKeys } from "./queryKeys";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { readAllNotifications } from "../api/notificationApi";

export function useReadAllNotificationsMutation(){
    const queryClient = useQueryClient()
    return useMutation({
        mutationFn: readAllNotifications,
        // 실패해도 종 배지·목록을 서버 기준으로 다시 맞춤
        onSettled: () => {
            queryClient.invalidateQueries({queryKey: queryKeys.notifications() })
        }
    })
}