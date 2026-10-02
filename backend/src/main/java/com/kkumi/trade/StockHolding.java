package com.kkumi.trade;

import com.kkumi.member.Member;
import com.kkumi.stock.Stock;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주식 보유 현황 (현재 상태, 수량 0이면 행 삭제)
 */
@Entity
@Table(name = "stock_holding",
	uniqueConstraints = @UniqueConstraint(name = "uk_holding_member_stock", columnNames = {"member_id", "stock_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockHolding {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "stock_id", nullable = false)
	private Stock stock;

	@Column(nullable = false)
	private int quantity;

	/** 평균 매입가 (평균이라 소수점이 생길 수 있음) */
	@Column(nullable = false, precision = 19, scale = 4)
	private BigDecimal avgPrice;

	// TODO: 매수·매도 시 수량, 평균 매입가 변경
}
