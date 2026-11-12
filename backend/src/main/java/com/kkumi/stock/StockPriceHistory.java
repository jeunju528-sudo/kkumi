package com.kkumi.stock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 1분 간격 시세 기록 (분봉 차트용, 기록 시각은 분 단위, 같은 분은 1번만, 24시간 지나면 삭제, ADR 056)
 * 종목 코드만 저장 (조회는 코드 + 시각 범위뿐이라 연관관계 없음)
 */
@Entity
@Table(name = "stock_price_history",
	indexes = {
		@Index(name = "idx_stock_price_history_code_time", columnList = "stock_code, recorded_at"), // 차트 조회
		@Index(name = "idx_stock_price_history_time", columnList = "recorded_at") // 같은 분 확인, 오래된 기록 삭제
	})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockPriceHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "stock_code", nullable = false, length = 12)
	private String stockCode;

	@Column(nullable = false)
	private long price;

	@Column(name = "recorded_at", nullable = false)
	private LocalDateTime recordedAt;

	private StockPriceHistory(String stockCode, long price, LocalDateTime recordedAt) {
		this.stockCode = stockCode;
		this.price = price;
		this.recordedAt = recordedAt;
	}

	public static StockPriceHistory of(String stockCode, long price, LocalDateTime recordedAt) {
		return new StockPriceHistory(stockCode, price, recordedAt);
	}
}
