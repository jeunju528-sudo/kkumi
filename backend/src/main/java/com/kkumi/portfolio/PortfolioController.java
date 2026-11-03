package com.kkumi.portfolio;

import com.kkumi.portfolio.dto.PortfolioResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portfolios")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping("/me")
    public PortfolioResponse getMyPortfolio(@AuthenticationPrincipal Long memberId) {
        return portfolioService.getMyPortfolio(memberId);
    }
}
