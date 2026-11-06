package com.kkumi.apartment;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

// 살 수 있는 거래: 해제 안 됨 + 최근 6개월 (ADR 014·015)
class AptDealTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 6);
    private static final AptComplex COMPLEX = AptComplex.of("51190", "황지동", "1", "꿈이아파트", 2001, "강원 태백시");

    @Test
    @DisplayName("6개월 전 날짜 계약은 살 수 있음 (경계 포함)")
    void availableOnBoundary() {
        // given: 2026-05-06
        AptDeal deal = AptDeal.of(COMPLEX, new BigDecimal("39.00"), 5, 18_000_000, TODAY.minusMonths(6));

        // when, then
        assertThat(deal.isAvailable(TODAY)).isTrue();
    }

    @Test
    @DisplayName("6개월 하루 전 계약은 살 수 없음")
    void unavailableBeforeBoundary() {
        // given: 2026-05-05
        AptDeal deal = AptDeal.of(COMPLEX, new BigDecimal("39.00"), 5, 18_000_000, TODAY.minusMonths(6).minusDays(1));

        // when, then
        assertThat(deal.isAvailable(TODAY)).isFalse();
    }

    @Test
    @DisplayName("해제된 거래는 기간 안이어도 살 수 없음")
    void canceled() {
        // given
        AptDeal deal = AptDeal.of(COMPLEX, new BigDecimal("39.00"), 5, 18_000_000, TODAY);
        ReflectionTestUtils.setField(deal, "canceled", true);

        // when, then
        assertThat(deal.isAvailable(TODAY)).isFalse();
    }
}
