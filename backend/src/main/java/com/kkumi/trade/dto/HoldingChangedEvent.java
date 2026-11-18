package com.kkumi.trade.dto;

import java.math.BigDecimal;

// 매수·매도 커밋 후 알림 장부 갱신용 이벤트 (서버 안 이벤트, Kafka 아님)
// avgPrice null = 전량 매도 → 장부에서 삭제
public record HoldingChangedEvent(Long memberId, String stockCode, BigDecimal avgPrice) {

    // 매수·일부 매도 후 평균 매입가
    public static HoldingChangedEvent of(Long memberId, String stockCode, BigDecimal avgPrice) {
        return new HoldingChangedEvent(memberId, stockCode, avgPrice);
    }

    // 전량 매도
    public static HoldingChangedEvent soldOut(Long memberId, String stockCode) {
        return new HoldingChangedEvent(memberId, stockCode, null);
    }

    public boolean isSoldOut() {
        return avgPrice == null;
    }
}
