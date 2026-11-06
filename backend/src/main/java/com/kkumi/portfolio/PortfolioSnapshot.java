package com.kkumi.portfolio;

import com.kkumi.member.Member;
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
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 하루 마감 평가액 기록 (과거 시세를 저장하지 않아 다시 계산 불가 → 그날 값 저장, ADR 049)
 * 회원·날짜 UK: 같은 날 두 번 저장 방지 + 기간 조회 인덱스 겸용
 */
@Entity
@Table(name = "portfolio_snapshot",
    uniqueConstraints = @UniqueConstraint(name = "uk_snapshot_member_date", columnNames = {"member_id", "snapshot_date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    /** 기록 기준일 (KST, 이날 마감 값) */
    @Column(nullable = false)
    private LocalDate snapshotDate;

    /** 평가액 = 현금 + 주식 평가액 (원) */
    @Column(nullable = false)
    private long totalAsset;

    private PortfolioSnapshot(Member member, LocalDate snapshotDate, long totalAsset) {
        this.member = member;
        this.snapshotDate = snapshotDate;
        this.totalAsset = totalAsset;
    }

    public static PortfolioSnapshot of(Member member, LocalDate snapshotDate, long totalAsset) {
        return new PortfolioSnapshot(member, snapshotDate, totalAsset);
    }
}
