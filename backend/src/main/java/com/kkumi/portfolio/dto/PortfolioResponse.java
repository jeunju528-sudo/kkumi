package com.kkumi.portfolio.dto;

import com.kkumi.member.Member;
import com.kkumi.portfolio.DailyChange;
import com.kkumi.portfolio.PortfolioValuation;
import com.kkumi.trade.dto.HoldingResponse;
import java.math.BigDecimal;
import java.util.List;

// 홈 순자산 카드·보유 목록 (rate 0.0123 = +1.23%)
// totalAsset(평가액)은 집 추천·첫 집 목표 기준, netWorth(순자산)는 카드·차트 기준 (ADR 054)
// dailyChange·dailyChangeRate: 어제 마감 기록 없으면 null (첫날)
public record PortfolioResponse(long cash, long initialCash, long stockValue, long totalAsset,
    long houseEquity, long netWorth, long profit, BigDecimal profitRate,
    Long dailyChange, BigDecimal dailyChangeRate, List<HoldingResponse> holdings) {

    public static PortfolioResponse of(Member member, PortfolioValuation valuation, DailyChange dailyChange,
                                       List<HoldingResponse> holdings) {
        Long dailyAmount = dailyChange == null ? null : dailyChange.amount();
        BigDecimal dailyRate = dailyChange == null ? null : dailyChange.rate();
        return new PortfolioResponse(member.getCash(), member.getInitialCash(), valuation.stockValue(),
            valuation.totalAsset(), valuation.houseEquity(), valuation.netWorth(), valuation.profit(),
            valuation.profitRate(), dailyAmount, dailyRate, holdings);
    }
}
