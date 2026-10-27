package com.kkumi.stock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 종목 마스터 (가상 종목 20개, ADR 041). 현재가는 시세 엔진이 관리 (ADR 040)
 */
@Entity
@Table(name = "stock")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 종목코드 (예: KK0001) */
	@Column(nullable = false, unique = true, length = 12)
	private String code;

	@Column(nullable = false, length = 100)
	private String name;

	/** 변동 유형 (ADR 029, 041) */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private StockType type;

	/** 시작가 = 평균 회귀 기준가 (원) */
	@Column(nullable = false)
	private long basePrice;

	private Stock(String code, String name, StockType type, long basePrice) {
		this.code = code;
		this.name = name;
		this.type = type;
		this.basePrice = basePrice;
	}

	public static Stock of(String code, String name, StockType type, long basePrice) {
		return new Stock(code, name, type, basePrice);
	}
}
