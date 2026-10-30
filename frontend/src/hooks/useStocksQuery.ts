import { useQuery } from "@tanstack/react-query";
import { getStocks } from "../api/stockApi";
import { queryKeys } from "./queryKeys";

// 주식 다시 가격 가지고오는 실행 주기 5초
const STOCK_REFETCH_MS = 5000

// 사용자 커스텀 훅
// () 괄호를 붙이면 바로 실행되어버리기 때문에 getStock이라는 함수 자체를 넘겨서
// 나중에 실행될 때 함수를 쓰라고 하는 뜻이 됨
// queryFn: getStocks == queryFn: () => getStocks()
// 인자를 넣어야할 때는 그래서 queryFn: () => getStocks(code) 이런식으로 써야 함
// queryKeys.stocks() 는 stocks() 함수가 실행되어야 배열을 보내주기때문에 바로 실행하라고 쓴 것
export function useStocksQuery() {
    return useQuery({
        queryKey: queryKeys.stocks(),
        queryFn: getStocks,
        refetchInterval: STOCK_REFETCH_MS
    })
}