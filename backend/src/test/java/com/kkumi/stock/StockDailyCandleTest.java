package com.kkumi.stock;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// 일봉 시가·고가·저가·종가 계산 (ADR 056)
class StockDailyCandleTest {

    private static final LocalDate DAY = LocalDate.of(2026, 11, 12);

    @Test
    @DisplayName("첫 기록이 시작가보다 낮으면 고가 = 시가, 저가 = 지금 가격")
    void openBelowDayOpen() {
        // when
        StockDailyCandle candle = StockDailyCandle.open("KK0001", DAY, 10_000L, 9_700L);

        // then
        assertThat(candle.getOpenPrice()).isEqualTo(10_000L);
        assertThat(candle.getHighPrice()).isEqualTo(10_000L);
        assertThat(candle.getLowPrice()).isEqualTo(9_700L);
        assertThat(candle.getClosePrice()).isEqualTo(9_700L);
    }

    @Test
    @DisplayName("갱신은 고가·저가를 넓히기만 하고 종가는 마지막 가격")
    void updateWidensRange() {
        // given
        StockDailyCandle candle = StockDailyCandle.open("KK0001", DAY, 10_000L, 10_000L);

        // when
        candle.update(10_400L);
        candle.update(9_900L);
        candle.update(10_200L);

        // then
        assertThat(candle.getHighPrice()).isEqualTo(10_400L);
        assertThat(candle.getLowPrice()).isEqualTo(9_900L);
        assertThat(candle.getClosePrice()).isEqualTo(10_200L);
        assertThat(candle.getOpenPrice()).isEqualTo(10_000L);
    }
}
