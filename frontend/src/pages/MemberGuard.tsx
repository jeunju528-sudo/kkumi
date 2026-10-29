import { Navigate, Outlet } from "react-router-dom";
import { useMemberQuery } from "../hooks/useMemberQuery";

export function MemberGuard() {
    const { isPending, isError } = useMemberQuery()
    if(isPending) {
        return null;
    }
    if(isError) {
        // replace하면 이동 기록을 덮어써서 뒤로 가기를 눌러도 막힌 화면으로 돌아가서 다시 튕기는 일이 없음
        return <Navigate to="/" replace />
    }
    
    // outlet : 자식화면이 들어갈 자리
    return <Outlet />
}