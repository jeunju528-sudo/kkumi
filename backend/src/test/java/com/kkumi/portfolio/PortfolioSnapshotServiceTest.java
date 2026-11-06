package com.kkumi.portfolio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.kkumi.global.error.BusinessException;
import com.kkumi.global.error.ErrorCode;
import com.kkumi.member.Member;
import com.kkumi.member.MemberRepository;
import com.kkumi.member.SeedMoney;
import com.kkumi.trade.HoldingService;
import com.kkumi.trade.dto.HoldingResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// 하루 평가액 기록: 저장 값, 같은 날 재실행 시 건너뜀(멱등) (ADR 049)
@ExtendWith(MockitoExtension.class)
class PortfolioSnapshotServiceTest {

    private static final Long MEMBER_ID = 1L;
    private static final LocalDate DATE = LocalDate.of(2026, 11, 5);

    @InjectMocks
    private PortfolioSnapshotService snapshotService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private HoldingService holdingService;

    @Mock
    private PortfolioSnapshotRepository snapshotRepository;

    @Test
    @DisplayName("현금 + 주식 평가액을 그날 날짜로 저장")
    void recordSnapshot_success() {
        // given: 시드 500만, 50만 원 써서 현금 450만 / 10주 × 현재가 52,000 = 52만
        Member member = Member.join("kakao-1", "꿈이", SeedMoney.SMALL);
        member.withdraw(500_000);
        HoldingResponse holding = new HoldingResponse("KK0001", "꿈이전자", 10, new BigDecimal("50000"), 52_000);
        given(snapshotRepository.existsByMemberIdAndSnapshotDate(MEMBER_ID, DATE)).willReturn(false);
        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));
        given(holdingService.findHoldings(MEMBER_ID)).willReturn(List.of(holding));

        // when
        boolean isSaved = snapshotService.recordSnapshot(MEMBER_ID, DATE);

        // then: 4,500,000 + 520,000 = 5,020,000
        assertThat(isSaved).isTrue();
        ArgumentCaptor<PortfolioSnapshot> saved = ArgumentCaptor.forClass(PortfolioSnapshot.class);
        verify(snapshotRepository).save(saved.capture());
        assertThat(saved.getValue().getTotalAsset()).isEqualTo(5_020_000);
        assertThat(saved.getValue().getSnapshotDate()).isEqualTo(DATE);
        assertThat(saved.getValue().getMember()).isSameAs(member);
    }

    @Test
    @DisplayName("같은 날 기록이 이미 있으면 계산·저장 없이 건너뜀")
    void recordSnapshot_alreadyRecorded() {
        // given
        given(snapshotRepository.existsByMemberIdAndSnapshotDate(MEMBER_ID, DATE)).willReturn(true);

        // when
        boolean isSaved = snapshotService.recordSnapshot(MEMBER_ID, DATE);

        // then
        assertThat(isSaved).isFalse();
        verify(memberRepository, never()).findById(any());
        verify(snapshotRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원이 없으면 MEMBER_NOT_FOUND, 저장 안 함")
    void recordSnapshot_memberNotFound() {
        // given
        given(snapshotRepository.existsByMemberIdAndSnapshotDate(MEMBER_ID, DATE)).willReturn(false);
        given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> snapshotService.recordSnapshot(MEMBER_ID, DATE))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode").isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        verify(snapshotRepository, never()).save(any());
    }

    @Test
    @DisplayName("기간 시작일: 주는 6일 전, 월은 29일 전 (오늘 포함 7·30칸)")
    void chartPeriod_from() {
        LocalDate today = LocalDate.of(2026, 11, 14);

        assertThat(ChartPeriod.WEEK.toFrom(today)).isEqualTo(LocalDate.of(2026, 11, 8));
        assertThat(ChartPeriod.MONTH.toFrom(today)).isEqualTo(LocalDate.of(2026, 10, 16));
    }
}
