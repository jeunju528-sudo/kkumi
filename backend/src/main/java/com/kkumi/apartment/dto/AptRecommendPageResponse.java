package com.kkumi.apartment.dto;

import java.util.List;
import org.springframework.data.domain.Slice;

// 집 카드 한 묶음 + 평가액으로 살 수 있는 최대 집값 (화면 "약 N까지")
public record AptRecommendPageResponse(List<AptRecommendResponse> items, int page, boolean hasNext, long maxPrice) {

    public static AptRecommendPageResponse of(Slice<AptRecommendResponse> slice, long maxPrice) {
        return new AptRecommendPageResponse(slice.getContent(), slice.getNumber(), slice.hasNext(), maxPrice);
    }
}
