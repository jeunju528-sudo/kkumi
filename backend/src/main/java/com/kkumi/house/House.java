package com.kkumi.house;

import com.kkumi.apartment.AptDeal;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 내 집 컬렉션 (같은 거래를 여러 명이 살 수 있음 → UK 없음)
 */
@Entity
@Table(name = "house")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class House {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "deal_id", nullable = false)
	private AptDeal deal;

	/** 구매가 스냅샷 (차익 계산 기준) */
	@Column(nullable = false)
	private long purchasePrice;

	@Column(nullable = false)
	private LocalDateTime purchasedAt;

	private House(Member member, AptDeal deal, long purchasePrice) {
		this.member = member;
		this.deal = deal;
		this.purchasePrice = purchasePrice;
		this.purchasedAt = LocalDateTime.now();
	}

	public static House purchase(Member member, AptDeal deal, long purchasePrice) {
		return new House(member, deal, purchasePrice);
	}
}
