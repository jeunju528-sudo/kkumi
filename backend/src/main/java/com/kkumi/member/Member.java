package com.kkumi.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 카카오 소셜로그인 키 */
	@Column(nullable = false, unique = true, length = 64)
	private String kakaoId;

	@Column(nullable = false, length = 30)
	private String nickname;

	/** 초기 자금 (500만 / 5,000만 원) */
	@Column(nullable = false)
	private long initialCash;

	/** 현재 현금 잔고 (원) */
	@Column(nullable = false)
	private long cash;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	private Member(String kakaoId, String nickname, SeedMoney seedMoney) {
		this.kakaoId = kakaoId;
		this.nickname = nickname;
		this.initialCash = seedMoney.getAmount();
		this.cash = seedMoney.getAmount();
		this.createdAt = LocalDateTime.now();
	}

	public static Member join(String kakaoId, String nickname, SeedMoney seedMoney) {
		return new Member(kakaoId, nickname, seedMoney);
	}

	// TODO: 매수·매도 시 잔고 변경
}
