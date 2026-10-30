package com.kkumi.trade.dto;

import com.kkumi.trade.StockHolding;
import java.math.BigDecimal;

// 보유 종목 한 줄 (price는 현재가)
public record HoldingResponse(String code, String name, int quantity, BigDecimal avgPrice, long price) {

    public static HoldingResponse of(StockHolding holding, long price) {
        return new HoldingResponse(holding.getStock().getCode(), holding.getStock().getName(),
            holding.getQuantity(), holding.getAvgPrice(), price);
    }
}
