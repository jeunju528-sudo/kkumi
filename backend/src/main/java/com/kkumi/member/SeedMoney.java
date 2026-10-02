package com.kkumi.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 회원가입 시 선택하는 시드머니
 */
@Getter
@RequiredArgsConstructor
public enum SeedMoney {

	SMALL(5_000_000L),
	LARGE(50_000_000L);

	private final long amount;
}
