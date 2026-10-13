package com.kkumi.global.config;

import com.kkumi.global.jwt.JwtProvider;
import com.kkumi.global.security.CookieAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    /*
      .oauth2Login(Customizer.withDefaults()) 를 켜면 security chain에 추가하면 2가지 url을 만들어 줌(고정된 패턴)
      1. /oauth2/authorization/{registrationId}: state를 만들어서 카카오 로그인 화면으로 보내줌
      2. /login/oauth2/code/{registrationId}: 카카오가 코드를 돌려주는 곳, 카카오 디벨로퍼에 등록한 redirect URI 주소
                                   여기서 토큰 교환과 사용자 정보 조회까지 가능
      * {registrationId} : yaml의 spring: security: oauth2: client: registration: kakao: 에서 kakao가 id로 설정됨

      아래 다섯가지 일도 처리해줌
       1. 로그인 시작 주소를 받는 컨트롤러 만들기
       2. 카카오 로그인 화면 주소를 조립해서 보내기(client_id, redirect_uri, state 붙이기)
       3. 카카오가 돌아올 주소를 받는 컨트롤러 만들기, state가 맞는지 검증하기
       4. 카카오 서버에 HTTP 요청 보내서 인가 코드를 액세스 토큰으로 교환하기
       5. 그 토큰으로 사용자 정보 요청 보내고 JSON 응답 해석하기
    * */
    /*
     * 로그인 프로세스
     * 사용자 로그인 -> 핸들러가 JWT 만듦 -> 브라우저에 set-cookie
     * -> 브라우저가 요청마다 cookie 같이 보내줌 -> 서버에서 jwt꺼내서 인증
     * */

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationSuccessHandler loginSuccessHandler,
                                                   JwtProvider jwtProvider)
            throws Exception {
        // oauth2를 켜두면 인증 안 된 요청이 막혔을 때 Spring의 기본 동작은 로그인 페이지로 302 리다이렉트됨
        // 프론트에서 401(누군지 모름) 을 받으면 로그인 페이지로 분기처리를 해주기 위해 401로 에러코드 보냄
        http
			.csrf(AbstractHttpConfigurer::disable)
            .oauth2Login(oauth -> oauth.successHandler(loginSuccessHandler))
            .addFilterBefore(new CookieAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/login/oauth2/**").permitAll()
                .requestMatchers("/api/members/seed").permitAll()
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/error").permitAll()
				.anyRequest().authenticated())
            // AuthenticationEntryPoint: 인증 안 된 요청이 막혔을 때 "어떻게 응답할지" 정하는 곳
            .exceptionHandling(e -> e
                    .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        return http.build();
    }
}

/*
 * 로그인 요청 주소 : http://localhost:8080/oauth2/authorization/kakao
 * 카카오 주소 :  https://kauth.kakao.com/oauth/authorize?
                response_type=code : 인가 코드 방식으로 진행하겠다는 선언
                &client_id= : 앱 client id
                &scope=profile_nickname : yaml의 scope, 닉네임 보내달라는 것을 요청
                &state= : spring이 만드는 임의의 문자열, 돌아오는 응답값이 이 state와 같아야 내가 시작한 로그인이라고 확인
                &redirect_uri=http://localhost:8080/login/oauth2/code/kakao yaml에서 redirect url
                &code_challenge= : 스프링이 자동으로 붙이는 PKCE 문자열
                &code_challenge_method= : 스프링이 자동으로 붙이는 PKCE 문자열
 */

/*
* /error 도 permitAll 로 열어둔 이유
* 1. POST /api/members/seed (쿠키 없음)
* 2. Controller의 @CookieValue가 쿠키를 못 찾음 → 400 에러로 결정
* 3. Tomcat이 에러 화면을 만들려고 /error로 내부 forward
* 4. forward도 Security 필터 체인을 다시 통과함
* 5. /error가 permitAll이 아니면 → 인증 없음 → EntryPoint → 401
   → 원래 원인(400, 쿠키 없음)이 401에 덮여서 사라짐
*
* */