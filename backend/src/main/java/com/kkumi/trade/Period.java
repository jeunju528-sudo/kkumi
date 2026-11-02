package com.kkumi.trade;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

// 내 거래 화면 기간 필터 (일수는 프론트 기간 버튼과 같은 값)
@RequiredArgsConstructor
public enum Period {
    WEEK(7),
    MONTH(31),
    QUARTER(92),
    ALL(0);

    private final int days;

    // 조회 시작 시각, ALL은 null (기간 제한 없음)
    public LocalDateTime toFrom(LocalDateTime now){
        if(this == ALL) {
            return null;
        }
        return now.minusDays(days);
    }
}
