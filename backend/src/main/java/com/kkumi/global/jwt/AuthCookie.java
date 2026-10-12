package com.kkumi.global.jwt;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

// 인증 쿠키 이름·Path 상수와 생성. 필터(global)와 핸들러(member)가 같은 값을 쓰려고 global에 둠
// JWT는 로그인을 했다는 인증서이고 cookie는 그 인증서를 들고 다닐 수 있도록 하기 위해 만들어두는 것 
// 서버에서 cookie를 만들어서 브라우저로 보내주면 브라우저에서는 저장해두고 서버에 요청할 때 마다 같이 보내줌
@Component
@RequiredArgsConstructor
public class AuthCookie {

	public static final String ACCESS_NAME = "accessToken";
	public static final String SIGNUP_NAME = "signupToken";
	public static final String ACCESS_PATH = "/";
	public static final String SIGNUP_PATH = "/api/members/seed";

	private final AuthCookieProperties cookieProperties;
	private final JwtProperties jwtProperties;

	// 정식 토큰 쿠키: HttpOnly, SameSite=Lax, Path=/, Max-Age는 정식 토큰 만료와 동일
	public ResponseCookie createAccessCookie(String token) {
		return build(ACCESS_NAME, token, ACCESS_PATH, jwtProperties.accessExpiration());
	}

	// 임시 토큰 쿠키: HttpOnly, SameSite=Lax, Path=/api/members/seed, Max-Age는 임시 토큰 만료와 동일
	public ResponseCookie createSignupCookie(String token) {
		return build(SIGNUP_NAME, token, SIGNUP_PATH, jwtProperties.signupExpiration());
	}

	private ResponseCookie build(String name, String token, String path, Duration maxAge) {
		return ResponseCookie.from(name, token)
			.httpOnly(true)
			.secure(cookieProperties.secure())
			.sameSite("Lax")
			.path(path)
			.maxAge(maxAge)
			.build();
	}
}
