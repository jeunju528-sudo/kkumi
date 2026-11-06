package com.kkumi.apartment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

// 집값 40% 내 돈 + 60% 가짜 대출 (ADR 047)
class HousePriceRuleTest {

    @Test
    @DisplayName("내 돈은 집값의 40%, 대출은 나머지 60%")
    void ownAndLoan() {
        // given: 1억 8,000만 (만원 단위 거래라 나눠떨어짐)
        long price = 180_000_000;

        // when
        long own = HousePriceRule.ownAmount(price);
        long loan = HousePriceRule.loanAmount(price);

        // then
        assertThat(own).isEqualTo(72_000_000);
        assertThat(loan).isEqualTo(108_000_000);
        assertThat(own + loan).isEqualTo(price);
    }

    @ParameterizedTest
    @CsvSource({
        "1, 1",          // 0.4 → 올림 1
        "10001, 4001",   // 4,000.4 → 올림 4,001
        "10000, 4000"    // 나눠떨어지면 그대로
    })
    @DisplayName("내 돈은 원 미만 올림")
    void ownAmountRoundsUp(long price, long expectedOwn) {
        assertThat(HousePriceRule.ownAmount(price)).isEqualTo(expectedOwn);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    @DisplayName("집값이 1원 미만이면 예외")
    void invalidPrice(long price) {
        assertThatThrownBy(() -> HousePriceRule.ownAmount(price))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({
        "5230000, 13075000",  // 523만 × 100 ÷ 40 = 1,307만 5,000
        "4000, 10000",        // 경계: 4,000 × 100 ÷ 40 = 10,000 → 내 돈 4,000
        "4001, 10002"         // 4,001 × 100 ÷ 40 = 10,002.5 → 내림
    })
    @DisplayName("평가액으로 살 수 있는 최대 집값")
    void maxAffordablePrice(long totalAsset, long expectedMaxPrice) {
        assertThat(HousePriceRule.maxAffordablePrice(totalAsset)).isEqualTo(expectedMaxPrice);
    }

    @ParameterizedTest
    @ValueSource(longs = {4000, 4001, 5230000})
    @DisplayName("최대 집값의 내 돈은 평가액 이하, 1원 비싸면 평가액 초과 (조회 조건과 판정이 같은 경계)")
    void maxPriceMatchesOwnAmount(long totalAsset) {
        // given
        long maxPrice = HousePriceRule.maxAffordablePrice(totalAsset);

        // when, then
        assertThat(HousePriceRule.ownAmount(maxPrice)).isLessThanOrEqualTo(totalAsset);
        assertThat(HousePriceRule.ownAmount(maxPrice + 1)).isGreaterThan(totalAsset);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    @DisplayName("평가액이 0원 이하면 살 수 있는 최대 집값 0")
    void maxAffordablePriceNoAsset(long totalAsset) {
        assertThat(HousePriceRule.maxAffordablePrice(totalAsset)).isZero();
    }

    @Test
    @DisplayName("집값 × 40이 long 범위를 넘으면 예외")
    void ownAmountOverflow() {
        assertThatThrownBy(() -> HousePriceRule.ownAmount(Long.MAX_VALUE / 10))
            .isInstanceOf(ArithmeticException.class);
    }
}
