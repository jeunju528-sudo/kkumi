package com.kkumi.portfolio;

import com.kkumi.global.error.BusinessException;
import com.kkumi.global.error.ErrorCode;
import com.kkumi.house.HouseService;
import com.kkumi.member.Member;
import com.kkumi.member.MemberRepository;
import com.kkumi.portfolio.dto.PortfolioResponse;
import com.kkumi.trade.HoldingService;
import com.kkumi.trade.dto.HoldingResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final MemberRepository memberRepository;
    private final HoldingService holdingService;
    private final HouseService houseService;
    private final PortfolioSnapshotRepository snapshotRepository;

    // 조회만 담당, 계산은 PortfolioCalculator (ADR 044). 읽기라 락 없음
    public PortfolioResponse getMyPortfolio(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        // /api/holdings와 같은 행 (현재가 포함)
        List<HoldingResponse> holdings = holdingService.findHoldings(memberId);
        List<Long> housePurchasePrices = houseService.findPurchasePrices(memberId);

        PortfolioValuation valuation = PortfolioCalculator.calculate(
            member.getCash(), member.getInitialCash(), holdings, housePurchasePrices);
        // 어제 마감 기록 없으면 null (가입 첫날, ADR 054)
        LocalDate yesterday = LocalDate.now(KST).minusDays(1);
        DailyChange dailyChange = snapshotRepository.findByMemberIdAndSnapshotDate(memberId, yesterday)
            .map(snapshot -> PortfolioCalculator.calculateDailyChange(valuation.netWorth(), snapshot.getNetWorth()))
            .orElse(null);
        return PortfolioResponse.of(member, valuation, dailyChange, holdings);
    }
}
