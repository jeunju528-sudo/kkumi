package com.kkumi.portfolio;

import com.kkumi.trade.dto.HoldingResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

// 평가액 계산 (심장 ②, ADR 044). DB·시세 조회 없이 숫자만 받음
// 홈 조회 API와 WebSocket 푸시가 같이 사용
public final class PortfolioCalculator {

    private static final int RATE_SCALE = 4;

    private PortfolioCalculator() {
    }

    // 주식 평가액 = Σ 현재가 × 수량, 평가액 = 현금 + 주식 평가액, 총손익 = 평가액 − 시드머니
    public static PortfolioValuation calculate(long cash, long initialCash, List<HoldingResponse> holdings) {
        if (initialCash <= 0) {
            throw new IllegalArgumentException("시드머니는 1원 이상: " + initialCash);
        }

        long stockValue = 0;
        for (HoldingResponse holding : holdings) {
            // long 넘침이면 ArithmeticException (조용한 음수 방지)
            long holdingValue = Math.multiplyExact(holding.price(), holding.quantity());
            stockValue = Math.addExact(stockValue, holdingValue);
        }

        long totalAsset = Math.addExact(cash, stockValue);
        long profit = Math.subtractExact(totalAsset, initialCash);
        return new PortfolioValuation(stockValue, totalAsset, profit, profitRate(profit, initialCash));
    }

    // 소수 4자리 HALF_UP (종목 changeRate와 같은 규칙)
    private static BigDecimal profitRate(long profit, long initialCash) {
        return BigDecimal.valueOf(profit)
            .divide(BigDecimal.valueOf(initialCash), RATE_SCALE, RoundingMode.HALF_UP);
    }
}
