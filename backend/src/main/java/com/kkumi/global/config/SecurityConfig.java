package com.kkumi.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 임시 보안 설정 (개발·배포 테스트용으로 전체 허용)
 * TODO: 카카오 로그인 붙이면 인증 필요한 경로 막기
 */
@Configuration
public class SecurityConfig {
    /*
      .oauth2Login(Customizer.withDefaults()) 를 security chain에 추가하면 2가지 url을 처리해 줌
      1. /oauth2/authorization/kakao: state를 만들어서 카카오 인가 화면으로 보내줌
      2. /login/oauth2/code/kakao: 카카오가 코드를 돌려주는 곳, 카카오 디벨로퍼에 등록한 redirect URI 주소
                                   여기서 토큰 교환과 사용자 정보 조회까지 가능
    * */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.csrf(AbstractHttpConfigurer::disable)
            .oauth2Login(Customizer.withDefaults())
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/actuator/health/**").permitAll()
				.anyRequest().permitAll());
		return http.build();
	}
}
