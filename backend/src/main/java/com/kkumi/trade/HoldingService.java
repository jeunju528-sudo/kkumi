package com.kkumi.trade;

import com.kkumi.stock.StockPriceProvider;
import com.kkumi.trade.dto.HoldingResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HoldingService {

    private final StockHoldingRepository stockHoldingRepository;
    private final StockPriceProvider stockPriceProvider;

    // 내 보유 종목 + 메모리 현재가
    public List<HoldingResponse> findHoldings(Long memberId) {
        return stockHoldingRepository.findAllByMemberIdWithStock(memberId).stream()
            .map(h -> HoldingResponse.of(h, stockPriceProvider.getCurrentPrice(h.getStock().getCode())))
            .toList();
    }
}
