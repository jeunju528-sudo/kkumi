package com.kkumi.portfolio.dto;

import com.kkumi.portfolio.PortfolioSnapshot;
import java.time.LocalDate;

// 차트 점 하나 (그날 마감 평가액)
public record PortfolioHistoryResponse(LocalDate date, long totalAsset) {

    public static PortfolioHistoryResponse from(PortfolioSnapshot snapshot) {
        return new PortfolioHistoryResponse(snapshot.getSnapshotDate(), snapshot.getTotalAsset());
    }
}
