package com.kkumi.trade;

import com.kkumi.trade.dto.StockProfitResponse;
import com.kkumi.trade.dto.TradeHistoryPageResponse;
import com.kkumi.trade.dto.TradeHistorySummaryResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trades/history")
public class TradeHistoryController {

    private static final int PAGE_SIZE = 20;

    private final TradeHistoryService tradeHistoryService;

    @GetMapping
    public TradeHistoryPageResponse findHistories(@AuthenticationPrincipal Long memberId,
                                                  @RequestParam(defaultValue = "WEEK") Period period,
                                                  @RequestParam(required = false) TradeType type,
                                                  @RequestParam(defaultValue = "0") @Min(0) int page) {
        return tradeHistoryService.findHistories(memberId, period, type, page, PAGE_SIZE);
    }

    @GetMapping("/summary")
    public TradeHistorySummaryResponse findSummary(@AuthenticationPrincipal Long memberId,
                                                   @RequestParam(defaultValue = "WEEK") Period period) {
        return tradeHistoryService.findSummary(memberId, period);
    }

    @GetMapping("/by-stock")
    public List<StockProfitResponse> findProfitByStock(@AuthenticationPrincipal Long memberId,
                                                       @RequestParam(defaultValue = "WEEK") Period period) {
        return tradeHistoryService.findProfitByStock(memberId, period);
    }
}
