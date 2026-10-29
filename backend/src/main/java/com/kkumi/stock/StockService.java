package com.kkumi.stock;

import com.kkumi.stock.dto.StockResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

	private final StockRepository stockRepository;
	private final StockPriceProvider stockPriceProvider;

	// 종목 목록 + 메모리 시세 (코드순)
	public List<StockResponse> findStocks() {
		return stockRepository.findAll(Sort.by("code")).stream()
			.map(stock -> StockResponse.of(stock, stockPriceProvider.getSnapshot(stock.getCode())))
			.toList();
	}
}
