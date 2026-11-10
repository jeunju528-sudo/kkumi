package com.kkumi.apartment.molit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MolitDealItemTest {

    private MolitDealItem item(String dealAmount, String area, String cdealType) {
        return new MolitDealItem("11110", " 익선동 ", "55", "현대뜨레비앙", "2003",
                area, "7", dealAmount, "2026", "10", "29", cdealType);
    }

    @Test
    @DisplayName("거래금액 콤마 제거 후 만원을 원으로")
    void toPriceWon() {
        // given
        MolitDealItem item = item("165,000", "84.858", " ");

        // when
        long price = item.toPriceWon();

        // then
        assertThat(price).isEqualTo(1_650_000_000L);
    }

    @Test
    @DisplayName("거래금액 형식이 이상하면 예외")
    void toPriceWonInvalid() {
        // given
        MolitDealItem item = item("가나다", "84.858", " ");

        // when, then
        assertThatThrownBy(item::toPriceWon)
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("면적 소수 3·4자리는 2자리로 반올림")
    void toArea() {
        // given, when, then
        assertThat(item("1", "84.858", " ").toArea()).isEqualTo(new BigDecimal("84.86"));
        assertThat(item("1", "83.7457", " ").toArea()).isEqualTo(new BigDecimal("83.75"));
        assertThat(item("1", "36.08", " ").toArea()).isEqualTo(new BigDecimal("36.08"));
    }

    @Test
    @DisplayName("면적 반올림 경계")
    void toAreaBoundary() {
        // given, when, then
        assertThat(item("1", "84.855", " ").toArea()).isEqualTo(new BigDecimal("84.86"));
        assertThat(item("1", "84.854", " ").toArea()).isEqualTo(new BigDecimal("84.85"));
    }

    @Test
    @DisplayName("해제 구분 O면 해제 거래")
    void isCanceled() {
        assertThat(item("1", "84.858", "O").isCanceled()).isTrue();
    }

    @Test
    @DisplayName("해제 구분이 공백 한 칸이면 정상 거래")
    void isNotCanceled() {
        assertThat(item("1", "84.858", " ").isCanceled()).isFalse();
    }

    @Test
    @DisplayName("연·월·일을 계약일로 합침")
    void toContractDate() {
        assertThat(item("1", "84.858", " ").toContractDate()).isEqualTo(LocalDate.of(2026, 10, 29));
    }

    @Test
    @DisplayName("앞뒤 공백 제거")
    void clean() {
        assertThat(item("1", "84.858", " ").toDong()).isEqualTo("익선동");
    }
}