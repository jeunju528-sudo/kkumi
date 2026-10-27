package com.kkumi.trade;

import com.kkumi.member.Member;
import com.kkumi.stock.Stock;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    // 팩토리 전용 생성자, 거래 시각은 생성 시점
    private TradeHistory(Member member, Stock stock, TradeType tradeType, long price, int quantity, Long realizedProfit){
        this.member = member;
        this.stock = stock;
        this.tradeType = tradeType;
        this.price = price;
        this.quantity = quantity;
        this.realizedProfit = realizedProfit;
        this.tradedAt = LocalDateTime.now();
    }

    public static TradeHistory ofBuy(Member member, Stock stock, long price, int quantity){
        return new TradeHistory(member, stock, TradeType.BUY, price, quantity, null);
    }

    public static TradeHistory ofSell(Member member, Stock stock, long price, int quantity, long realizedProfit){
        return new TradeHistory(member, stock, TradeType.SELL, price, quantity, realizedProfit);
    }
}
