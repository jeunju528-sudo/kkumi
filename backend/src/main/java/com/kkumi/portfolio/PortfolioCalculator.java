package com.kkumi.portfolio;

import com.kkumi.apartment.HousePriceRule;
import com.kkumi.trade.dto.HoldingResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

// 평가액·순자산 계산 (심장 ②, ADR 044, 054). DB·시세 조회 없이 숫자만 받음
// 홈 조회 API, WebSocket 푸시, 일별 기록이 같이 사용
public final class PortfolioCalculator {

    private static final int RATE_SCALE = 4;

    private PortfolioCalculator() {
    }

    // 주식 평가액 = Σ 현재가 × 수량, 평가액 = 현금 + 주식 평가액 (집 추천 기준)
    // 내 집 지분 = Σ 구매가 × 40% (집마다 올림 → 결제 때 빠진 현금과 같은 값)
    // 순자산 = 평가액 + 내 집 지분, 총손익 = 순자산 − 시드머니 (집 사기 전후 변화 0)
    public static PortfolioValuation calculate(long cash, long initialCash, List<HoldingResponse> holdings,
                                               List<Long> housePurchasePrices) {
        if (initialCash <= 0) {
            throw new IllegalArgumentException("시드머니는 1원 이상: " + initialCash);
        }

        long stockValue = 0;
        for (HoldingResponse holding : holdings) {
            // long 넘침이면 ArithmeticException (조용한 음수 방지)
            long holdingValue = Math.multiplyExact(holding.price(), holding.quantity());
            stockValue = Math.addExact(stockValue, holdingValue);
        }

        long houseEquity = 0;
        for (long purchasePrice : housePurchasePrices) {
            houseEquity = Math.addExact(houseEquity, HousePriceRule.ownAmount(purchasePrice));
        }

        long totalAsset = Math.addExact(cash, stockValue);
        long netWorth = Math.addExact(totalAsset, houseEquity);
        long profit = Math.subtractExact(netWorth, initialCash);
        return new PortfolioValuation(stockValue, totalAsset, houseEquity, netWorth, profit, toRate(profit, initialCash));
    }

    // 오늘 증감 = 지금 순자산 − 어제 마감 순자산. 어제 순자산 0 이하면 비율 null (0 나누기 방지)
    public static DailyChange calculateDailyChange(long netWorth, long yesterdayNetWorth) {
        long amount = Math.subtractExact(netWorth, yesterdayNetWorth);
        BigDecimal rate = yesterdayNetWorth > 0 ? toRate(amount, yesterdayNetWorth) : null;
        return new DailyChange(amount, rate);
    }

    // 소수 4자리 HALF_UP (종목 changeRate와 같은 규칙)
    private static BigDecimal toRate(long amount, long base) {
        return BigDecimal.valueOf(amount)
            .divide(BigDecimal.valueOf(base), RATE_SCALE, RoundingMode.HALF_UP);
    }
}
