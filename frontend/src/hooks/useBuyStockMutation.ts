import { useMutation, useQueryClient } from "@tanstack/react-query";
import { buyStock } from "../api/tradeApi";
import { queryKeys } from "./queryKeys";

// 주식을 사면
// 캐시 낡음 처리 → 지금 화면에서 쓰는 중이면 바로 다시 받아오고, 아니면 다음에 쓸 때 받아옴
// 갖고있는 주식 상태값도 바뀌니까 기존 캐시 무효화해서 다시 받아오기
// useMutation은 호출되면 mutate, isPending, isError, error 등을 반환
// 요청도구와 상태가 든 객체를 반환함
export function useBuyStockMutation() {
    const queryClient = useQueryClient()
    return useMutation({
        mutationFn: buyStock,
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: queryKeys.member() })
            queryClient.invalidateQueries({ queryKey: queryKeys.holdings() })
            queryClient.invalidateQueries({ queryKey: queryKeys.portfolio() })
        },
    })
}