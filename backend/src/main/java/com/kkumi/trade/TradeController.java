package com.kkumi.trade;

import com.kkumi.trade.dto.BuyStockRequest;
import com.kkumi.trade.dto.SellStockRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {
    private final TradeService tradeService;

    @PostMapping("/buy")
    public void buy(@Valid @RequestBody BuyStockRequest request, @AuthenticationPrincipal Long memberId){
        tradeService.buy(memberId, request);
    }

    @PostMapping("/sell")
    public void sell(@Valid @RequestBody SellStockRequest request, @AuthenticationPrincipal Long memberId){
        tradeService.sell(memberId, request);
    }
}
