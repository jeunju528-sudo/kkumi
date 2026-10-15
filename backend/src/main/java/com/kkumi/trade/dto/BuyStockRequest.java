package com.kkumi.trade.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

// 매수 요청 본문: {"code": "...", "quantity": 1}
public record BuyStockRequest(
	@NotBlank String code,
	// 1주 이상 (ADR 037)
	@Min(1) int quantity
) {
}
