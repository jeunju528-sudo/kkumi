import { queryKeys } from "./queryKeys";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { deleteNotification } from "../api/notificationApi";

export function useDeleteNotificationMutation(){
    const queryClient = useQueryClient()
    return useMutation({
        mutationFn: deleteNotification,
        // 다른 기기에서 이미 지운 알림이면 404가 나감, 실패해도 목록을 새로 받아서 화면을 서버와 맞추려고 onSettled 사용
        onSettled: () => {
            queryClient.invalidateQueries({queryKey: queryKeys.notifications() })
        }
    })
}