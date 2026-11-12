package com.kkumi.stock.dto;

import com.kkumi.stock.StockPriceHistory;
import java.time.LocalDateTime;

// 종목 상세 차트 점 하나 (1분 간격 기록)
public record StockPriceResponse(LocalDateTime time, long price) {

	public static StockPriceResponse from(StockPriceHistory history) {
		return new StockPriceResponse(history.getRecordedAt(), history.getPrice());
	}
}
