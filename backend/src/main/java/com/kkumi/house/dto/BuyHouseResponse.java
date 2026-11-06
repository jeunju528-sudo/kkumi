package com.kkumi.house.dto;

import com.kkumi.house.Tier;

// 집 사기 결과 (isTierUp: 이번 구매로 변신 단계가 올라갔는지)
public record BuyHouseResponse(Long houseId, Tier tier, boolean isTierUp) {
}
