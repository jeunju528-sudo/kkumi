package com.kkumi.apartment.dto;

import com.kkumi.apartment.Affordability;
import java.math.BigDecimal;
import java.time.LocalDate;

// 집 카드 한 장 (area ㎡, ownAmount 집값 40%, loanAmount 집값 60%)
public record AptRecommendResponse(long dealId, String complexName, String region, BigDecimal area, int floor,
    long price, LocalDate contractDate, long ownAmount, long loanAmount, Affordability affordability, long shortage) {
}
