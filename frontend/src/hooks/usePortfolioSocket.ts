import { useEffect } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { subscribePortfolio } from "../ws/portfolioSocket";
import { queryKeys } from "./queryKeys";
// useQueryClient :: React Query(Tanstack Query) 에서 캐시된 데이터에 
// 접근, 관리, 수동으로 수정할 수 있도록 QueryClient 인스턴스를 반환하는 React Hook
// 앱 전체가 같이 쓰는 TanStack Query 캐시(브라우저 메모리 보관함)를 다루는 열쇠

// 1. MemberGuard가 화면에 나타나서 usePortfolioSocket 실행
// 1-1. React가 useEffect 본문 실행, 본문이 return 한 함수(cleanup)을 react가 보관함
// 2. subscribePortfolio 함수 실행하고 return client.deactivate() 버튼을 return 받아서 변수에 저장해둠
// 2-1. subscribePortfolio 이 안에서 구독 등록하고
// 2-2. 구독 콜백으로 온 평가액을 setQueryData로 캐시에 저장
// 3. MemberGuard가 화면에서 사라짐(unmount)
// 3-1. react가 보관해 둔 cleanup 실행 -> disconnect() -> client.deactivate()
export function usePortfolioSocket(isEnabled: boolean){
    const queryClient = useQueryClient()
    
    // Hook 은 if문 안에서 부를 수 없어서 
    // 훅 안에서 if 문 사용
    useEffect(()=>{
        // 아직 로그인 전이면
        if(!isEnabled) {
            return
        }
        const unsubscribe = subscribePortfolio((portfolio) => {
            // 요청 없이 캐시 칸에 값을 직접 넣음
            // 우편이 오면 캐시 ['portfolio','me'] 에 값을 바로 넣음
            // 이 칸을 보는 화면들은 자동으로 다시 그려짐
            queryClient.setQueryData(queryKeys.portfolio(), portfolio)
        })

        // react는 useEffect가 처음 실행될 때 돌려받은 함수를 기억해뒀다가
        // 컴포넌트가 사라지는 순간에 그 함수를 대신 불러줌
        // 이 컴포넌트가 사라지는 순간에 subscribePortfolio 함수가 리턴한 
        // () => { client.deactivate() } 를 실행해줌
        return () => unsubscribe()
    }, [isEnabled, queryClient])
}