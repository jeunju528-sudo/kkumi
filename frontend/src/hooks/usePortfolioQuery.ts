import { useQuery } from "@tanstack/react-query";
import { queryKeys } from "./queryKeys";
import { getMyPortfolio } from "../api/portfolioApi";

// 서버 시세 갱신 주기랑 맞춤
const PORTFOLIO_REFETCH_MS = 5000

// 이 훅을 쓰는 화면이 떠 있는 동안만 setInterval이 수행됨
// 다른 화면으로 가면 화면이 사라지면서(unmount) 타이머도 꺼짐
// refetchInterval 은 내부 queryObserver.js에 보면 setInterval로 돌게 설정되어있음
export function usePortfolioQuery(){
    return useQuery({
        queryKey: queryKeys.portfolio(),
        queryFn: getMyPortfolio,
        refetchInterval: PORTFOLIO_REFETCH_MS // 여기서 타이머를 켬 5초마다 돌게
    })
}