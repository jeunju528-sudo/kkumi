package com.kkumi.portfolio.dto;

import com.kkumi.portfolio.PortfolioSnapshot;
import java.time.LocalDate;

// 차트 점 하나 (그날 마감 순자산, ADR 054)
public record PortfolioHistoryResponse(LocalDate date, long netWorth) {

    public static PortfolioHistoryResponse from(PortfolioSnapshot snapshot) {
        return new PortfolioHistoryResponse(snapshot.getSnapshotDate(), snapshot.getNetWorth());
    }
}
