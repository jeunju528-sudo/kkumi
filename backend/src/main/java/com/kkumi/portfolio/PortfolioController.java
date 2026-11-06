package com.kkumi.portfolio;

import com.kkumi.portfolio.dto.PortfolioHistoryResponse;
import com.kkumi.portfolio.dto.PortfolioResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final PortfolioSnapshotService portfolioSnapshotService;

    @GetMapping("/me")
    public PortfolioResponse getMyPortfolio(@AuthenticationPrincipal Long memberId) {
        return portfolioService.getMyPortfolio(memberId);
    }

    // 홈 손익 차트 (어제까지 일별, 오늘은 WebSocket)
    @GetMapping("/me/history")
    public List<PortfolioHistoryResponse> getMyHistory(@AuthenticationPrincipal Long memberId,
                                                       @RequestParam ChartPeriod period) {
        return portfolioSnapshotService.findHistory(memberId, period);
    }
}
