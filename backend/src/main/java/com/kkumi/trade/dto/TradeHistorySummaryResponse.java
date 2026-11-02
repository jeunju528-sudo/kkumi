package com.kkumi.trade.dto;

// 기간 합계 (soldCostBasis는 실현 수익률 분모)
public record TradeHistorySummaryResponse(long realizedProfit, long soldCostBasis,
    long buyTotal, long sellTotal, long tradeCount) {
}
