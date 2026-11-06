package com.kkumi.house.dto;

import jakarta.validation.constraints.NotNull;

// 집 사기: 고른 거래 id
public record BuyHouseRequest(@NotNull Long dealId) {
}
