package com.kkumi.member;

import com.kkumi.global.jwt.AuthCookie;
import com.kkumi.global.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

// 카카오 로그인 성공 직후 호출. 판단은 MemberService, 여기서는 토큰 발급·쿠키·이동만
@Slf4j
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

	private static final String SEED_PATH = "/seed";
	private static final String HOME_PATH = "/home";

	private final MemberService memberService;
	private final JwtProvider jwtProvider;
	private final AuthCookie authCookie;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		// 1. 카카오 사용자 정보에서 kakaoId, 닉네임 꺼내기
		//    - authentication.getPrincipal()은 Object 타입 → 카카오 사용자 정보를 담는 Spring 타입으로 변환
		//    - kakaoId: application.yaml의 user-name-attribute: id 설정 덕에 getName()으로 꺼낼 수 있음
		//    - 닉네임: 카카오 응답 attributes 안에 있음 (구조는 처음에 log.info로 직접 확인)
        OAuth2User user = (OAuth2User) authentication.getPrincipal();
        String kakaoId = user.getName();
        Map<String, Object> properties = user.getAttribute("properties");
        // toString() 은 값이 없을때 NPE가 나므로 (String) 캐스팅
        String nickname = (String) properties.get("nickname");
        
		// 2. memberService로 가입 여부 확인 (결과는 Optional<Long>)
        Optional<Long> result = memberService.findMemberIdByKakaoId(kakaoId);

		// 3-a. 기존 회원: 정식 토큰 → accessToken 쿠키 → HOME_PATH로 이동
		// 3-b. 신규 회원: 임시 토큰 → signupToken 쿠키 → SEED_PATH로 이동
		//    - 쿠키는 응답의 Set-Cookie 헤더로 추가, 이동은 response.sendRedirect
		//    - 이동한 뒤에는 아래 코드가 더 실행되지 않게 주의
        if(result.isPresent()){
            String token = jwtProvider.createAccessToken(result.get());
            ResponseCookie cookie = authCookie.createAccessCookie(token);
            // 쿠키 두개 이상 만들 때는 response.addCookie() 로 만들어야 함
            response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            response.sendRedirect(HOME_PATH);
        }
        else {
            String token = jwtProvider.createSignupToken(kakaoId, nickname);
            ResponseCookie cookie = authCookie.createSignupCookie(token);
            response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            response.sendRedirect(SEED_PATH);
        }

	}
}
