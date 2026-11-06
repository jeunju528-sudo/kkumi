import { useEffect, useState } from "react";
import { toMidnightLeft } from "../utils/time";

const TICK_MS = 1000

// "오늘 마감까지" 시계: 0시에 오늘 시작가·상하한·일별 기록이 바뀜 (ADR 050)
// 1초마다 문자열만 갱신, 화면을 떠나면 cleanup에서 타이머 해제
export function useMidnightCountdown(): string {
    const [left, setLeft] = useState(() => toMidnightLeft(new Date()))

    useEffect(() => {
        const timer = setInterval(() => setLeft(toMidnightLeft(new Date())), TICK_MS)
        return () => clearInterval(timer)
    }, [])

    return left
}
