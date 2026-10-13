package com.kkumi.global.security;

import com.kkumi.global.error.BusinessException;
import com.kkumi.global.jwt.AuthCookie;
import com.kkumi.global.jwt.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

// 요청마다 accessToken 쿠키를 읽어 인증 정보를 등록하는 필터
// @Component를 붙이지 않음: 붙이면 서블릿 컨테이너에도 따로 등록되어 두 번 실행됨. SecurityConfig에서 직접 생성
@RequiredArgsConstructor
public class CookieAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        Cookie[] cookies = request.getCookies();
        // 쿠키가 하나도 없으면 getCookies()가 null을 돌려줌
        if(cookies != null){
            for(Cookie cookie : cookies){
                // 1. request.getCookies()에서 AuthCookie.ACCESS_NAME 쿠키 찾기
                if(cookie.getName().equals(AuthCookie.ACCESS_NAME)) {
                    try {
                        // 2. 찾았으면 jwtProvider.parseAccessToken(값)으로 memberId 꺼내기
                        long memberId = jwtProvider.parseAccessToken(cookie.getValue());
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(memberId, null, List.of());
                        // 3. 성공하면 SecurityContextHolder에 인증 정보 등록 (principal = memberId)
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } catch (BusinessException e) {
                        // 인증 없이 진행: 접근 제한은 SecurityConfig 규칙이 담당
                    }
                }
            }
        }

        // 4. 어떤 경우에도 마지막에는 다음 필터로 넘기기
        filterChain.doFilter(request, response);
    }
}
