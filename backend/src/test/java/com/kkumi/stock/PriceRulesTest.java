package com.kkumi.stock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PriceRulesTest {

    @ParameterizedTest
    @CsvSource({
            "1, 1", "1999, 1",
            "2000, 5", "4999, 5",
            "5000, 10", "19999, 10",
            "20000, 50", "49999, 50",
            "50000, 100", "199999, 100",
            "200000, 500", "499999, 500",
            "500000, 1000", "689000, 1000"
    })
    @DisplayName("가격대 경계마다 호가 단위가 바뀜")
    void tickSizeByPriceBand(long price, long expected) {
        assertThat(PriceRules.tickSize(price)).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    @DisplayName("가격이 1원 미만이면 예외")
    void tickSizeInvalidPrice(long price) {
        assertThatThrownBy(() -> PriceRules.tickSize(price))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "248150.0, 248000",
            "248300.0, 248500",
            "51049.9, 51000",
            "51050.0, 51100",
            "1999.7, 2000"
    })
    @DisplayName("반올림 전 가격을 가장 가까운 호가로 반올림")
    void roundToTick(double rawPrice, long expected) {
        assertThat(PriceRules.roundToTick(rawPrice)).isEqualTo(expected);
    }

    @Test
    @DisplayName("상한가는 시작가 +30%를 호가 단위로 내림")
    void upperLimit() {
        // given
        long dayOpenPrice = 248_000L;

        // when
        long upper = PriceRules.upperLimit(dayOpenPrice);

        // then
        // 248000 * 1.3 = 322400인데 호가 단위 500원이니까 322000
        assertThat(upper).isEqualTo(322_000L);
    }

    @Test
    @DisplayName("하한가는 시작가 -30%를 호가 단위로 올림")
    void lowerLimit() {
        // given
        long dayOpenPrice = 8_730L;

        // when
        long lower = PriceRules.lowerLimit(dayOpenPrice);

        // then
        // 8730 * 0.7 = 6111 인데 호가 10원 단위라서 6120원
        assertThat(lower).isEqualTo(6_120L);
    }
}