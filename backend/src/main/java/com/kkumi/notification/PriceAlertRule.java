package com.kkumi.notification;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;

/**
 * 하락 알림 규칙 (ADR 059)
 * - 기준가 = 평균 매입가 × 97%, 원 단위 내림
 * - 현재가 ≤ 기준가면 알림. 현재가가 정수라 내림해도 "현재가 ≤ 평균 매입가 × 97%"와 결과 같음
 * - 발행 후 30초 넘은 시세는 판정 안 함
 */
public final class PriceAlertRule {

    public static final int ALERT_RATE_PERCENT = 97;
    public static final Duration STALE_AFTER = Duration.ofSeconds(30);

    private PriceAlertRule() {
    }

    // 기준가 (원 단위 내림)
    public static long thresholdOf(BigDecimal avgPrice) {
        return avgPrice.multiply(BigDecimal.valueOf(ALERT_RATE_PERCENT))
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR)
                .longValueExact();
    }

    // 30초 초과면 지난 시세. 딱 30초는 판정
    public static boolean isStale(Instant tickedAt, Instant now) {
        return Duration.between(tickedAt, now).compareTo(STALE_AFTER) > 0;
    }
}
