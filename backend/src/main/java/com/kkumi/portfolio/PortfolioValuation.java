package com.kkumi.portfolio;

import java.math.BigDecimal;

// 평가액 계산 결과 (profitRate 0.0123 = +1.23%, 시드머니 대비)
public record PortfolioValuation(long stockValue, long totalAsset, long profit, BigDecimal profitRate) {
}
