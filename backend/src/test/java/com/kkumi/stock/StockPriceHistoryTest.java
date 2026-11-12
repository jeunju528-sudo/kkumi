package com.kkumi.stock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.kkumi.global.error.BusinessException;
import com.kkumi.global.error.ErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

// 1분 시세 기록·일봉 저장·조회·정리 (ADR 056)
// 시각은 과거 고정값 (테스트 중 1분 저장 스케줄이 넣는 지금 시각 기록과 섞이지 않게)
@SpringBootTest
@Transactional
class StockPriceHistoryTest {

    private static final LocalDateTime T = LocalDateTime.of(2026, 1, 1, 10, 0);

    @Autowired
    private StockPriceSnapshotService snapshotService;

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StockPriceHistoryRepository historyRepository;

    @Autowired
    private StockDailyCandleRepository candleRepository;

    private static final LocalDate DAY1 = LocalDate.of(2026, 1, 1);

    @BeforeEach
    void setUp() {
        stockRepository.save(Stock.of("TS0001", "테스트전자", StockType.STABLE, 10_000L));
        stockRepository.save(Stock.of("TS0002", "테스트바이오", StockType.STABLE, 20_000L));
    }

    @Test
    @DisplayName("시세 저장 때 스냅샷 있는 종목만 기록 1행씩 추가")
    void savePricesAddsHistory() {
        // given
        Map<String, PriceSnapshot> snapshots = Map.of(
                "TS0001", new PriceSnapshot(10_500L, 10_000L, LocalDate.of(2026, 1, 1)));

        // when
        snapshotService.savePrices(snapshots, T);

        // then
        assertThat(historyRepository.findRecent("TS0001", T.minusMinutes(1)))
                .extracting(StockPriceHistory::getPrice, StockPriceHistory::getRecordedAt)
                .containsExactly(tuple(10_500L, T));
        assertThat(historyRepository.findRecent("TS0002", T.minusMinutes(1))).isEmpty();
    }

    @Test
    @DisplayName("since 이후 기록만 오래된 순, 다른 종목 제외")
    void findRecentOrderedSince() {
        // given
        historyRepository.saveAll(List.of(
                StockPriceHistory.of("TS0001", 10_200L, T.plusMinutes(2)),
                StockPriceHistory.of("TS0001", 9_900L, T.minusMinutes(1)),
                StockPriceHistory.of("TS0001", 10_100L, T.plusMinutes(1)),
                StockPriceHistory.of("TS0001", 10_000L, T),
                StockPriceHistory.of("TS0002", 20_000L, T.plusMinutes(1))));

        // when
        List<StockPriceHistory> recent = historyRepository.findRecent("TS0001", T);

        // then
        assertThat(recent).extracting(StockPriceHistory::getPrice)
                .containsExactly(10_000L, 10_100L, 10_200L);
    }

    @Test
    @DisplayName("before 이전 기록만 삭제, 경계 시각 기록은 남김")
    void deleteRecordedBefore() {
        // given
        historyRepository.saveAll(List.of(
                StockPriceHistory.of("TS0001", 10_000L, T.minusSeconds(1)),
                StockPriceHistory.of("TS0001", 10_100L, T),
                StockPriceHistory.of("TS0001", 10_200L, T.plusMinutes(1))));

        // when
        int deleted = snapshotService.deleteHistoryBefore(T);

        // then
        assertThat(deleted).isEqualTo(1);
        assertThat(historyRepository.findRecent("TS0001", T.minusDays(1)))
                .extracting(StockPriceHistory::getPrice)
                .containsExactly(10_100L, 10_200L);
    }

    @Test
    @DisplayName("같은 날 1분 저장이 쌓이면 일봉 1행에 고가·저가·종가 갱신, 시가는 오늘 시작가")
    void savePricesUpdatesDailyCandle() {
        // given: 시작가 10,000 → 10,300 → 9,800 → 10,100
        long dayOpen = 10_000L;

        // when
        snapshotService.savePrices(Map.of("TS0001", new PriceSnapshot(10_300L, dayOpen, DAY1)), T);
        snapshotService.savePrices(Map.of("TS0001", new PriceSnapshot(9_800L, dayOpen, DAY1)), T.plusMinutes(1));
        snapshotService.savePrices(Map.of("TS0001", new PriceSnapshot(10_100L, dayOpen, DAY1)), T.plusMinutes(2));

        // then
        assertThat(candleRepository.findRecent("TS0001", DAY1))
                .extracting(StockDailyCandle::getOpenPrice, StockDailyCandle::getHighPrice,
                        StockDailyCandle::getLowPrice, StockDailyCandle::getClosePrice)
                .containsExactly(tuple(10_000L, 10_300L, 9_800L, 10_100L));
    }

    @Test
    @DisplayName("날짜가 바뀌면 새 일봉, 오래된 순 조회")
    void newCandleNextDay() {
        // given
        snapshotService.savePrices(Map.of("TS0001", new PriceSnapshot(10_300L, 10_000L, DAY1)), T);

        // when: 다음 날 시작가 = 전날 마지막 가격
        snapshotService.savePrices(Map.of("TS0001", new PriceSnapshot(10_500L, 10_300L, DAY1.plusDays(1))),
                T.plusDays(1));

        // then
        assertThat(candleRepository.findRecent("TS0001", DAY1))
                .extracting(StockDailyCandle::getTradeDate, StockDailyCandle::getOpenPrice, StockDailyCandle::getClosePrice)
                .containsExactly(tuple(DAY1, 10_000L, 10_300L), tuple(DAY1.plusDays(1), 10_300L, 10_500L));
    }

    @Test
    @DisplayName("같은 분에 두 번 저장되면 1분 기록은 1행, 일봉 종가는 마지막 가격")
    void sameMinuteRecordedOnce() {
        // given: 서버가 멈췄다 풀려 같은 분 저장이 몰아서 실행된 상황
        snapshotService.savePrices(Map.of("TS0001", new PriceSnapshot(10_100L, 10_000L, DAY1)), T);

        // when
        snapshotService.savePrices(Map.of("TS0001", new PriceSnapshot(10_200L, 10_000L, DAY1)), T);

        // then
        assertThat(historyRepository.findRecent("TS0001", T.minusMinutes(1)))
                .extracting(StockPriceHistory::getPrice)
                .containsExactly(10_100L);
        assertThat(candleRepository.findRecent("TS0001", DAY1))
                .extracting(StockDailyCandle::getClosePrice)
                .containsExactly(10_200L);
    }

    @Test
    @DisplayName("없는 종목 차트 조회는 STOCK_NOT_FOUND")
    void unknownStock() {
        assertThatThrownBy(() -> stockService.findRecentPrices("NOPE00"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.STOCK_NOT_FOUND);
    }
}
