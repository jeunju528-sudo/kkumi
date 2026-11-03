package com.kkumi.portfolio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kkumi.trade.dto.HoldingResponse;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

// 평가액·총손익·총수익률 (심장 ②, ADR 044)
class PortfolioCalculatorTest {

    private static final long SEED_SMALL = 5_000_000;

    @Test
    @DisplayName("보유 종목 없으면 평가액은 현금과 같음")
    void noHoldings() {
        // given
        long cash = 5_000_000;

        // when
        PortfolioValuation result = PortfolioCalculator.calculate(cash, SEED_SMALL, List.of());

        // then
        assertThat(result.stockValue()).isZero();
        assertThat(result.totalAsset()).isEqualTo(5_000_000);
        assertThat(result.profit()).isZero();
        assertThat(result.profitRate()).isEqualTo(new BigDecimal("0.0000"));
    }

    @Test
    @DisplayName("평가액은 현금과 종목별 수량×현재가 합")
    void sumOfHoldings() {
        // given: 52,000 × 10 = 520,000 / 101,500 × 3 = 304,500
        long cash = 1_000_000;
        List<HoldingResponse> holdings = List.of(
            holding("KK0001", 10, 52_000),
            holding("KK0002", 3, 101_500));

        // when
        PortfolioValuation result = PortfolioCalculator.calculate(cash, SEED_SMALL, holdings);

        // then
        assertThat(result.stockValue()).isEqualTo(824_500);
        assertThat(result.totalAsset()).isEqualTo(1_824_500);
    }

    @Test
    @DisplayName("평가액이 시드보다 크면 손익·수익률 양수")
    void profit() {
        // given: 현금 421만 + 51,000 × 20 = 523만
        long cash = 4_210_000;
        List<HoldingResponse> holdings = List.of(holding("KK0001", 20, 51_000));

        // when
        PortfolioValuation result = PortfolioCalculator.calculate(cash, SEED_SMALL, holdings);

        // then: 23만 ÷ 500만 = 4.6%
        assertThat(result.totalAsset()).isEqualTo(5_230_000);
        assertThat(result.profit()).isEqualTo(230_000);
        assertThat(result.profitRate()).isEqualTo(new BigDecimal("0.0460"));
    }

    @Test
    @DisplayName("평가액이 시드보다 작으면 손익·수익률 음수")
    void loss() {
        // given: 현금 300만 + 60,000 × 30 = 480만
        long cash = 3_000_000;
        List<HoldingResponse> holdings = List.of(holding("KK0001", 30, 60_000));

        // when
        PortfolioValuation result = PortfolioCalculator.calculate(cash, SEED_SMALL, holdings);

        // then: −20만 ÷ 500만 = −4%
        assertThat(result.totalAsset()).isEqualTo(4_800_000);
        assertThat(result.profit()).isEqualTo(-200_000);
        assertThat(result.profitRate()).isEqualTo(new BigDecimal("-0.0400"));
    }

    @ParameterizedTest
    @CsvSource({
        "5000250, 0.0001",  // +250 ÷ 500만 = 0.00005 → 올림
        "5000249, 0.0000",  // +249 ÷ 500만 = 0.0000498 → 버림
        "4999750, -0.0001", // −250 → HALF_UP은 0에서 먼 쪽
        "4999751, 0.0000"   // −249 → 0 (scale 4 유지)
    })
    @DisplayName("수익률은 소수 4자리 반올림")
    void rateRounding(long cash, String expectedRate) {
        // when
        PortfolioValuation result = PortfolioCalculator.calculate(cash, SEED_SMALL, List.of());

        // then: isEqualTo라 scale(4자리)까지 비교
        assertThat(result.profitRate()).isEqualTo(new BigDecimal(expectedRate));
    }

    @Test
    @DisplayName("시드머니 5,000만이면 같은 손익도 수익률 1/10")
    void largeSeed() {
        // given: 시드 5,000만, 평가액 5,023만
        long cash = 50_230_000;

        // when
        PortfolioValuation result = PortfolioCalculator.calculate(cash, 50_000_000, List.of());

        // then
        assertThat(result.profit()).isEqualTo(230_000);
        assertThat(result.profitRate()).isEqualTo(new BigDecimal("0.0046"));
    }

    @Test
    @DisplayName("수량×현재가가 long 범위를 넘으면 예외")
    void multiplyOverflow() {
        // given
        List<HoldingResponse> holdings = List.of(holding("KK0001", 3, Long.MAX_VALUE / 2));

        // when, then
        assertThatThrownBy(() -> PortfolioCalculator.calculate(0, SEED_SMALL, holdings))
            .isInstanceOf(ArithmeticException.class);
    }

    @Test
    @DisplayName("현금 + 주식 평가액이 long 범위를 넘으면 예외")
    void addOverflow() {
        // given
        List<HoldingResponse> holdings = List.of(holding("KK0001", 1, 1));

        // when, then
        assertThatThrownBy(() -> PortfolioCalculator.calculate(Long.MAX_VALUE, SEED_SMALL, holdings))
            .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    @DisplayName("시드머니가 1원 미만이면 예외")
    void invalidInitialCash(long initialCash) {
        assertThatThrownBy(() -> PortfolioCalculator.calculate(5_000_000, initialCash, List.of()))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // 평가액 계산엔 수량·현재가만 사용, 평균 매입가는 아무 값
    private static HoldingResponse holding(String code, int quantity, long price) {
        return new HoldingResponse(code, code, quantity, BigDecimal.valueOf(price), price);
    }
}
