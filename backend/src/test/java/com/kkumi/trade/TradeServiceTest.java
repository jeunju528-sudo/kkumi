package com.kkumi.trade;

import com.kkumi.global.error.BusinessException;
import com.kkumi.global.error.ErrorCode;
import com.kkumi.member.Member;
import com.kkumi.member.MemberRepository;
import com.kkumi.member.MemberService;
import com.kkumi.member.SeedMoney;
import com.kkumi.stock.Stock;
import com.kkumi.stock.StockPriceProvider;
import com.kkumi.stock.StockRepository;
import com.kkumi.stock.StockType;
import com.kkumi.trade.dto.BuyStockRequest;
import com.kkumi.trade.dto.SellStockRequest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

// 매수·매도 흐름 (심장 ①, ADR 011·025)
@ExtendWith(MockitoExtension.class)
class TradeServiceTest {
    // 진짜. 진짜 테스트하려는 그 객체
    @InjectMocks
    private TradeService tradeService;

    // 가짜. 테스트 대상이 기대는 부품들
    @Mock
    MemberRepository memberRepository;

    @Mock
    StockRepository stockRepository;

    @Mock
    StockHoldingRepository stockHoldingRepository;

    @Mock
    StockPriceProvider stockPriceProvider;

    @Mock
    TradeHistoryRepository tradeHistoryRepository;

	@Test
	@DisplayName("매수하면 현금 차감, 보유 증가, 이력 저장")
	void buy_success() {
		// given
        Member member = Member.join("abcd", "꿈이", SeedMoney.SMALL);
        ReflectionTestUtils.setField(member, "id", 1L);
        Stock stock = Stock.of("KK0001", "세르반반도체", StockType.ROLLER_COASTER, 51_000L);
        StockHolding holding = StockHolding.create(member, stock, 10, 10000L);
        BuyStockRequest request = new BuyStockRequest(stock.getCode(), 10);

        given(memberRepository.findByIdForUpdate(1L)).willReturn(Optional.of(member));
        given(stockRepository.findByCode("KK0001")).willReturn(Optional.of(stock));
        given(stockHoldingRepository.findByMemberAndStock(member, stock)).willReturn(Optional.of(holding));
        given(stockPriceProvider.getCurrentPrice("KK0001")).willReturn(12000L);
        tradeService.buy(member.getId(), request);

		// then
        // 남은 현금
        assertThat(member.getCash()).isEqualTo(4_880_000L);
        // 보유 수량
        assertThat(holding.getQuantity()).isEqualTo(20);
        // 평균 매입가
        assertThat(holding.getAvgPrice()).isEqualByComparingTo(new BigDecimal("11000"));
        // tradeHistoryRepository.save가 실제로 불렸는지 확인
        verify(tradeHistoryRepository).save(any());
	}

