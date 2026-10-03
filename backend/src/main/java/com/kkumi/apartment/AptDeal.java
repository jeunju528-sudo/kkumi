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

	/** 거래금액 (원/만원 단위 변환 여부 미정) */
	@Column(nullable = false)
	private long price;

	@Column(nullable = false)
	private LocalDate contractDate;

	/** 계약 해제(취소) 여부 */
	@Column(nullable = false)
	private boolean canceled;

	// TODO: 국토부 응답 → 엔티티 변환, 해제여부 갱신
}
