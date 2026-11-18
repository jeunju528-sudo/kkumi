package com.kkumi.notification;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

// 메모리 장부: 기준가 ≥ 현재가인 회원만, 매수·매도 갱신, 받은 목록 날짜 키
class PriceAlertBookTest {

    private static final String CODE = "KK0001";
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 18);

    private final PriceAlertBook book = new PriceAlertBook();

    @Test
    @DisplayName("현재가가 기준가와 같으면 대상")
    void targetAtThreshold() {
        // given: 평단 10,000 → 기준가 9,700
        book.put(1L, CODE, new BigDecimal("10000"));

        // when, then
        assertThat(book.findTargets(CODE, 9_700)).extracting(AlertTarget::memberId).containsExactly(1L);
    }

    @Test
    @DisplayName("현재가가 기준가보다 1원 높으면 대상 아님")
    void notTargetAboveThreshold() {
        // given
        book.put(1L, CODE, new BigDecimal("10000"));

        // when, then
        assertThat(book.findTargets(CODE, 9_701)).isEmpty();
    }

    @Test
    @DisplayName("여러 명 중 기준가 ≥ 현재가인 회원만, 평단 스냅샷 같이")
    void onlyDroppedMembers() {
        // given: 1번 기준가 9,700, 2번 기준가 8,730, 3번 기준가 9,700(같은 기준가)
        book.put(1L, CODE, new BigDecimal("10000"));
        book.put(2L, CODE, new BigDecimal("9000"));
        book.put(3L, CODE, new BigDecimal("10000.5000"));

        // when
        var targets = book.findTargets(CODE, 9_000);

        // then
        assertThat(targets).extracting(AlertTarget::memberId).containsExactlyInAnyOrder(1L, 3L);
        assertThat(targets).filteredOn(t -> t.memberId().equals(3L))
                .extracting(AlertTarget::avgPrice).containsExactly(new BigDecimal("10000.5000"));
    }

    @Test
    @DisplayName("추가 매수로 평단이 바뀌면 옛 기준가는 사라짐")
    void putReplacesThreshold() {
        // given: 평단 10,000(기준가 9,700) → 물타기로 평단 9,000(기준가 8,730)
        book.put(1L, CODE, new BigDecimal("10000"));
        book.put(1L, CODE, new BigDecimal("9000"));

        // when, then
        assertThat(book.findTargets(CODE, 9_000)).isEmpty();
        assertThat(book.findTargets(CODE, 8_730)).extracting(AlertTarget::memberId).containsExactly(1L);
    }

    @Test
    @DisplayName("전량 매도하면 대상 아님, 다른 종목은 그대로")
    void removeOnlyThatStock() {
        // given
        book.put(1L, CODE, new BigDecimal("10000"));
        book.put(1L, "KK0002", new BigDecimal("10000"));

        // when
        book.remove(1L, CODE);

        // then
        assertThat(book.findTargets(CODE, 1)).isEmpty();
        assertThat(book.findTargets("KK0002", 9_700)).hasSize(1);
    }

    @Test
    @DisplayName("장부에 없는 종목·회원 삭제는 무시")
    void removeUnknown() {
        book.remove(9L, "KK9999");
        assertThat(book.findTargets("KK9999", 1)).isEmpty();
    }

    @Test
    @DisplayName("받은 목록은 날짜 키라 다음 날엔 안 받음, 지난 날짜 줄은 정리")
    void alertedByDate() {
        // given
        AlertKey todayKey = new AlertKey(1L, CODE, TODAY);
        book.markAlerted(todayKey);

        // when
        book.forgetAlertsBefore(TODAY.plusDays(1));

        // then
        assertThat(book.isAlerted(todayKey)).isFalse();
        assertThat(book.isAlerted(new AlertKey(1L, CODE, TODAY.plusDays(1)))).isFalse();
    }

    @Test
    @DisplayName("같은 날 정리는 오늘 줄을 안 지움")
    void keepTodayAlerts() {
        // given
        AlertKey todayKey = new AlertKey(1L, CODE, TODAY);
        book.markAlerted(todayKey);

        // when
        book.forgetAlertsBefore(TODAY);

        // then
        assertThat(book.isAlerted(todayKey)).isTrue();
    }
}
