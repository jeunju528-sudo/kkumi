package com.kkumi.portfolio;

import com.kkumi.global.error.BusinessException;
import com.kkumi.global.error.ErrorCode;
import com.kkumi.member.Member;
import com.kkumi.member.MemberRepository;
import com.kkumi.portfolio.dto.PortfolioHistoryResponse;
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
public class PortfolioSnapshotService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final MemberRepository memberRepository;
    private final HoldingService holdingService;
    private final PortfolioSnapshotRepository snapshotRepository;

    // 기록 대상 회원 id 전체
    public List<Long> findAllMemberIds() {
        return memberRepository.findAllIds();
    }

    // 회원 1명 기록, 회원마다 트랜잭션 분리 (한 명 실패해도 나머지 저장)
    // 이미 있으면 건너뜀 → 재시작·재실행해도 하루 1행 (멱등)
    @Transactional
    public boolean recordSnapshot(Long memberId, LocalDate snapshotDate) {
        if (snapshotRepository.existsByMemberIdAndSnapshotDate(memberId, snapshotDate)) {
            return false;
        }
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        List<HoldingResponse> holdings = holdingService.findHoldings(memberId);
        // 홈 평가액과 같은 계산 (ADR 044)
        PortfolioValuation valuation = PortfolioCalculator.calculate(member.getCash(), member.getInitialCash(), holdings);

        snapshotRepository.save(PortfolioSnapshot.of(member, snapshotDate, valuation.totalAsset()));
        return true;
    }

    // 차트 기간 기록 (어제까지, 빈 날은 행 없음)
    public List<PortfolioHistoryResponse> findHistory(Long memberId, ChartPeriod period) {
        LocalDate from = period.toFrom(LocalDate.now(KST));
        return snapshotRepository.findAllByMemberIdSince(memberId, from).stream()
            .map(PortfolioHistoryResponse::from)
            .toList();
    }
}
