package com.kkumi.stock.dto;

import com.kkumi.stock.StockDailyCandle;
import java.time.LocalDate;

// 종목 상세 일봉 하나
public record StockCandleResponse(LocalDate date, long open, long high, long low, long close) {

	public static StockCandleResponse from(StockDailyCandle candle) {
		return new StockCandleResponse(candle.getTradeDate(), candle.getOpenPrice(), candle.getHighPrice(),
			candle.getLowPrice(), candle.getClosePrice());
	}
}
