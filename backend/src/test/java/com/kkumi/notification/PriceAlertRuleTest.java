package com.kkumi.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

// 하락 알림 규칙: 기준가 = 평균 매입가 × 97% 내림, 30초 넘은 시세 건너뜀 (ADR 059)
class PriceAlertRuleTest {

    private static final Instant NOW = Instant.parse("2026-11-18T03:00:00Z");

    @ParameterizedTest
    @CsvSource({
            "10000, 9700",      // 딱 떨어짐
            "10001, 9700",      // 9700.97 → 내림
            "10000.5000, 9700", // 9700.485 → 내림
            "103.0927, 99"      // 99.999... → 내림 (100 아님)
    })
    @DisplayName("기준가는 평균 매입가 × 97%를 원 단위 내림")
    void thresholdOf(String avgPrice, long expected) {
        // when
        long threshold = PriceAlertRule.thresholdOf(new BigDecimal(avgPrice));

        // then
        assertThat(threshold).isEqualTo(expected);
    }

    @Test
    @DisplayName("발행 후 딱 30초면 판정")
    void notStaleAt30Seconds() {
        // given
        Instant tickedAt = NOW.minusSeconds(30);

        // when, then
        assertThat(PriceAlertRule.isStale(tickedAt, NOW)).isFalse();
    }

    @Test
    @DisplayName("발행 후 30초 + 1ms면 건너뜀")
    void staleAfter30Seconds() {
        // given
        Instant tickedAt = NOW.minusSeconds(30).minusMillis(1);

        // when, then
        assertThat(PriceAlertRule.isStale(tickedAt, NOW)).isTrue();
    }

    @Test
    @DisplayName("발행 시각이 지금보다 뒤여도(시계 차이) 판정")
    void notStaleWhenTickedAtInFuture() {
        assertThat(PriceAlertRule.isStale(NOW.plusSeconds(1), NOW)).isFalse();
    }
}
