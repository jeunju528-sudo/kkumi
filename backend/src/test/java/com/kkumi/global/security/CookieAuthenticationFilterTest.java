package com.kkumi.global.security;

import com.kkumi.global.jwt.AuthCookie;
import com.kkumi.global.jwt.JwtProperties;
import com.kkumi.global.jwt.JwtProvider;
import jakarta.servlet.http.Cookie;

import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

// 서버를 띄우지 않고 가짜 요청·응답으로 필터만 검증
class CookieAuthenticationFilterTest {

    private final JwtProvider jwtProvider = new JwtProvider(
            new JwtProperties("test-secret-test-secret-test-secret-1234", Duration.ofHours(1), Duration.ofMinutes(10)));
    private final CookieAuthenticationFilter filter = new CookieAuthenticationFilter(jwtProvider);

    // 인증 정보가 다음 테스트로 새지 않게 비움
    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 accessToken 쿠키면 memberId로 인증된다")
    void validAccessCookie() throws Exception {
        // given
        String token = jwtProvider.createAccessToken(7L);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(AuthCookie.ACCESS_NAME, token));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // when
        filter.doFilter(request, response, chain);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // then
        assertThat(authentication.getPrincipal()).isEqualTo(7L);
    }

    @Test
    @DisplayName("쿠키가 없으면 인증 없이 다음 필터로 넘어간다")
    void noCookie() throws Exception {
        // given: 쿠키를 설정하지 않은 요청
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // when
        filter.doFilter(request, response, chain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("잘못된 토큰이면 인증 없이 다음 필터로 넘어간다")
    void invalidToken() throws Exception {
        // given
        String token = jwtProvider.createAccessToken(7L);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(AuthCookie.ACCESS_NAME, "1234asdf"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // when
        filter.doFilter(request, response, chain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("만료된 토큰이면 인증 없이 다음 필터로 넘어간다")
    void expiredToken() throws Exception {
        // given
        JwtProvider jwt = new JwtProvider(
                new JwtProperties("test-secret-test-secret-test-secret-1234",
                        Duration.ofHours(-1),
                        Duration.ofMinutes(10)));
        String token = jwt.createAccessToken(7L);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(AuthCookie.ACCESS_NAME, token));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // when
        filter.doFilter(request, response, chain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("signupToken 쿠키만 있으면 인증되지 않는다")
    void signupCookieOnly() throws Exception{
        // given
        String token = jwtProvider.createSignupToken("abc", "꿈이");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(AuthCookie.SIGNUP_NAME, token));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // when
        filter.doFilter(request, response, chain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

    }

    @Test
    @DisplayName("어떤 경우에도 다음 필터가 호출된다")
    void alwaysContinuesChain() throws Exception {
        // TODO: MockFilterChain.getRequest()가 null이 아니면 다음 필터로 넘어간 것
        // given
        String token = jwtProvider.createSignupToken("abc", "꿈이");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(AuthCookie.SIGNUP_NAME, token));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // when
        filter.doFilter(request, response, chain);

        // then
        // 무조건 chain이 넘어가면 그 요청을 기억해두므로 chain은 null 이 아니어야 정상임
        assertThat(chain.getRequest()).isNotNull();
    }
}
