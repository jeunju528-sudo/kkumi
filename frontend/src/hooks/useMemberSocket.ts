import { useState, useEffect } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { connectMemberSocket } from "../ws/memberSocket";
import type { PriceAlertPushResponse } from "../types/api";
import { queryKeys } from "./queryKeys";
// useQueryClient :: React Query(Tanstack Query) 에서 캐시된 데이터에 
// 접근, 관리, 수동으로 수정할 수 있도록 QueryClient 인스턴스를 반환하는 React Hook
// 앱 전체가 같이 쓰는 TanStack Query 캐시(브라우저 메모리 보관함)를 다루는 열쇠

const TOAST_DURATION_MS = 4000
// AppLayout은 로그인이 성공한 후에 그려짐, 로그인 체크 불필요
// 1. AppLayout이 화면에 나타나서 useMemberSocket 실행
// 1-1. React가 useEffect 본문 실행, 본문이 return 한 함수(cleanup)을 react가 보관함
// 2. connectMemberSocket 함수 실행하고 return된 연결 끊기 함수를 변수에 저장해둠
// 2-1. connectMemberSocket 안에서 평가액·알림 구독 2개 등록
// 2-2. 평가액은 setQueryData로 캐시에, 알림은 안 읽은 수를 캐시에 + latestAlert state에 저장
// 3. AppLayout가 화면에서 사라짐(unmount)
// 3-1. react가 보관해 둔 cleanup 실행 -> disconnect() -> client.deactivate()
export function useMemberSocket() {
    const queryClient = useQueryClient()
    const [ latestAlert, setLatestAlert ] = useState<PriceAlertPushResponse | null>(null)

    useEffect(()=>{

        const disconnect = connectMemberSocket({
            onPortfolio: (portfolio) => {
                queryClient.setQueryData(queryKeys.portfolio(), portfolio)
            },
            onAlert: (alert) => {
                queryClient.setQueryData(queryKeys.notificationUnreadCount(), alert.unreadCount)
                setLatestAlert(alert)
            },
        })

        
        // react는 useEffect가 처음 실행될 때 돌려받은 함수를 기억해뒀다가
        // 컴포넌트가 사라지는 순간에 그 함수를 대신 불러줌
        // 이 컴포넌트가 사라지는 순간에 connectMemberSocket 함수가 리턴한 
        // disconnect() 실행
        return () => disconnect()
    }, [queryClient])

    // 4초 안에 새 알림이 오면 latestAlert이 바뀌고 React가 이전 cleanup(clearTimeout)을 먼저 실행한 뒤 새 타이머를 걸어서
    // 새 토스트는 항상 처음부터 4초를 받음
    useEffect(()=>{
        if(latestAlert === null) return
        const timerId = window.setTimeout(()=>setLatestAlert(null), TOAST_DURATION_MS)
        return () => window.clearTimeout(timerId)
    }, [latestAlert])

    return { latestAlert, clearLatestAlert: () => setLatestAlert(null)}
}