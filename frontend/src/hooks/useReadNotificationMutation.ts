import { queryKeys } from "./queryKeys";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { readNotification } from "../api/notificationApi";

export function useReadNotificationMutation(){
    const queryClient = useQueryClient()
    return useMutation({
        mutationFn: readNotification,
        // 다른 기기에서 이미 지운 알림이면 404가 나감, 실패해도 목록을 새로 받아서 화면을 서버와 맞추려고 onSettled 사용
        // onSettled : mutation이 끝났을 때 성공이던 실패던 상관없이 무조건 한 번 불리는 콜백
        // onSuccess : 성공했을 때만, onError: 실패했을 때만
        onSettled: () => {
            queryClient.invalidateQueries({queryKey: queryKeys.notifications() })
        }
    })
}