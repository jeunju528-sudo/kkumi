package com.kkumi.notification;

import java.math.BigDecimal;

// 장부에서 꺼낸 알림 대상 (회원 + 평균 매입가 스냅샷)
public record AlertTarget(Long memberId, BigDecimal avgPrice) {
}
