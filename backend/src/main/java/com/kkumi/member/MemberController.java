package com.kkumi.member;

import com.kkumi.global.jwt.AuthCookie;
import com.kkumi.global.jwt.JwtProvider;
import com.kkumi.global.jwt.SignupClaims;
import com.kkumi.member.dto.MemberResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;
    private final AuthCookie authCookie;

    // 로그인한 회원 정보 (쿠키 필터가 넣은 memberId)
    @GetMapping("/me")
    public MemberResponse getMyInfo(@AuthenticationPrincipal Long memberId) {
        return memberService.getMyInfo(memberId);
    }

    // 시드머니 선택 → 가입 → 정식 토큰 쿠키 발급
    @PostMapping("/seed")
    public ResponseEntity<Void> selectSeed(
            @RequestBody SeedRequest request,
            @CookieValue(AuthCookie.SIGNUP_NAME) String signupToken,
            HttpServletResponse response) {
        // 1. jwtProvider.parseSignupToken(signupToken)으로 kakaoId, 닉네임 꺼내기 (결과는 SignupClaims)
        SignupClaims claims = jwtProvider.parseSignupToken(signupToken);
        String kakaoId = claims.kakaoId();
        String nickname = claims.nickname();

        // 2. memberService.join(kakaoId, 닉네임, request.seedMoney())로 가입 → memberId
        long memberId = memberService.join(kakaoId, nickname, request.seedMoney());

        // 3. jwtProvider.createAccessToken(memberId) → authCookie.createAccessCookie(...)
        String token = jwtProvider.createAccessToken(memberId);
        ResponseCookie responseCookie = authCookie.createAccessCookie(token);
        // setHeader : 헤더에 같은 이름이 있을 때 지우고 덮어씀, 쿠키는 몇개가 있어도 헤더에서 이름은 SET-COOKIE로 동일함
        // signupCookie와 accessCookie는 쿠키명이 다른거지 헤더명은 동일한것임
        response.setHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());

        // 4. (다음 단계) signupToken 쿠키 지우기
        ResponseCookie deleteCookie = authCookie.expireSignupCookie();
        // addHeader : 기존 헤더를 두고 같은 이름의 헤더를 하나 더 추가함
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ResponseEntity.ok().build(); // 임시: 지금은 아무것도 안 하고 200만 돌려줌
    }
}
