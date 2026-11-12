package com.kkumi.apartment;

import com.kkumi.apartment.dto.AptRecommendPageResponse;
import com.kkumi.apartment.dto.AptRecommendResponse;
import com.kkumi.global.error.BusinessException;
import com.kkumi.global.error.ErrorCode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/apartments")
public class AptRecommendController {

    private static final int PAGE_SIZE = 20;

    private final AptRecommendService aptRecommendService;

    // q: 단지명·지역명·동 검색, affordableOnly=false면 못 사는 집 포함
    @GetMapping
    public AptRecommendPageResponse findRecommendations(@AuthenticationPrincipal Long memberId,
                                                        @RequestParam(required = false) @Size(max = 50) String q,
                                                        @RequestParam(name = "affordableOnly", defaultValue = "true") boolean isAffordableOnly,
                                                        @RequestParam(defaultValue = "0") @Min(0) int page) {
        return aptRecommendService.findRecommendations(memberId, q, isAffordableOnly, page, PAGE_SIZE);
    }

    // 홈 목표 패널: 못 사는 집 중 가장 싼 집, 없으면 404 (/{dealId}보다 고정 경로 우선)
    @GetMapping("/goal")
    public AptRecommendResponse getGoal(@AuthenticationPrincipal Long memberId) {
        return aptRecommendService.findGoal(memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.GOAL_NOT_FOUND));
    }

    // 계약서 화면: 거래 1건 + 카드 상태
    @GetMapping("/{dealId}")
    public AptRecommendResponse getDeal(@AuthenticationPrincipal Long memberId, @PathVariable Long dealId) {
        return aptRecommendService.getDeal(memberId, dealId);
    }
}
