package com.kkumi.apartment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 실거래 기록 (배치 원본)
 * - HOUSE가 PK 참조 → 배치는 증분(upsert)으로만 적재
 * - 해제여부(canceled)는 나중에 바뀔 수 있어서 UK에서 제외
 */
@Entity
@Table(name = "apt_deal",
	uniqueConstraints = @UniqueConstraint(name = "uk_deal",
		columnNames = {"complex_id", "area", "floor", "price", "contract_date"}),
	indexes = @Index(name = "idx_deal_contract_date", columnList = "contract_date"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AptDeal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "complex_id", nullable = false)
	private AptComplex complex;

	/** 전용면적 (㎡) */
	@Column(nullable = false, precision = 7, scale = 2)
	private BigDecimal area;

	@Column(nullable = false)
	private int floor;

	/** 거래금액 (원). 국토부 응답은 만원 단위라 배치에서 × 10,000 (ADR 019, 047) */
	@Column(nullable = false)
	private long price;

	@Column(nullable = false)
	private LocalDate contractDate;

	/** 계약 해제(취소) 여부 */
	@Column(nullable = false)
	private boolean canceled;

	/** 시세 기간 (ADR 015): 최근 6개월 거래만 추천·구매 대상 */
	public static final int MARKET_PRICE_MONTHS = 6;

	private AptDeal(AptComplex complex, BigDecimal area, int floor, long price, LocalDate contractDate) {
		this.complex = complex;
		this.area = area;
		this.floor = floor;
		this.price = price;
		this.contractDate = contractDate;
		this.canceled = false;
	}

	public static AptDeal of(AptComplex complex, BigDecimal area, int floor, long price, LocalDate contractDate) {
		return new AptDeal(complex, area, floor, price, contractDate);
	}

	// 시세 기간 시작일 (오늘 − 6개월, 이 날짜 계약까지 포함)
	public static LocalDate marketPriceSince(LocalDate today) {
		return today.minusMonths(MARKET_PRICE_MONTHS);
	}

	// 살 수 있는 거래: 해제 안 됨 + 시세 기간 안
	public boolean isAvailable(LocalDate today) {
		return !canceled && !contractDate.isBefore(marketPriceSince(today));
	}

	// 해제 여부 갱신, 바뀌었으면 true
    public boolean updateCanceled(boolean canceled){
        if (this.canceled == canceled){
            return false;
        }
        this.canceled = canceled;
        return true;
    }
}
