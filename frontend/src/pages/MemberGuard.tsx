import { Navigate, Outlet } from "react-router-dom";
import { useMemberQuery } from "../hooks/useMemberQuery";
import { usePortfolioSocket } from "../hooks/usePortfolioSocket";

export function MemberGuard() {
    const { isPending, isError, isSuccess } = useMemberQuery()
    // 회원 조회 성공하면 평가액 WebSocket 연결·구독 (로딩·실패 중엔 연결 안 함)
    usePortfolioSocket(isSuccess)

    if (isPending) {
        return null;
    }
    if (isError) {
        // replace하면 이동 기록을 덮어써서 뒤로 가기를 눌러도 막힌 화면으로 돌아가서 다시 튕기는 일이 없음
        return <Navigate to="/" replace />
    }

    // outlet : 자식화면이 들어갈 자리
    return <Outlet />
}