package com.kkumi.house;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

// 변신 4단계: 0채 걸뱅이 / 1채 서민 / 2~4채 졸부 / 5채 이상 갑부
class TierTest {

    @Test
    @DisplayName("집이 없으면 걸뱅이")
    void zeroHouse() {
        assertThat(Tier.of(0)).isEqualTo(Tier.GEOLBAENGI);
    }

    @Test
    @DisplayName("1채면 서민")
    void oneHouse() {
        assertThat(Tier.of(1)).isEqualTo(Tier.SEOMIN);
    }

    @ParameterizedTest
    @ValueSource(longs = {2, 3, 4})
    @DisplayName("2~4채면 졸부")
    void jokbu(long houseCount) {
        assertThat(Tier.of(houseCount)).isEqualTo(Tier.JOKBU);
    }

    @ParameterizedTest
    @ValueSource(longs = {5, 6, 100})
    @DisplayName("5채 이상이면 갑부")
    void gapbu(long houseCount) {
        assertThat(Tier.of(houseCount)).isEqualTo(Tier.GAPBU);
    }

    @Test
    @DisplayName("집 수가 음수면 예외")
    void negativeCount() {
        assertThatThrownBy(() -> Tier.of(-1))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
