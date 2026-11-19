package com.kkumi.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTest {

    private static final LocalDate ALERT_DATE = LocalDate.of(2026, 11, 19);

    // 계산에 회원·종목 안 씀 → null
    private Notification notificationOf(long price, String avgPrice) {
        return Notification.ofPriceDrop(null, null, price, new BigDecimal(avgPrice), ALERT_DATE);
    }

    @Test
    @DisplayName("평단 대비 하락률은 소수 4자리 반올림")
    void calculateDropRate_roundsHalfUp() {
        // given: (14,000 - 15,000) / 15,000 = -0.06666...
        Notification notification = notificationOf(14_000L, "15000");

        // when
        BigDecimal dropRate = notification.calculateDropRate();

        // then: HALF_UP은 0에서 먼 쪽 → -0.0667, isEqualTo라 소수 4자리까지 확인
        assertThat(dropRate).isEqualTo(new BigDecimal("-0.0667"));
    }

    @Test
    @DisplayName("평단과 같으면 0")
    void calculateDropRate_zeroWhenSamePrice() {
        // given
        Notification notification = notificationOf(10_000L, "10000");

        // when
        BigDecimal dropRate = notification.calculateDropRate();

        // then
        assertThat(dropRate).isEqualTo(new BigDecimal("0.0000"));
    }

    @Test
    @DisplayName("평단 스냅샷이 소수여도 현재가 기준으로 계산")
    void calculateDropRate_withDecimalAvgPrice() {
        // given: (9,700 - 10,000.5) / 10,000.5 = -0.030048...
        Notification notification = notificationOf(9_700L, "10000.5000");

        // when
        BigDecimal dropRate = notification.calculateDropRate();

        // then
        assertThat(dropRate).isEqualTo(new BigDecimal("-0.0300"));
    }

    @Test
    @DisplayName("저장 직후는 안 읽음")
    void isRead_falseWhenCreated() {
        // given
        Notification notification = notificationOf(14_000L, "15000");

        // when
        boolean isRead = notification.isRead();

        // then
        assertThat(isRead).isFalse();
    }
}
