package com.kkumi.apartment;

import static org.assertj.core.api.Assertions.assertThat;

import com.kkumi.house.House;
import com.kkumi.house.HouseRepository;
import com.kkumi.member.Member;
import com.kkumi.member.MemberRepository;
import com.kkumi.member.SeedMoney;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Limit;
import org.springframework.transaction.annotation.Transactional;

// 목표 집 조회 쿼리 (ADR 055)
@SpringBootTest
@Transactional
class AptDealRepositoryTest {

    // 평가액 4,000,000 → maxPrice 10,000,000 (4,000,000 × 100 / 40)
    private static final long MAX_PRICE = 10_000_000L;
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 12);
    private static final LocalDate SINCE = AptDeal.marketPriceSince(TODAY);

    @Autowired
    private AptDealRepository aptDealRepository;

    @Autowired
    private AptComplexRepository aptComplexRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private HouseRepository houseRepository;

    private AptComplex complex;
    private Member me;

    @BeforeEach
    void setUp() {
        complex = aptComplexRepository.save(AptComplex.of("51190", "장성동", "1", "태백한신", 1995, "강원 태백시"));
        me = memberRepository.save(Member.join("kakao-me", "나", SeedMoney.SMALL));
    }

    private AptDeal saveDeal(long price) {
        return aptDealRepository.save(AptDeal.of(complex, new BigDecimal("39.00"), 3, price, TODAY.minusDays(1)));
    }

    private List<AptDeal> findGoal(Long memberId) {
        return aptDealRepository.findGoalCandidates(SINCE, MAX_PRICE, memberId, Limit.of(1));
    }

    @Test
    @DisplayName("집값 = maxPrice는 살 수 있어서 목표 아님, maxPrice + 1원이 목표")
    void goalStartsAboveMaxPrice() {
        // given
        saveDeal(MAX_PRICE);
        AptDeal justAbove = saveDeal(MAX_PRICE + 1);
        saveDeal(MAX_PRICE + 2);

        // when
        List<AptDeal> goal = findGoal(me.getId());

        // then
        assertThat(goal).extracting(AptDeal::getId).containsExactly(justAbove.getId());
        assertThat(HousePriceRule.ownAmount(goal.get(0).getPrice())).isEqualTo(4_000_001L);
    }

    @Test
    @DisplayName("이미 산 거래는 목표에서 빠지고 다음으로 싼 집이 목표")
    void excludeBoughtDeal() {
        // given
        AptDeal bought = saveDeal(MAX_PRICE + 1);
        AptDeal next = saveDeal(MAX_PRICE + 2);
        houseRepository.save(House.purchase(me, bought, bought.getPrice()));

        // when
        List<AptDeal> goal = findGoal(me.getId());

        // then
        assertThat(goal).extracting(AptDeal::getId).containsExactly(next.getId());
    }

    @Test
    @DisplayName("다른 회원이 산 거래는 나에게 그대로 목표")
    void otherMemberBoughtDealStillGoal() {
        // given
        Member other = memberRepository.save(Member.join("kakao-other", "남", SeedMoney.SMALL));
        AptDeal deal = saveDeal(MAX_PRICE + 1);
        houseRepository.save(House.purchase(other, deal, deal.getPrice()));

        // when
        List<AptDeal> goal = findGoal(me.getId());

        // then
        assertThat(goal).extracting(AptDeal::getId).containsExactly(deal.getId());
    }

    @Test
    @DisplayName("모든 집이 maxPrice 이하면 목표 없음")
    void noGoalWhenAllAffordable() {
        // given
        saveDeal(MAX_PRICE - 1);
        saveDeal(MAX_PRICE);

        // when
        List<AptDeal> goal = findGoal(me.getId());

        // then
        assertThat(goal).isEmpty();
    }
}
