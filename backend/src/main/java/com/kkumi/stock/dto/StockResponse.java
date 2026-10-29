package com.kkumi.stock.dto;

import com.kkumi.stock.PriceSnapshot;
import com.kkumi.stock.Stock;
import java.math.BigDecimal;
import java.math.RoundingMode;

// 종목 목록 한 줄 (changeRate 0.0123 = +1.23%, 오늘 시작가 대비)
public record StockResponse(String code, String name, long price, BigDecimal changeRate) {

	public static StockResponse of(Stock stock, PriceSnapshot snapshot) {
		return new StockResponse(stock.getCode(), stock.getName(), snapshot.currentPrice(),
			changeRate(snapshot.currentPrice(), snapshot.dayOpenPrice()));
	}

	// (현재가 - 오늘 시작가) / 오늘 시작가, 소수 4자리 반올림
	private static BigDecimal changeRate(long price, long dayOpenPrice) {
		return BigDecimal.valueOf(price - dayOpenPrice)
			.divide(BigDecimal.valueOf(dayOpenPrice), 4, RoundingMode.HALF_UP);
	}
}
