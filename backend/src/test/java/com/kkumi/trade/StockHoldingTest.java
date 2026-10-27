package com.kkumi.trade;

import com.kkumi.global.error.BusinessException;
import com.kkumi.global.error.ErrorCode;
import com.kkumi.member.Member;
import com.kkumi.member.SeedMoney;
import com.kkumi.stock.Stock;
import com.kkumi.stock.StockType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 보유 수량·평균 매입가·실현손익 (심장 ①, ADR 019·037)
class StockHoldingTest {

    @Test
    @DisplayName("첫 매수면 수량, 평균가 = 체결가")
    void buy_first() {
        // given
        Member member = Member.join("abcd", "꿈이", SeedMoney.SMALL);
        Stock stock = Stock.of("KK0001", "세르반반도체", StockType.ROLLER_COASTER, 51_000L);

        // when
        StockHolding stockHolding = StockHolding.create(member, stock, 10, 50000);

        // then
        assertThat(stockHolding.getQuantity()).isEqualTo(10);
        assertThat(stockHolding.getAvgPrice()).isEqualByComparingTo("50000");
    }

    @Test
    @DisplayName("추가 매수하면 평균가는 가중평균")
    void buy_additional_weightedAverage() {
        // given
        Member member = Member.join("abcd", "꿈이", SeedMoney.SMALL);
        Stock stock = Stock.of("KK0001", "세르반반도체", StockType.ROLLER_COASTER, 51_000L);
        StockHolding stockHolding = StockHolding.create(member, stock, 10, 1000);

        // when
        stockHolding.buy(5, 1200);

        // then
        assertThat(stockHolding.getQuantity()).isEqualTo(15);
        assertThat(stockHolding.getAvgPrice()).isEqualByComparingTo("1066.6667");
    }

    @Test
    @DisplayName("평균가가 소수면 4자리 HALF_UP 반올림")
    void buy_avgPrice_roundHalfUp() {
        // given
        Member member = Member.join("abcd", "꿈이", SeedMoney.SMALL);
        Stock stock = Stock.of("KK0001", "세르반반도체", StockType.ROLLER_COASTER, 51_000L);
        StockHolding stockHolding = StockHolding.create(member, stock, 31, 1000);

        // when : (31,000 + 1,001) ÷ 32 = 1000.03125 5번째 자리가 5라서 0313이 되어야함
        stockHolding.buy(1, 1001);

        // then
        assertThat(stockHolding.getAvgPrice()).isEqualByComparingTo("1000.0313");
    }

    @Test
    @DisplayName("매도하면 수량 감소, 평균가 유지")
    void sell_decreaseQuantity_keepAvgPrice() {
        // given
        Member member = Member.join("abcd", "꿈이", SeedMoney.SMALL);
        Stock stock = Stock.of("KK0001", "세르반반도체", StockType.ROLLER_COASTER, 51_000L);
        StockHolding stockHolding = StockHolding.create(member, stock, 10, 1000);

        // when
        long realized = stockHolding.sell(4, 1500);

        // then
        assertThat(stockHolding.getQuantity()).isEqualTo(6);
        assertThat(stockHolding.getAvgPrice()).isEqualByComparingTo("1000");
    }

    @Test
    @DisplayName("체결가가 평균가보다 높으면 실현손익 양수")
    void sell_realizedProfit_positive() {
        // given
        Member member = Member.join("abcd", "꿈이", SeedMoney.SMALL);
        Stock stock = Stock.of("KK0001", "세르반반도체", StockType.ROLLER_COASTER, 51_000L);
        StockHolding stockHolding = StockHolding.create(member, stock, 10, 1000);

        // when
        long realized = stockHolding.sell(4, 1500);

        // then
        assertThat(realized).isEqualTo(2000);
    }

    @Test
    @DisplayName("체결가가 평균가보다 낮으면 실현손익 음수")
    void sell_realizedProfit_negative() {
        // given
        Member member = Member.join("abcd", "꿈이", SeedMoney.SMALL);
        Stock stock = Stock.of("KK0001", "세르반반도체", StockType.ROLLER_COASTER, 51_000L);
        StockHolding stockHolding = StockHolding.create(member, stock, 10, 1000);

        // when
        long realized = stockHolding.sell(4, 800);

        // then
        assertThat(realized).isEqualTo(-800);
    }

    @Test
    @DisplayName("실현손익이 소수면 원 단위 HALF_UP (손실은 0에서 먼 쪽)")
    void sell_realizedProfit_roundHalfUp() {
        // given
        Member member = Member.join("abcd", "꿈이", SeedMoney.SMALL);
        Stock stock = Stock.of("KK0001", "세르반반도체", StockType.ROLLER_COASTER, 51_000L);
        StockHolding stockHolding = StockHolding.create(member, stock, 1, 1000);
        stockHolding.buy(1, 1001);

        // when
        long realized = stockHolding.sell(1, 1000);

        // then
        assertThat(realized).isEqualTo(-1);
    }

    @Test
    @DisplayName("전량 매도하면 수량 0")
    void sell_all() {
        // given
        Member member = Member.join("abcd", "꿈이", SeedMoney.SMALL);
        Stock stock = Stock.of("KK0001", "세르반반도체", StockType.ROLLER_COASTER, 51_000L);
        StockHolding stockHolding = StockHolding.create(member, stock, 10, 1000);

        // when
        long realized = stockHolding.sell(10, 1500);

        // then
        assertThat(stockHolding.getQuantity()).isEqualTo(0);
    }

    @Test
    @DisplayName("보유보다 1주 많이 팔면 INSUFFICIENT_HOLDING")
    void sell_moreThanHolding() {
        // given
        Member member = Member.join("abcd", "꿈이", SeedMoney.SMALL);
        Stock stock = Stock.of("KK0001", "세르반반도체", StockType.ROLLER_COASTER, 51_000L);
        StockHolding stockHolding = StockHolding.create(member, stock, 10, 1000);

        // then
        assertThatThrownBy(()->stockHolding.sell(11, 1500))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INSUFFICIENT_HOLDING);
        assertThat(stockHolding.getQuantity()).isEqualTo(10);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    @DisplayName("매수 수량이 0 이하면 INVALID_QUANTITY")
    void buy_invalidQuantity(int quantity) {
        // given
        Member member = Member.join("abcd", "꿈이", SeedMoney.SMALL);
        Stock stock = Stock.of("KK0001", "세르반반도체", StockType.ROLLER_COASTER, 51_000L);

        // then
        assertThatThrownBy(() -> StockHolding.create(member, stock, quantity, 50000))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_QUANTITY);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    @DisplayName("매도 수량이 0 이하면 INVALID_QUANTITY")
    void sell_invalidQuantity(int quantity) {
        // given
        Member member = Member.join("abcd", "꿈이", SeedMoney.SMALL);
        Stock stock = Stock.of("KK0001", "세르반반도체", StockType.ROLLER_COASTER, 51_000L);
        StockHolding stockHolding = StockHolding.create(member, stock, 10, 1000);

        // then
        assertThatThrownBy(()->stockHolding.sell(quantity, 1500))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_QUANTITY);
    }
}