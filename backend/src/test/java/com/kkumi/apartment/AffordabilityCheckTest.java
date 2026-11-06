package com.kkumi.apartment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

// 카드 상태: 추천은 평가액 ≥ 집값 × 40%, 결제는 현금 기준
class AffordabilityCheckTest {

    private static final long OWN = 72_000_000; // 1억 8,000만 집의 40%

    @ParameterizedTest
    @CsvSource({
        "72000000, 80000000, OK, 0",                // 현금 = 내 돈 (경계)
        "80000000, 80000000, OK, 0",                // 현금 넉넉
        "71999999, 80000000, NEED_SELL, 1",         // 현금 1원 부족
        "10000000, 72000000, NEED_SELL, 62000000",  // 평가액 = 내 돈 (경계)
        "10000000, 71999999, SHORT, 1",             // 평가액 1원 부족
        "0, 0, SHORT, 72000000"                     // 돈 없음
    })
    @DisplayName("현금·평가액 경계에 따라 OK / NEED_SELL / SHORT와 부족액")
    void check(long cash, long totalAsset, Affordability expected, long expectedShortage) {
        // when
        AffordabilityCheck result = AffordabilityCheck.of(OWN, cash, totalAsset);

        // then
        assertThat(result.affordability()).isEqualTo(expected);
        assertThat(result.shortage()).isEqualTo(expectedShortage);
    }
}
