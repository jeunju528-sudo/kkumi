package com.kkumi.trade;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// 매수·매도 흐름 (심장 ①, ADR 011·025)
class TradeServiceTest {

	@Test
	@Disabled("작성 전")
	@DisplayName("매수하면 현금 차감, 보유 증가, 이력 저장")
	void buy_success() {
		// given

		// when

		// then
	}

	@Test
	@Disabled("작성 전")
	@DisplayName("현금이 부족하면 매수 실패")
	void buy_insufficientCash() {
		// given

		// when

		// then
	}

	@Test
	@Disabled("작성 전")
	@DisplayName("매도하면 현금 입금, 실현손익 이력 저장")
	void sell_success() {
		// given

		// when

		// then
	}

	@Test
	@Disabled("작성 전")
	@DisplayName("전량 매도하면 보유 행 삭제")
	void sell_all_deleteHolding() {
		// given

		// when

		// then
	}

	@Test
	@Disabled("작성 전")
	@DisplayName("보유하지 않은 종목을 팔면 INSUFFICIENT_HOLDING")
	void sell_noHolding() {
		// given

		// when

		// then
	}

	@Test
	@Disabled("작성 전")
	@DisplayName("매수·매도 시 회원을 비관적 락으로 조회")
	void trade_lockMember() {
		// given

		// when

		// then
	}
}
