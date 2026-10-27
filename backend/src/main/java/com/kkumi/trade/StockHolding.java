package com.kkumi.trade;

import com.kkumi.global.error.BusinessException;
import com.kkumi.global.error.ErrorCode;
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
import java.math.RoundingMode;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자별 주식 보유 현황 (현재 상태, 수량 0이면 행 삭제)
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

    /**
     * 평균 매입가 (평균이라 소수점이 생길 수 있음)
     */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal avgPrice;

    // 정적 팩토리 메서드를 위한 private 생성자
    private StockHolding(Member member, Stock stock, int quantity, BigDecimal avgPrice) {
        this.member = member;
        this.stock = stock;
        this.quantity = quantity;
        this.avgPrice = avgPrice;
    }

    // 첫 매수 시 보유 행 생성
    public static StockHolding create(Member member, Stock stock, int quantity, long price) {
        validateQuantity(quantity);
        return new StockHolding(member, stock, quantity, BigDecimal.valueOf(price));
    }

    // 추가 매수
    public void buy(int quantity, long price) {
        validateQuantity(quantity);
        int totalQuantity = this.quantity + quantity;
        // 기존 총액 : 평균 매입가 * 수량
        BigDecimal oldTotal = this.avgPrice.multiply(BigDecimal.valueOf(this.quantity));
        // 신규 총액 : 체결가 * 수량
        BigDecimal buyTotal = BigDecimal.valueOf(price).multiply(BigDecimal.valueOf(quantity));
        this.avgPrice = oldTotal.add(buyTotal).divide(BigDecimal.valueOf(totalQuantity), 4, RoundingMode.HALF_UP);
        this.quantity = totalQuantity;
    }

    // 매도, 실현손익 반환. 보유보다 많으면 INSUFFICIENT_HOLDING
    public long sell(int quantity, long price) {
        validateQuantity(quantity);
        if(this.quantity < quantity){
            throw new BusinessException(ErrorCode.INSUFFICIENT_HOLDING);
        }
        // 실현 손익
        long realized = BigDecimal.valueOf(price)
                .subtract(this.avgPrice)
                .multiply(BigDecimal.valueOf(quantity))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        this.quantity -= quantity;

        return realized;
    }

    // 수량 1주 이상
    private static void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new BusinessException(ErrorCode.INVALID_QUANTITY);
        }
    }
}
