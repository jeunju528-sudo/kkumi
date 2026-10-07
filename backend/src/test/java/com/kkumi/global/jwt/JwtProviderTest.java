package com.kkumi.global.jwt;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// TODO: 본문 작성 후 @Disabled 제거
@Disabled("TODO")
class JwtProviderTest {

	@Test
	@DisplayName("정식 토큰을 만들면 검증해서 memberId를 꺼낼 수 있다")
	void accessTokenRoundTrip() {
		// given
		// when
		// then
	}

	@Test
	@DisplayName("임시 토큰을 만들면 검증해서 kakaoId와 닉네임을 꺼낼 수 있다")
	void signupTokenRoundTrip() {
		// given
		// when
		// then
	}

	@Test
	@DisplayName("만료된 토큰이면 검증에 실패한다")
	void expiredToken() {
		// given
		// when
		// then
	}

	@Test
	@DisplayName("서명이 다른 토큰이면 검증에 실패한다")
	void forgedToken() {
		// given
		// when
		// then
	}

	@Test
	@DisplayName("임시 토큰을 정식 토큰으로 검증하면 실패한다")
	void signupTokenAsAccessToken() {
		// given
		// when
		// then
	}
}
