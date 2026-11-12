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

// 평가액·순자산·총손익·오늘 증감 (심장 ②, ADR 044, 054)
class PortfolioCalculatorTest {

    private static final long SEED_SMALL = 5_000_000;
    private static final List<Long> NO_HOUSE = List.of();

    @Test
    @DisplayName("보유 종목 없으면 평가액은 현금과 같음")
    void noHoldings() {
        // given
        long cash = 5_000_000;

        // when
        PortfolioValuation result = PortfolioCalculator.calculate(cash, SEED_SMALL, List.of(), NO_HOUSE);

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
        PortfolioValuation result = PortfolioCalculator.calculate(cash, SEED_SMALL, holdings, NO_HOUSE);

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
        PortfolioValuation result = PortfolioCalculator.calculate(cash, SEED_SMALL, holdings, NO_HOUSE);

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
        PortfolioValuation result = PortfolioCalculator.calculate(cash, SEED_SMALL, holdings, NO_HOUSE);

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
        PortfolioValuation result = PortfolioCalculator.calculate(cash, SEED_SMALL, List.of(), NO_HOUSE);

        // then: isEqualTo라 scale(4자리)까지 비교
        assertThat(result.profitRate()).isEqualTo(new BigDecimal(expectedRate));
    }

    @Test
    @DisplayName("시드머니 5,000만이면 같은 손익도 수익률 1/10")
    void largeSeed() {
        // given: 시드 5,000만, 평가액 5,023만
        long cash = 50_230_000;

        // when
        PortfolioValuation result = PortfolioCalculator.calculate(cash, 50_000_000, List.of(), NO_HOUSE);

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
        assertThatThrownBy(() -> PortfolioCalculator.calculate(0, SEED_SMALL, holdings, NO_HOUSE))
            .isInstanceOf(ArithmeticException.class);
    }

    @Test
    @DisplayName("현금 + 주식 평가액이 long 범위를 넘으면 예외")
    void addOverflow() {
        // given
        List<HoldingResponse> holdings = List.of(holding("KK0001", 1, 1));

        // when, then
        assertThatThrownBy(() -> PortfolioCalculator.calculate(Long.MAX_VALUE, SEED_SMALL, holdings, NO_HOUSE))
            .isInstanceOf(ArithmeticException.class);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    @DisplayName("시드머니가 1원 미만이면 예외")
    void invalidInitialCash(long initialCash) {
        assertThatThrownBy(() -> PortfolioCalculator.calculate(5_000_000, initialCash, List.of(), NO_HOUSE))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("내 집 지분은 집마다 구매가 40% 올림 후 합")
    void houseEquity_ceilPerHouse() {
        // given: 300,000,001 × 40% = 120,000,000.4 → 120,000,001 / 50,000,000 × 40% = 20,000,000
        List<Long> housePurchasePrices = List.of(300_000_001L, 50_000_000L);

        // when
        PortfolioValuation result = PortfolioCalculator.calculate(0, SEED_SMALL, List.of(), housePurchasePrices);

        // then: 지분 합 140,000,001, 평가액엔 안 들어감
        assertThat(result.houseEquity()).isEqualTo(140_000_001);
        assertThat(result.totalAsset()).isZero();
        assertThat(result.netWorth()).isEqualTo(140_000_001);
    }

    @Test
    @DisplayName("집을 사도 순자산·손익은 그대로, 평가액만 결제액만큼 줄어듦")
    void buyHouse_netWorthUnchanged() {
        // given: 시드 500만 그대로 / 1,000만 원 집 사서 현금 400만 차감 → 현금 100만
        PortfolioValuation before = PortfolioCalculator.calculate(5_000_000, SEED_SMALL, List.of(), NO_HOUSE);

        // when
        PortfolioValuation after = PortfolioCalculator.calculate(1_000_000, SEED_SMALL, List.of(), List.of(10_000_000L));

        // then
        assertThat(after.totalAsset()).isEqualTo(1_000_000);
        assertThat(after.houseEquity()).isEqualTo(4_000_000);
        assertThat(after.netWorth()).isEqualTo(before.netWorth()).isEqualTo(5_000_000);
        assertThat(after.profit()).isEqualTo(before.profit()).isZero();
    }

    @Test
    @DisplayName("순자산은 현금 + 주식 + 내 집 지분, 손익은 순자산 − 시드")
    void netWorth_allAssets() {
        // given: 현금 100만 + 52,000 × 10 = 52만 + 1,000만 집 지분 400만
        List<HoldingResponse> holdings = List.of(holding("KK0001", 10, 52_000));

        // when
        PortfolioValuation result = PortfolioCalculator.calculate(1_000_000, SEED_SMALL, holdings, List.of(10_000_000L));

        // then: 5,520,000 − 5,000,000 = 520,000 → 520,000 / 5,000,000 = 0.1040
        assertThat(result.totalAsset()).isEqualTo(1_520_000);
        assertThat(result.netWorth()).isEqualTo(5_520_000);
        assertThat(result.profit()).isEqualTo(520_000);
        assertThat(result.profitRate()).isEqualTo(new BigDecimal("0.1040"));
    }

    @Test
    @DisplayName("오늘 증감: 어제보다 늘면 양수")
    void dailyChange_up() {
        // when: 5,520,000 − 5,000,000
        DailyChange result = PortfolioCalculator.calculateDailyChange(5_520_000, 5_000_000);

        // then
        assertThat(result.amount()).isEqualTo(520_000);
        assertThat(result.rate()).isEqualTo(new BigDecimal("0.1040"));
    }

    @Test
    @DisplayName("오늘 증감: 어제보다 줄면 음수")
    void dailyChange_down() {
        // when: 4,900,000 − 5,000,000
        DailyChange result = PortfolioCalculator.calculateDailyChange(4_900_000, 5_000_000);

        // then
        assertThat(result.amount()).isEqualTo(-100_000);
        assertThat(result.rate()).isEqualTo(new BigDecimal("-0.0200"));
    }

    @Test
    @DisplayName("오늘 증감: 어제 순자산이 0이면 비율 없음")
    void dailyChange_zeroBase() {
        // when
        DailyChange result = PortfolioCalculator.calculateDailyChange(1_000, 0);

        // then
        assertThat(result.amount()).isEqualTo(1_000);
        assertThat(result.rate()).isNull();
    }

    // 평가액 계산엔 수량·현재가만 사용, 평균 매입가는 아무 값
    private static HoldingResponse holding(String code, int quantity, long price) {
        return new HoldingResponse(code, code, quantity, BigDecimal.valueOf(price), price);
    }
}
