import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "./queryKeys";
import { getMyPortfolio } from "../api/portfolioApi";

// 이 훅을 쓰는 화면이 떠 있는 동안만 setInterval이 수행됨
// 다른 화면으로 가면 화면이 사라지면서(unmount) 타이머도 꺼짐
// (이전) 폴링: refetchInterval이 화면이 떠 있는 동안 setInterval로 5초마다 요청
// (현재) 첫 값만 GET, 이후 값은 WebSocket이 setQueryData로 넣음 (usePortfolioSocket)
// refetchInterval 은 내부 queryObserver.js에 보면 setInterval로 돌게 설정되어있음
export function usePortfolioQuery(){
    return useQuery({
        queryKey: queryKeys.portfolio(),
        queryFn: getMyPortfolio,
    })
}