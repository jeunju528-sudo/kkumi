package com.kkumi.trade.dto;

// 종목별 실현손익 (costBasis는 판 수량의 매입 원가 합)
public record StockProfitResponse(String code, String name, long profit, long costBasis, long sellCount) {
}