	@Test
	@DisplayName("현금이 부족하면 매수 실패")
	void buy_insufficientCash() {
		// given
        Member member = Member.join("abcd", "꿈이", SeedMoney.SMALL);
        ReflectionTestUtils.setField(member, "id", 1L);
        Stock stock = Stock.of("KK0001", "세르반반도체", StockType.ROLLER_COASTER, 51_000L);
        StockHolding holding = StockHolding.create(member, stock, 10, 10000L);
        BuyStockRequest request = new BuyStockRequest(stock.getCode(), 1000);

        given(memberRepository.findByIdForUpdate(1L)).willReturn(Optional.of(member));
        given(stockRepository.findByCode("KK0001")).willReturn(Optional.of(stock));
        given(stockHoldingRepository.findByMemberAndStock(member, stock)).willReturn(Optional.of(holding));
        given(stockPriceProvider.getCurrentPrice("KK0001")).willReturn(12000L);

		// then
        assertThatThrownBy(() -> tradeService.buy(member.getId(), request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INSUFFICIENT_CASH);

        // 실패 시 현금·보유 그대로
        assertThat(member.getCash()).isEqualTo(5_000_000L);
        assertThat(holding.getQuantity()).isEqualTo(10);
        // 이력 저장 안 됨
        verify(tradeHistoryRepository, never()).save(any());
	}

	@Test
	@DisplayName("매도하면 현금 입금, 실현손익 이력 저장")
	void sell_success() {
		// given
        Member member = Member.join("abcd", "꿈이", SeedMoney.SMALL);
        ReflectionTestUtils.setField(member, "id", 1L);
        Stock stock = Stock.of("KK0001", "세르반반도체", StockType.ROLLER_COASTER, 51_000L);
        StockHolding holding = StockHolding.create(member, stock, 10, 10000L);
        SellStockRequest request = new SellStockRequest(stock.getCode(), 5);

        given(memberRepository.findByIdForUpdate(1L)).willReturn(Optional.of(member));
        given(stockRepository.findByCode("KK0001")).willReturn(Optional.of(stock));
        given(stockHoldingRepository.findByMemberAndStock(member, stock)).willReturn(Optional.of(holding));
        given(stockPriceProvider.getCurrentPrice("KK0001")).willReturn(12000L);

        // then
        tradeService.sell(member.getId(), request);

        // then
        // 매도 후 현금
        assertThat(member.getCash()).isEqualTo(5_060_000L);
        // 보유 수량
        assertThat(holding.getQuantity()).isEqualTo(5);
        // 실현손익
        // ArgumentCaptor : 가짜(Mock) 메서드가 불릴 때 넘어온 인자를 붙잡아두는 도구
        ArgumentCaptor<TradeHistory> captor = ArgumentCaptor.forClass(TradeHistory.class);
        verify(tradeHistoryRepository).save(captor.capture());
        TradeHistory history = captor.getValue();

        assertThat(history.getRealizedProfit()).isEqualTo(10_000L);
        // 행이 남아있는지 확인
        verify(stockHoldingRepository, never()).delete(any());

	}

	@Test
	@DisplayName("전량 매도하면 보유 행 삭제")
	void sell_all_deleteHolding() {
		// given
        Member member = Member.join("abcd", "꿈이", SeedMoney.SMALL);
        ReflectionTestUtils.setField(member, "id", 1L);
        Stock stock = Stock.of("KK0001", "세르반반도체", StockType.ROLLER_COASTER, 51_000L);
        StockHolding holding = StockHolding.create(member, stock, 10, 10000L);
        SellStockRequest request = new SellStockRequest(stock.getCode(), 10);

        given(memberRepository.findByIdForUpdate(1L)).willReturn(Optional.of(member));
        given(stockRepository.findByCode("KK0001")).willReturn(Optional.of(stock));
        given(stockHoldingRepository.findByMemberAndStock(member, stock)).willReturn(Optional.of(holding));
        given(stockPriceProvider.getCurrentPrice("KK0001")).willReturn(12000L);

        // when
        tradeService.sell(member.getId(), request);

		// then
        verify(stockHoldingRepository).delete(holding);
	}

	@Test
	@DisplayName("보유하지 않은 종목을 팔면 INSUFFICIENT_HOLDING")
	void sell_noHolding() {
		// given
        Member member = Member.join("abcd", "꿈이", SeedMoney.SMALL);
        ReflectionTestUtils.setField(member, "id", 1L);
        Stock stock = Stock.of("KK0001", "세르반반도체", StockType.ROLLER_COASTER, 51_000L);
        SellStockRequest request = new SellStockRequest("KK0001", 10);

        given(memberRepository.findByIdForUpdate(1L)).willReturn(Optional.of(member));
        given(stockRepository.findByCode("KK0001")).willReturn(Optional.of(stock));
        given(stockHoldingRepository.findByMemberAndStock(member, stock)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> tradeService.sell(member.getId(), request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INSUFFICIENT_HOLDING);

        // 실패 시 현금·보유 그대로
        assertThat(member.getCash()).isEqualTo(5_000_000L);
        // 이력 저장 안 됨
        verify(tradeHistoryRepository, never()).save(any());
	}

	@Test
	@DisplayName("매수·매도 시 회원을 비관적 락으로 조회")
	void trade_lockMember() {
		// given
        Member member = Member.join("abcd", "꿈이", SeedMoney.SMALL);
        ReflectionTestUtils.setField(member, "id", 1L);
        Stock stock = Stock.of("KK0001", "세르반반도체", StockType.ROLLER_COASTER, 51_000L);
        StockHolding holding = StockHolding.create(member, stock, 10, 10000L);
        BuyStockRequest buyRequest = new BuyStockRequest(stock.getCode(), 10);
        SellStockRequest sellRequest = new SellStockRequest("KK0001", 10);

        given(memberRepository.findByIdForUpdate(1L)).willReturn(Optional.of(member));
        given(stockRepository.findByCode("KK0001")).willReturn(Optional.of(stock));
        given(stockHoldingRepository.findByMemberAndStock(member, stock)).willReturn(Optional.of(holding));
        given(stockPriceProvider.getCurrentPrice("KK0001")).willReturn(12000L);

        // when
        tradeService.buy(member.getId(), buyRequest);
        tradeService.sell(member.getId(), sellRequest);

		// then
        verify(memberRepository, times(2)).findByIdForUpdate(1L);
        verify(memberRepository, never()).findById(any());
	}
}
