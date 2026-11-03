package com.kkumi.portfolio.dto;

import com.kkumi.member.Member;
import com.kkumi.portfolio.PortfolioValuation;
import com.kkumi.trade.dto.HoldingResponse;
import java.math.BigDecimal;
import java.util.List;

// 홈 평가액 카드·보유 목록 (profitRate 0.0123 = +1.23%)
public record PortfolioResponse(long cash, long initialCash, long stockValue, long totalAsset,
    long profit, BigDecimal profitRate, List<HoldingResponse> holdings) {

    public static PortfolioResponse of(Member member, PortfolioValuation valuation, List<HoldingResponse> holdings) {
        return new PortfolioResponse(member.getCash(), member.getInitialCash(), valuation.stockValue(),
            valuation.totalAsset(), valuation.profit(), valuation.profitRate(), holdings);
    }
}
