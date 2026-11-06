package com.kkumi.house;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.kkumi.apartment.AptComplex;
import com.kkumi.apartment.AptDeal;
import com.kkumi.apartment.AptDealRepository;
import com.kkumi.global.error.BusinessException;
import com.kkumi.global.error.ErrorCode;
import com.kkumi.house.dto.BuyHouseResponse;
import com.kkumi.member.Member;
import com.kkumi.member.MemberRepository;
import com.kkumi.member.SeedMoney;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

// 집 사기: 현금에서 집값 40% 차감, 변신 단계 (도메인 규칙, ADR 025·047)
@ExtendWith(MockitoExtension.class)
class HouseServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long DEAL_ID = 10L;
    private static final LocalDate TODAY = LocalDate.now(ZoneId.of("Asia/Seoul"));
    private static final AptComplex COMPLEX = AptComplex.of("51190", "황지동", "1", "꿈이아파트", 2001, "강원 태백시");

    @InjectMocks
    private HouseService houseService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AptDealRepository aptDealRepository;

    @Mock
    private HouseRepository houseRepository;

    @Test
    @DisplayName("집을 사면 현금에서 집값 40%가 빠지고 집 저장, 첫 집이면 서민으로 변신")
    void buyHouse_success() {
        // given: 시드 5,000만, 1억 집 → 내 돈 4,000만
        Member member = member(SeedMoney.LARGE);
        AptDeal deal = deal(100_000_000, TODAY);
        givenMemberAndDeal(member, deal);
        given(houseRepository.existsByMemberIdAndDealId(MEMBER_ID, DEAL_ID)).willReturn(false);
        given(houseRepository.countByMemberId(MEMBER_ID)).willReturn(0L);
        given(houseRepository.save(any(House.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        BuyHouseResponse response = houseService.buyHouse(MEMBER_ID, DEAL_ID);

        // then
        assertThat(member.getCash()).isEqualTo(10_000_000);
        ArgumentCaptor<House> saved = ArgumentCaptor.forClass(House.class);
        verify(houseRepository).save(saved.capture());
        assertThat(saved.getValue().getPurchasePrice()).isEqualTo(100_000_000);
        assertThat(saved.getValue().getDeal()).isSameAs(deal);
        assertThat(response.tier()).isEqualTo(Tier.SEOMIN);
        assertThat(response.isTierUp()).isTrue();
    }

    @Test
    @DisplayName("현금이 딱 내 돈만큼이면 살 수 있고 현금 0원")
    void buyHouse_exactCash() {
        // given: 현금 5,000만, 1억 2,500만 집 → 내 돈 5,000만
        Member member = member(SeedMoney.LARGE);
        AptDeal deal = deal(125_000_000, TODAY);
        givenMemberAndDeal(member, deal);
        given(houseRepository.countByMemberId(MEMBER_ID)).willReturn(0L);
        given(houseRepository.save(any(House.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        houseService.buyHouse(MEMBER_ID, DEAL_ID);

        // then
        assertThat(member.getCash()).isZero();
    }

    @ParameterizedTest
    @CsvSource({
        "1, JOKBU, true",    // 2채째: 서민 → 졸부
        "2, JOKBU, false",   // 3채째: 졸부 그대로
        "4, GAPBU, true",    // 5채째: 졸부 → 갑부
        "5, GAPBU, false"    // 6채째: 갑부 그대로
    })
    @DisplayName("산 뒤 집 수로 변신 단계와 변신 여부 계산")
    void buyHouse_tier(long beforeCount, Tier expectedTier, boolean expectedTierUp) {
        // given
        Member member = member(SeedMoney.LARGE);
        AptDeal deal = deal(10_000_000, TODAY);
        givenMemberAndDeal(member, deal);
        given(houseRepository.countByMemberId(MEMBER_ID)).willReturn(beforeCount);
        given(houseRepository.save(any(House.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        BuyHouseResponse response = houseService.buyHouse(MEMBER_ID, DEAL_ID);

        // then
        assertThat(response.tier()).isEqualTo(expectedTier);
        assertThat(response.isTierUp()).isEqualTo(expectedTierUp);
    }

    @Test
    @DisplayName("현금이 내 돈보다 1원이라도 적으면 실패, 현금 그대로·집 저장 안 함 (자동 매도 없음)")
    void buyHouse_insufficientCash() {
        // given: 현금 5,000만, 1억 2,500만 1만 원 집 → 내 돈 5,000만 4,000원
        Member member = member(SeedMoney.LARGE);
        AptDeal deal = deal(125_010_000, TODAY);
        givenMemberAndDeal(member, deal);
        given(houseRepository.countByMemberId(MEMBER_ID)).willReturn(0L);

        // when, then
        assertThatThrownBy(() -> houseService.buyHouse(MEMBER_ID, DEAL_ID))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.INSUFFICIENT_CASH);
        assertThat(member.getCash()).isEqualTo(50_000_000);
        verify(houseRepository, never()).save(any());
    }

    @Test
    @DisplayName("없는 거래면 DEAL_NOT_FOUND")
    void buyHouse_dealNotFound() {
        // given
        given(memberRepository.findByIdForUpdate(MEMBER_ID)).willReturn(Optional.of(member(SeedMoney.LARGE)));
        given(aptDealRepository.findById(DEAL_ID)).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> houseService.buyHouse(MEMBER_ID, DEAL_ID))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DEAL_NOT_FOUND);
        verify(houseRepository, never()).save(any());
    }

    @Test
    @DisplayName("6개월 지난 거래면 DEAL_NOT_AVAILABLE, 현금 그대로")
    void buyHouse_expiredDeal() {
        // given
        Member member = member(SeedMoney.LARGE);
        givenMemberAndDeal(member, deal(10_000_000, AptDeal.marketPriceSince(TODAY).minusDays(1)));

        // when, then
        assertThatThrownBy(() -> houseService.buyHouse(MEMBER_ID, DEAL_ID))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.DEAL_NOT_AVAILABLE);
        assertThat(member.getCash()).isEqualTo(50_000_000);
        verify(houseRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 산 거래면 ALREADY_OWNED_DEAL, 현금 그대로")
    void buyHouse_alreadyOwned() {
        // given
        Member member = member(SeedMoney.LARGE);
        givenMemberAndDeal(member, deal(10_000_000, TODAY));
        given(houseRepository.existsByMemberIdAndDealId(MEMBER_ID, DEAL_ID)).willReturn(true);

        // when, then
        assertThatThrownBy(() -> houseService.buyHouse(MEMBER_ID, DEAL_ID))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.ALREADY_OWNED_DEAL);
        assertThat(member.getCash()).isEqualTo(50_000_000);
        verify(houseRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원은 비관적 락으로 조회 (매수·매도와 같은 락, ADR 025)")
    void buyHouse_usesLock() {
        // given
        givenMemberAndDeal(member(SeedMoney.LARGE), deal(10_000_000, TODAY));
        given(houseRepository.save(any(House.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        houseService.buyHouse(MEMBER_ID, DEAL_ID);

        // then
        verify(memberRepository).findByIdForUpdate(MEMBER_ID);
        verify(memberRepository, never()).findById(any());
    }

    private static Member member(SeedMoney seedMoney) {
        Member member = Member.join("kakao-1", "꿈이", seedMoney);
        ReflectionTestUtils.setField(member, "id", MEMBER_ID);
        return member;
    }

    private static AptDeal deal(long price, LocalDate contractDate) {
        AptDeal deal = AptDeal.of(COMPLEX, new BigDecimal("59.00"), 7, price, contractDate);
        ReflectionTestUtils.setField(deal, "id", DEAL_ID);
        return deal;
    }

    private void givenMemberAndDeal(Member member, AptDeal deal) {
        given(memberRepository.findByIdForUpdate(MEMBER_ID)).willReturn(Optional.of(member));
        given(aptDealRepository.findById(DEAL_ID)).willReturn(Optional.of(deal));
    }
}
