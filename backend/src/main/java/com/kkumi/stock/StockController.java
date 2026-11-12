package com.kkumi.stock;

import com.kkumi.stock.dto.StockCandleResponse;
import com.kkumi.stock.dto.StockPriceResponse;
import com.kkumi.stock.dto.StockResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stocks")
public class StockController {

	private final StockService stockService;

	@GetMapping
	public List<StockResponse> findStocks() {
		return stockService.findStocks();
	}

	// 분봉 차트: 최근 24시간 1분 기록
	@GetMapping("/{code}/prices")
	public List<StockPriceResponse> findRecentPrices(@PathVariable String code) {
		return stockService.findRecentPrices(code);
	}

	// 일봉 차트: 최근 30일 시가·고가·저가·종가
	@GetMapping("/{code}/candles")
	public List<StockCandleResponse> findDailyCandles(@PathVariable String code) {
		return stockService.findDailyCandles(code);
	}
}
