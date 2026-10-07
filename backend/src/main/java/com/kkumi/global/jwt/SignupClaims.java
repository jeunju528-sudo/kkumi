package com.kkumi.global.jwt;

// 임시 토큰에서 꺼낸 값 (시드 선택 전 회원)
public record SignupClaims(String kakaoId, String nickname) {
}
