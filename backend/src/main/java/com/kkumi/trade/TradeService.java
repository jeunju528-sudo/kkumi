package com.kkumi.trade;

import com.kkumi.global.error.BusinessException;
import com.kkumi.global.error.ErrorCode;
import com.kkumi.member.Member;
import com.kkumi.member.MemberRepository;
import com.kkumi.stock.Stock;
import com.kkumi.stock.StockPriceProvider;
import com.kkumi.stock.StockRepository;
import com.kkumi.trade.dto.BuyStockRequest;
import com.kkumi.trade.dto.SellStockRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TradeService {

    private final MemberRepository memberRepository;
    private final StockRepository stockRepository;
    private final StockHoldingRepository stockHoldingRepository;
    private final StockPriceProvider stockPriceProvider;
    private final TradeHistoryRepository tradeHistoryRepository;

    @Transactional
    public void buy(Long memberId, BuyStockRequest request) {
        // 0. 회원 조회 + 비관적 락 (동시 요청은 여기서 대기, 락은 항상 회원 먼저)
        // orElseThrow는 예외를 만드는 방법(Supplier)을 받음. 값이 없을 때만 람다를 실행해 예외 생성
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        // 1. 종목 엔티티도 가져오기
        Stock stock = stockRepository.findByCode(request.code())
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_NOT_FOUND));
        // 2. 회원 엔티티랑 종목 엔티티로 이미 구매한 주식인지 확인
        Optional<StockHolding> stockHolding = stockHoldingRepository.findByMemberAndStock(member, stock);
        // 3. 현재가 조회 (StockPriceProvider), 체결 단가
        long price = stockPriceProvider.getCurrentPrice(request.code());
        // 4. 금액 계산 (현재가 × 수량, 범위 넘으면 예외)
        long amount = Math.multiplyExact(price, request.quantity());
        // 5. 현금에서 차감 (부족하면 INSUFFICIENT_CASH)
        member.withdraw(amount);

        stockHolding.ifPresentOrElse(
                // 6. 이미 구매했으면 평균값 새로 구하고
                holding -> holding.buy(request.quantity(), price),
                // 7. 구매 안했으면 보유 행 새로 만들어서 저장
                () -> stockHoldingRepository.save(StockHolding.create(member, stock, request.quantity(), price))
        );

        // 8. 구매 히스토리 저장
        TradeHistory tradeHistory = TradeHistory.ofBuy(member, stock, price, request.quantity());
        tradeHistoryRepository.save(tradeHistory);
    }

    @Transactional
    public void sell(Long memberId, SellStockRequest request) {
        // 0. 회원 조회 + 비관적 락 (연타 시 중복 매도 방지)
        Member member = memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(()->new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        // 1. 주식 종목 정보 가지고 옴
        Stock stock = stockRepository.findByCode(request.code())
                .orElseThrow(()->new BusinessException(ErrorCode.STOCK_NOT_FOUND));
        // 2. 회원이 주식 보유한 정보 가지고 옴
        StockHolding stockHolding = stockHoldingRepository.findByMemberAndStock(member, stock)
                .orElseThrow(()->new BusinessException(ErrorCode.INSUFFICIENT_HOLDING));
        // 3. 현재가 가지고 옴
        long price = stockPriceProvider.getCurrentPrice(request.code());
        // 4. 갖고있는 주식에서 주식 차감하고 실현손익 받음
        long realizedProfit = stockHolding.sell(request.quantity(), price);
        // 5. 판 금액 구해서 현금에 더해주고 = 현재금액 * 수량 = 실현손익 포함된 금액
        long sellAmount = Math.multiplyExact(request.quantity(), price);
        member.deposit(sellAmount);
        // 6. 수량 0이면 행 삭제
        if(stockHolding.getQuantity() == 0){
            stockHoldingRepository.delete(stockHolding);
        }
        // 7. 매도 히스토리 저장
        TradeHistory tradeHistory = TradeHistory.ofSell(member, stock, price, request.quantity(), realizedProfit);
        tradeHistoryRepository.save(tradeHistory);
    }
}
