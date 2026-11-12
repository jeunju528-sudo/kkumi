package com.kkumi.stock;

import com.kkumi.global.error.BusinessException;
import com.kkumi.global.error.ErrorCode;
import com.kkumi.stock.dto.StockCandleResponse;
import com.kkumi.stock.dto.StockPriceResponse;
import com.kkumi.stock.dto.StockResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockService {

	private static final ZoneId KST = ZoneId.of("Asia/Seoul");
	// 분봉: 최근 24시간 1분 기록 전부 / 일봉: 오늘 포함 최근 30일 (ADR 056)
	private static final long MINUTE_CHART_HOURS = 24;
	private static final long DAILY_CHART_DAYS = 30;
	private final StockRepository stockRepository;
	private final StockPriceProvider stockPriceProvider;
	private final StockPriceHistoryRepository historyRepository;
	private final StockDailyCandleRepository candleRepository;

	// 종목 목록 + 메모리 시세 (코드순)
	public List<StockResponse> findStocks() {
		return stockRepository.findAll(Sort.by("code")).stream()
			.map(stock -> StockResponse.of(stock, stockPriceProvider.getSnapshot(stock.getCode())))
			.toList();
	}

	// 분봉 차트: 최근 24시간 1분 기록, 오래된 순 (없는 종목이면 404)
	public List<StockPriceResponse> findRecentPrices(String code) {
		validateStock(code);
		LocalDateTime since = LocalDateTime.now(KST).minusHours(MINUTE_CHART_HOURS);
		return historyRepository.findRecent(code, since).stream()
			.map(StockPriceResponse::from)
			.toList();
	}

	// 일봉 차트: 오늘 포함 최근 30일, 오래된 순 (없는 종목이면 404)
	public List<StockCandleResponse> findDailyCandles(String code) {
		validateStock(code);
		LocalDate since = LocalDate.now(KST).minusDays(DAILY_CHART_DAYS - 1);
		return candleRepository.findRecent(code, since).stream()
			.map(StockCandleResponse::from)
			.toList();
	}

	private void validateStock(String code) {
		if (stockRepository.findByCode(code).isEmpty()) {
			throw new BusinessException(ErrorCode.STOCK_NOT_FOUND);
		}
	}
}
