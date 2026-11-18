package com.kkumi.trade.dto;

import java.math.BigDecimal;

// 알림 장부 초기 로드용 조회 결과 (엔티티 대신 필요한 값 3개만)
public record HoldingAvgPrice(Long memberId, String stockCode, BigDecimal avgPrice) {
}
