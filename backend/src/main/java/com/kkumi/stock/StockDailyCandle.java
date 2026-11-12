package com.kkumi.stock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 종목 일봉 (시가·고가·저가·종가, 종목 + 날짜마다 1행, 삭제 없음, ADR 056)
 * 1분 시세 저장 때마다 오늘 일봉을 갱신 (배치 없이 증분 upsert)
 */
@Entity
@Table(name = "stock_daily_candle",
	uniqueConstraints = @UniqueConstraint(name = "uk_stock_daily_candle_code_date",
		columnNames = {"stock_code", "trade_date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockDailyCandle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "stock_code", nullable = false, length = 12)
	private String stockCode;

	@Column(name = "trade_date", nullable = false)
	private LocalDate tradeDate;

	/** 시가 = 오늘 시작가 (등락률 기준과 같은 값) */
	@Column(nullable = false)
	private long openPrice;

	@Column(nullable = false)
	private long highPrice;

	@Column(nullable = false)
	private long lowPrice;

	/** 종가 = 마지막 1분 기록 가격 (오늘 일봉은 계속 바뀜) */
	@Column(nullable = false)
	private long closePrice;

	private StockDailyCandle(String stockCode, LocalDate tradeDate, long openPrice, long price) {
		this.stockCode = stockCode;
		this.tradeDate = tradeDate;
		this.openPrice = openPrice;
		this.highPrice = Math.max(openPrice, price);
		this.lowPrice = Math.min(openPrice, price);
		this.closePrice = price;
	}

	// 그날 첫 기록: 시가는 오늘 시작가, 고가·저가는 시가와 지금 가격 중
	public static StockDailyCandle open(String stockCode, LocalDate tradeDate, long openPrice, long price) {
		return new StockDailyCandle(stockCode, tradeDate, openPrice, price);
	}

	// 1분 기록 반영: 고가·저가 넓히고 종가 교체
	public void update(long price) {
		this.highPrice = Math.max(this.highPrice, price);
		this.lowPrice = Math.min(this.lowPrice, price);
		this.closePrice = price;
	}
}
