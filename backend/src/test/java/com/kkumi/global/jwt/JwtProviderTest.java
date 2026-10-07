package com.kkumi.global.jwt;

import com.kkumi.global.error.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtProviderTest {

    /*
        claims 준비 : 누구인지 + 만료시간 + type  -> 비밀키로 서명 -> 직렬화
        나중에 토큰 검증할 때는 : 문자열을 읽고 -> 서명이 맞는지 확인하고 -> 만료 시간이 지났는지 확인하고 -> 값을 꺼냄
        서명이 틀리면 : 위조, 시간이 지났으면 만료
    * */
	@Test
	@DisplayName("정식 토큰을 만들면 검증해서 memberId를 꺼낼 수 있다")
	void accessTokenRoundTrip() {
		// given
        // secret : 서버만 아는 비밀 도장
        // 서명 : header + payload를 secret으로 계산한 값
        // 서명을 검증할 때는 같은 secret으로 다시 계산해서 token 끝의 서명과 비교함
        String secret = "test-secret-test-secret-test-secret-1234";

        // jwt 설정값을 담는 자바 객체, application.yaml을 값을 안읽고 그냥 테스트할때만 설정해서 쓰려고 이렇게 작성
        // 첫번째 duration : 정식 토큰, 두 번째 duration : 임시 토큰 
        JwtProperties properties = new JwtProperties(secret, Duration.ofHours(1), Duration.ofMinutes(10));

        // jwtprovider : 토큰을 만들고 검수하는 곳
        JwtProvider jwtProvider = new JwtProvider(properties);

		// when
        // token : 로그인 한 사람이라는 걸 증명하는 입장권, 서버가 발급하고 브라우저가 들고다니다가 요청할 때 마다 같이 싣어서 보냄
        // token안에는 header(서명알고리즘 정보) . payload(memberid, 만료시각, type) . signature(서버가 계산해서 붙인 서명값) 들어있음
        // signature = HMAC-SHA256( secret, header + "." + payload )
        String token = jwtProvider.createAccessToken(1L);
        long memberId = jwtProvider.parseAccessToken(token);

		// then
        assertThat(memberId).isEqualTo(1L);
	}

	@Test
	@DisplayName("임시 토큰을 만들면 검증해서 kakaoId와 닉네임을 꺼낼 수 있다")
	void signupTokenRoundTrip() {
		// given
        String secret = "test-secret-test-secret-test-secret-1234";

        JwtProperties properties = new JwtProperties(secret, Duration.ofHours(1), Duration.ofMinutes(10));
        JwtProvider jwtProvider = new JwtProvider(properties);

        String token = jwtProvider.createSignupToken("abc", "꿈이");
        SignupClaims claims = jwtProvider.parseSignupToken(token);

		// when & then
        assertThat(claims.kakaoId()).isEqualTo("abc");
        assertThat(claims.nickname()).isEqualTo("꿈이");
	}

	@Test
	@DisplayName("만료된 토큰이면 검증에 실패한다")
	void expiredToken() {
		// given
        JwtProperties properties
                = new JwtProperties("test-secret-test-secret-test-secret-1234", Duration.ofSeconds(-1), Duration.ofMinutes(10));
		JwtProvider jwtProvider = new JwtProvider(properties);
        String token = jwtProvider.createAccessToken(1L);

        // when & then
        assertThatThrownBy(()->jwtProvider.parseAccessToken(token)).isInstanceOf(BusinessException.class);
	}

	@Test
	@DisplayName("서명이 다른 토큰이면 검증에 실패한다")
	void forgedToken() {
		// given
        JwtProvider issuer
                = new JwtProvider(new JwtProperties("test-secret-test-secret-test-secret-5678", Duration.ofHours(1), Duration.ofMinutes(10)));
        JwtProvider verifier
                = new JwtProvider(new JwtProperties("test-secret-test-secret-test-secret-1234", Duration.ofHours(1), Duration.ofMinutes(10)));
        String token = issuer.createAccessToken(1L);

        // when & then
        assertThatThrownBy(()->verifier.parseAccessToken(token)).isInstanceOf(BusinessException.class);
	}

	@Test
	@DisplayName("임시 토큰을 정식 토큰으로 검증하면 실패한다")
	void signupTokenAsAccessToken() {
		// given
        JwtProvider jwt
                = new JwtProvider(
                        new JwtProperties("test-secret-test-secret-test-secret-1234",
                                Duration.ofHours(1),
                                Duration.ofMinutes(10)
                                ));
        String token = jwt.createSignupToken("abc", "꿈이");

		// when & then
        assertThatThrownBy(()->jwt.parseAccessToken(token)).isInstanceOf(BusinessException.class);
	}

    @Test
    @DisplayName("정식 토큰을 임시 토큰으로 검증하면 실패한다")
    void accessTokenAsSignupToken() {
        // given
        JwtProvider jwt = new JwtProvider(
                new JwtProperties("test-secret-test-secret-test-secret-1234", Duration.ofHours(1), Duration.ofMinutes(10)));
        String token = jwt.createAccessToken(1L);

        // when & then
        assertThatThrownBy(() -> jwt.parseSignupToken(token))
                .isInstanceOf(BusinessException.class);
    }
}
