package com.kkumi.member;

import com.kkumi.global.error.BusinessException;
import com.kkumi.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 기본적으로 서버, DB없이 직접 불러서 결과가 기대값과 같은지 자동으로 확인하는 코드

// 현금 차감·입금 (심장 ①)
class MemberTest {

    @Test
    @DisplayName("현금보다 적은 금액이면 차감")
    void withdraw_lessThanCash() {
        // given
        Member member = Member.join("1234", "꿈이", SeedMoney.SMALL);

        // when
        member.withdraw(4000000L);

        // then
        assertThat(member.getCash()).isEqualTo(1000000L);
    }

    @Test
    @DisplayName("현금과 같은 금액이면 잔액 0")
    void withdraw_sameAsCash() {
        // given
        Member member = Member.join("1234", "꿈이", SeedMoney.SMALL);

        // when
        member.withdraw(5000000L);

        // then
        assertThat(member.getCash()).isEqualTo(0);
    }

    @Test
    @DisplayName("현금보다 1원 많으면 INSUFFICIENT_CASH")
    void withdraw_moreThanCash() {
        // given
        Member member = Member.join("1234", "꿈이", SeedMoney.SMALL);

        // then
        assertThatThrownBy(() -> member.withdraw(5000001L))
                .isInstanceOf(BusinessException.class)
                // 검사 대상을 예외 객체에서 그 안의 errorCode 필드 값으로 바꿈
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INSUFFICIENT_CASH);

        assertThat(member.getCash()).isEqualTo(5000000L);
    }

    @Test
    @DisplayName("입금하면 현금 증가")
    void deposit() {
        // given
        Member member = Member.join("1234", "꿈이", SeedMoney.SMALL);

        // when
        member.deposit(1000000L);

        // then
        assertThat(member.getCash()).isEqualTo(6000000L);
    }
}
