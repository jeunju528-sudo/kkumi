import { useQuery } from "@tanstack/react-query";
import { getMyInfo } from "../api/memberApi";
import { queryKeys } from "./queryKeys";

// 로그인 한 회원 정보, 화면 가드와 상단 현금 칩에서 같이 사용
export function useMemberQuery() {
    // queryKey(캐시 칸의 이름)를 보고 값이 비었거나 낡았으면
    // queryFn을 실행해서(isPending = true) 값을 저장하고 data로 돌려주고 화면 다시 그림
    // 다른 컴포넌트가 같은 키로 부르면 요청을 또 보내지 않고 칸에 있는걸 같이 사용
    return useQuery({
        queryKey: queryKeys.member(),
        queryFn: getMyInfo,
        retry: false,
    })
}

/**
 * 훅 : 컴포넌트가 리액트 기능을 쓸 수 있게끔 만들어주는 use로 시작하는 함수
 * useMemberQuery : 커스텀 훅
 * useQuery, useMutation : TanStack Query (외부 라이브러리)가 만든 훅, 이거하려고 tanstack 설치했음, 서버 데이터 가져오기 + 보관하기를 대신 해주는 훅
 * useState, useEffect : 리액트가 기본으로 주는 훅
 * 
 * const queryClient = new QueryClient() 를 하면
 * queryClient 객체 안에 빈 보관함(Map 같이 생김)이 하나 있고 이거를 캐시로 씀
 * 브라우저 탭의 메모리에 저장
 * 탭을 새로고침하거나 닫으면 사라짐
 * QueryClientProvider로 앱 전체를 감싸놨기 때문에 어느 화면의 useQuery든 같은 캐시 보관함을 사용
 * 
 * 앱 시작
 * new QueryClient() -> 보관함 비어있음 {}
 * /home 접속 -> memberGuard 실행 -> useMemberQuery() 실행 -> useQuery 가 보관함에 물어봄
 * ['member', 'me'] 칸 있어?
 * 없음 -> isPending = true로 두고 queryFn 즉 getMyInfo() 실행 -> 화면은 null
 * 응답 도착 useQuery가 보관함의 ['member', 'me'] 칸에다가 {id, nickname, cash} 저장
 * 그 칸을 쓰는 컴포넌트에 값 바뀜 신호 발생 -> MemberGuard 다시 실행되고
 * isPending = false -> <Outlet/>  된 후에 홈 화면이 보이는 것
 */

// getMyInfo function을 
// getMyInfo() 로 부르면 바로 실행해버림
// getMyInfo 로 함수 자체를 넘기면 나중에 필요할 때 쓰라고 알려주는게 됨
// queryFn 함수는 Promise를 돌려줘야 함 (성공하면 그 값이 data, throw하면 isError)