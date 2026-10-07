package com.kkumi.global.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 토큰 생성·검증만. 도메인 객체는 받지 않고 기본 타입만 다룸
@Component
@RequiredArgsConstructor
public class JwtProvider {

	private final JwtProperties jwtProperties;

	// 정식 토큰: memberId
	public String createAccessToken(long memberId) {
		throw new UnsupportedOperationException("TODO");
	}

	// 임시 토큰: kakaoId, nickname (시드 선택 전)
	public String createSignupToken(String kakaoId, String nickname) {
		throw new UnsupportedOperationException("TODO");
	}

	// 검증 실패 시 BusinessException(INVALID_TOKEN)
	public long parseAccessToken(String token) {
		throw new UnsupportedOperationException("TODO");
	}

	public SignupClaims parseSignupToken(String token) {
		throw new UnsupportedOperationException("TODO");
	}
}
