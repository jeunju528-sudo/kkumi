package com.kkumi.stock;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockPriceSnapshotService {
    private final StockRepository stockRepository;

    // 종목 코드별 시세 저장 (변경 감지로 update 함)
    @Transactional
    public void savePrices(Map<String, PriceSnapshot> snapshots){
        for(Stock stock : stockRepository.findAll()){
            PriceSnapshot snapshot = snapshots.get(stock.getCode());
            if(snapshot == null){
                continue;
            }
            stock.updatePrice(snapshot.currentPrice(), snapshot.dayOpenPrice(), snapshot.priceDate());
        }
    }
}
