package com.kkumi.trade.dto;

import com.kkumi.trade.TradeHistory;
import com.kkumi.trade.TradeType;
import java.time.LocalDateTime;

// 거래내역 한 줄 (realizedProfit은 매도만)
public record TradeHistoryResponse(long id, String code, String name, TradeType tradeType,
    long price, int quantity, Long realizedProfit, LocalDateTime tradedAt) {

    public static TradeHistoryResponse from(TradeHistory history) {
        return new TradeHistoryResponse(history.getId(), history.getStock().getCode(),
            history.getStock().getName(), history.getTradeType(), history.getPrice(),
            history.getQuantity(), history.getRealizedProfit(), history.getTradedAt());
    }
}
