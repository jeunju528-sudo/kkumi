package com.kkumi.notification;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 보유 종목 하락 알림 (ADR 059). 문구는 저장 안 함 → 화면이 숫자로 만듦
 * 유니크 키 (회원, 종목, 날짜): 종목당 하루 1번 + 중복 메시지 마지막 방어선
 */
@Entity
@Table(name = "notification",
        uniqueConstraints = @UniqueConstraint(name = "uk_notification_member_stock_date",
                columnNames = {"member_id", "stock_id", "alert_date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {

    private static final int RATE_SCALE = 4;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    /** 판정 시점 현재가 */
    @Column(nullable = false)
    private long price;

    /** 판정 시점 평균 매입가 스냅샷 (등락률 계산 근거) */
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal avgPrice;

    /** 알림 날짜 (KST) */
    @Column(nullable = false)
    private LocalDate alertDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** 읽은 시각, null이면 안 읽음 */
    private LocalDateTime readAt;

    private Notification(Member member, Stock stock, long price, BigDecimal avgPrice, LocalDate alertDate) {
        this.member = member;
        this.stock = stock;
        this.price = price;
        this.avgPrice = avgPrice;
        this.alertDate = alertDate;
        this.createdAt = LocalDateTime.now();
    }

    public static Notification ofPriceDrop(Member member, Stock stock, long price, BigDecimal avgPrice, LocalDate alertDate) {
        return new Notification(member, stock, price, avgPrice, alertDate);
    }

    public boolean isRead() {
        return readAt != null;
    }

    // 평단 대비 등락률, 소수 4자리 HALF_UP (PortfolioCalculator.toRate와 같은 규칙)
    public BigDecimal calculateDropRate() {
        return BigDecimal.valueOf(price).subtract(avgPrice)
                .divide(avgPrice, RATE_SCALE, RoundingMode.HALF_UP);
    }
}
