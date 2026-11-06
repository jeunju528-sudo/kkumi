package com.kkumi.apartment;

import com.kkumi.apartment.dto.AptRecommendPageResponse;
import com.kkumi.apartment.dto.AptRecommendResponse;
import com.kkumi.global.error.BusinessException;
import com.kkumi.global.error.ErrorCode;
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

    private final AptDealRepository aptDealRepository;
    private final PortfolioService portfolioService;

    // 평가액·현금은 PortfolioService 값 그대로 (계산 한 곳, ADR 044)
    public AptRecommendPageResponse findRecommendations(Long memberId, String keyword, boolean isAffordableOnly,
                                                        int page, int size) {
        PortfolioResponse portfolio = portfolioService.getMyPortfolio(memberId);
        long maxPrice = HousePriceRule.maxAffordablePrice(portfolio.totalAsset());
        LocalDate since = AptDeal.marketPriceSince(LocalDate.now(KST));
        String searchKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.strip();
        Pageable pageable = PageRequest.of(page, size);

        Slice<AptDeal> deals = isAffordableOnly
            ? aptDealRepository.findAffordableSlice(since, maxPrice, searchKeyword, pageable)
            : aptDealRepository.findRecentSlice(since, searchKeyword, pageable);

        return AptRecommendPageResponse.of(deals.map(deal -> toResponse(deal, portfolio)), maxPrice);
    }

    // 계약서 화면용 거래 1건 (살 수 없는 거래면 예외, 최종 검증은 집 사기에서 다시)
    public AptRecommendResponse getDeal(Long memberId, Long dealId) {
        AptDeal deal = aptDealRepository.findWithComplexById(dealId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DEAL_NOT_FOUND));
        if (!deal.isAvailable(LocalDate.now(KST))) {
            throw new BusinessException(ErrorCode.DEAL_NOT_AVAILABLE);
        }
        return toResponse(deal, portfolioService.getMyPortfolio(memberId));
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
