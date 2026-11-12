package com.kkumi.portfolio;

import java.math.BigDecimal;

// 평가액·순자산 계산 결과 (profitRate 0.0123 = +1.23%, 시드머니 대비)
// totalAsset = 평가액(현금 + 주식), netWorth = 순자산(평가액 + 내 집 지분)
public record PortfolioValuation(long stockValue, long totalAsset, long houseEquity, long netWorth,
                                 long profit, BigDecimal profitRate) {
}
