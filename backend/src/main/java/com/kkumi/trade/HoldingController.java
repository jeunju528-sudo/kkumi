package com.kkumi.trade;

import com.kkumi.trade.dto.HoldingResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/holdings")
public class HoldingController {

    private final HoldingService holdingService;

    @GetMapping
    public List<HoldingResponse> findHoldings(@AuthenticationPrincipal Long memberId) {
        return holdingService.findHoldings(memberId);
    }
}
