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
 * 종목 마스터 (현재가는 저장 X, 시세 API로 조회)
 */
@Entity
@Table(name = "stock")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 종목코드 (예: 005930) */
	@Column(nullable = false, unique = true, length = 12)
	private String code;

	@Column(nullable = false, length = 100)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private Market market;

	private Stock(String code, String name, Market market) {
		this.code = code;
		this.name = name;
		this.market = market;
	}

	public static Stock of(String code, String name, Market market) {
		return new Stock(code, name, market);
	}
}
