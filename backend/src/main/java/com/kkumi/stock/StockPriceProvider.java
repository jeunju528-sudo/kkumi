package com.kkumi.stock;

// 현재가 조회 창구. 구현체는 서버 가상 시세 (ADR 020, 029)
public interface StockPriceProvider {

	// 종목 코드의 현재가 (원)
	long getCurrentPrice(String code);

	// 현재가와 오늘 시작가를 같은 시점 값으로 (등락률 계산용)
	PriceSnapshot getSnapshot(String code);
}
