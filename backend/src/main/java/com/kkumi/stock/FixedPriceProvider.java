package com.kkumi.stock;

import org.springframework.stereotype.Component;

@Component
public class FixedPriceProvider implements StockPriceProvider{

    @Override
    public long getCurrentPrice(String code) {
        // 임시 고정가. 가상 시세 구현(ADR 029) 시 교체
        return 10_000L;
    }
}
