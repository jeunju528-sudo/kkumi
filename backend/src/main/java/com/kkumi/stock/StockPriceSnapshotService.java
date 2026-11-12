package com.kkumi.stock;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockPriceSnapshotService {
    private final StockRepository stockRepository;
    private final StockPriceHistoryRepository historyRepository;
    private final StockDailyCandleRepository candleRepository;

    // 종목 코드별 시세 저장 (변경 감지로 update 함) + 1분 기록 추가 + 오늘 일봉 갱신 (차트용, ADR 056)
    // recordedAt은 분 단위, 같은 분 기록이 이미 있으면 1분 기록만 생략 (현재가·일봉은 갱신)
    @Transactional
    public void savePrices(Map<String, PriceSnapshot> snapshots, LocalDateTime recordedAt){
        boolean isRecorded = historyRepository.existsByRecordedAt(recordedAt);
        Map<String, StockDailyCandle> candles = findCandles(snapshots);
        List<StockPriceHistory> histories = new ArrayList<>();
        List<StockDailyCandle> newCandles = new ArrayList<>();
        for(Stock stock : stockRepository.findAll()){
            PriceSnapshot snapshot = snapshots.get(stock.getCode());
            if(snapshot == null){
                continue;
            }
            long price = snapshot.currentPrice();
            stock.updatePrice(price, snapshot.dayOpenPrice(), snapshot.priceDate());
            if(!isRecorded){
                histories.add(StockPriceHistory.of(stock.getCode(), price, recordedAt));
            }

            // 일봉: 있으면 고가·저가·종가 갱신(변경 감지), 없으면 그날 첫 일봉 생성
            StockDailyCandle candle = candles.get(candleKey(stock.getCode(), snapshot.priceDate()));
            if(candle == null){
                newCandles.add(StockDailyCandle.open(stock.getCode(), snapshot.priceDate(), snapshot.dayOpenPrice(), price));
            } else {
                candle.update(price);
            }
        }
        historyRepository.saveAll(histories);
        candleRepository.saveAll(newCandles);
    }

    // 스냅샷 날짜의 일봉을 한 번에 조회해서 (종목 코드 + 날짜) 키로
    private Map<String, StockDailyCandle> findCandles(Map<String, PriceSnapshot> snapshots) {
        Set<LocalDate> dates = snapshots.values().stream()
                .map(PriceSnapshot::priceDate)
                .collect(Collectors.toSet());
        Map<String, StockDailyCandle> candles = new HashMap<>();
        if(dates.isEmpty()){
            return candles;
        }
        for(StockDailyCandle candle : candleRepository.findByTradeDateIn(dates)){
            candles.put(candleKey(candle.getStockCode(), candle.getTradeDate()), candle);
        }
        return candles;
    }

    private static String candleKey(String code, LocalDate date) {
        return code + "#" + date;
    }

    // 오래된 시세 기록 삭제, 삭제 행 수 반환
    @Transactional
    public int deleteHistoryBefore(LocalDateTime before) {
        return historyRepository.deleteRecordedBefore(before);
    }
}
