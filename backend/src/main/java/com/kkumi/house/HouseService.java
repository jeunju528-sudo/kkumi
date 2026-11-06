package com.kkumi.house;

import com.kkumi.apartment.AptDeal;
import com.kkumi.apartment.AptDealRepository;
import com.kkumi.apartment.HousePriceRule;
import com.kkumi.global.error.BusinessException;
import com.kkumi.global.error.ErrorCode;
import com.kkumi.house.dto.BuyHouseResponse;
import com.kkumi.house.dto.HouseResponse;
import com.kkumi.house.dto.MyHousesResponse;
import com.kkumi.member.Member;
import com.kkumi.member.MemberRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HouseService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final MemberRepository memberRepository;
    private final AptDealRepository aptDealRepository;
    private final HouseRepository houseRepository;

    // 회원 락 먼저 (매수·매도와 같은 순서, ADR 025) → 거래 검증 → 현금에서 40% 차감 → 집 저장
    @Transactional
    public BuyHouseResponse buyHouse(Long memberId, Long dealId) {
        Member member = memberRepository.findByIdForUpdate(memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        AptDeal deal = aptDealRepository.findById(dealId)
            .orElseThrow(() -> new BusinessException(ErrorCode.DEAL_NOT_FOUND));
        if (!deal.isAvailable(LocalDate.now(KST))) {
            throw new BusinessException(ErrorCode.DEAL_NOT_AVAILABLE);
        }
        // 락 안에서 확인해 연타해도 한 채만
        if (houseRepository.existsByMemberIdAndDealId(memberId, dealId)) {
            throw new BusinessException(ErrorCode.ALREADY_OWNED_DEAL);
        }

        long beforeCount = houseRepository.countByMemberId(memberId);
        // 결제는 현금 기준, 부족하면 INSUFFICIENT_CASH (자동 매도 없음)
        member.withdraw(HousePriceRule.ownAmount(deal.getPrice()));
        House house = houseRepository.save(House.purchase(member, deal, deal.getPrice()));

        Tier before = Tier.of(beforeCount);
        Tier after = Tier.of(beforeCount + 1);
        return new BuyHouseResponse(house.getId(), after, after != before);
    }

    public MyHousesResponse findMyHouses(Long memberId) {
        List<HouseResponse> houses = houseRepository.findAllByMemberIdWithDeal(memberId).stream()
            .map(HouseResponse::from)
            .toList();
        return new MyHousesResponse(houses, houses.size(), Tier.of(houses.size()));
    }
}
