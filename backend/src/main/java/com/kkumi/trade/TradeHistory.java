package com.kkumi.trade;

import com.kkumi.member.Member;
import com.kkumi.stock.Stock;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 매수·매도 이력 (insert only, 수정 X)
 */
@Entity
@Table(name = "trade_history",
	indexes = @Index(name = "idx_trade_member_traded_at", columnList = "member_id, traded_at"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TradeHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "stock_id", nullable = false)
	private Stock stock;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 4)
	private TradeType tradeType;

	/** 체결 단가 (원) */
	@Column(nullable = false)
	private long price;

	@Column(nullable = false)
	private int quantity;

	/** 실현손익 (매도 시에만, 매수는 null) */
	private Long realizedProfit;

	@Column(nullable = false)
	private LocalDateTime tradedAt;

	// TODO: 매수용/매도용 생성 메서드
}
