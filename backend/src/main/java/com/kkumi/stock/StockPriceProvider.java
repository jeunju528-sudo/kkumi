package com.kkumi.stock;

// 현재가 조회 창구. 구현체는 서버 가상 시세 (ADR 020, 029)
public interface StockPriceProvider {

	// 종목 코드의 현재가 (원)
	long getCurrentPrice(String code);
}
