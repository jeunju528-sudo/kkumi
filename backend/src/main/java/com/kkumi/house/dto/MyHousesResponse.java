package com.kkumi.house.dto;

import com.kkumi.house.Tier;
import java.util.List;

// 내 마을: 산 집 목록 + 집 수 + 변신 단계
public record MyHousesResponse(List<HouseResponse> houses, int houseCount, Tier tier) {
}
