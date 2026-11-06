package com.kkumi.apartment;

import com.kkumi.apartment.dto.AptRecommendPageResponse;
import com.kkumi.apartment.dto.AptRecommendResponse;
import com.kkumi.portfolio.PortfolioService;
import com.kkumi.portfolio.dto.PortfolioResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AptRecommendService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    // 시세 기간 (ADR 015)
    private static final int MARKET_PRICE_MONTHS = 6;

    private final AptDealRepository aptDealRepository;
    private final PortfolioService portfolioService;

    // 평가액·현금은 PortfolioService 값 그대로 (계산 한 곳, ADR 044)
    public AptRecommendPageResponse findRecommendations(Long memberId, String keyword, boolean isAffordableOnly,
                                                        int page, int size) {
        PortfolioResponse portfolio = portfolioService.getMyPortfolio(memberId);
        long maxPrice = HousePriceRule.maxAffordablePrice(portfolio.totalAsset());
        LocalDate since = LocalDate.now(KST).minusMonths(MARKET_PRICE_MONTHS);
        String searchKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.strip();
        Pageable pageable = PageRequest.of(page, size);

        Slice<AptDeal> deals = isAffordableOnly
            ? aptDealRepository.findAffordableSlice(since, maxPrice, searchKeyword, pageable)
            : aptDealRepository.findRecentSlice(since, searchKeyword, pageable);

        return AptRecommendPageResponse.of(deals.map(deal -> toResponse(deal, portfolio)), maxPrice);
    }

    private AptRecommendResponse toResponse(AptDeal deal, PortfolioResponse portfolio) {
        long ownAmount = HousePriceRule.ownAmount(deal.getPrice());
        AffordabilityCheck check = AffordabilityCheck.of(ownAmount, portfolio.cash(), portfolio.totalAsset());
        AptComplex complex = deal.getComplex();
        return new AptRecommendResponse(deal.getId(), complex.getName(), complex.getRegionName(), deal.getArea(),
            deal.getFloor(), deal.getPrice(), deal.getContractDate(), ownAmount,
            HousePriceRule.loanAmount(deal.getPrice()), check.affordability(), check.shortage());
    }
}
