package com.kkumi.stock;

import com.kkumi.stock.dto.StockResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
}
