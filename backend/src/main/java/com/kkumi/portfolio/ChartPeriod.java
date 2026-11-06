package com.kkumi.portfolio;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;

// 홈 손익 차트 기간 (지난 날 수, 오늘 점은 WebSocket 현재값)
@RequiredArgsConstructor
public enum ChartPeriod {
    WEEK(6),
    MONTH(29);

    private final int pastDays;

    // 조회 시작일 (포함)
    public LocalDate toFrom(LocalDate today) {
        return today.minusDays(pastDays);
    }
}
