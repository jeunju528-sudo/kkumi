package com.kkumi.global.jwt;

import com.kkumi.global.error.BusinessException;
import com.kkumi.global.error.ErrorCode;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;

// 토큰 생성·검증만. 도메인 객체는 받지 않고 기본 타입만 다룸
@Component
@RequiredArgsConstructor
public class JwtProvider {

	private final JwtProperties jwtProperties;

    private static final String TYPE_CLAIM = "type";
    private static final String ACCESS_TYPE = "access";
    private static final String SIGNUP_TYPE = "signup";

	// 정식 토큰: memberId
	public String createAccessToken(long memberId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.accessExpiration().toMillis());

        // claim : payload안에 들어있는 정보 한 쌍 "type":"access"
        // claims : claim을 모아둔 것
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(memberId))
                .claim(TYPE_CLAIM, ACCESS_TYPE)
                .issueTime(now)
                .expirationTime(expiry)
                .build();

        // 서명
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        try {
            jwt.sign(new MACSigner(jwtProperties.secret().getBytes(StandardCharsets.UTF_8)));
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
        return jwt.serialize();
	}

	// 임시 토큰: kakaoId, nickname (시드 선택 전)
	public String createSignupToken(String kakaoId, String nickname) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.signupExpiration().toMillis());

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(kakaoId)
                .claim(TYPE_CLAIM, SIGNUP_TYPE)
                .claim("nickname", nickname)
                .issueTime(now)
                .expirationTime(expiry)
                .build();

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        try {
            jwt.sign(new MACSigner(jwtProperties.secret().getBytes(StandardCharsets.UTF_8)));
        } catch (JOSEException e) {
            throw new IllegalStateException(e);
        }
        return jwt.serialize();
	}

	// 검증 실패 시 BusinessException(INVALID_TOKEN)
	public long parseAccessToken(String token) {
        JWTClaimsSet claims = parseClaims(token);
        if (!ACCESS_TYPE.equals(claims.getClaim(TYPE_CLAIM))) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        // 토큰에 이상 없으면 memberid 반환
        return Long.parseLong(claims.getSubject());
	}

    private JWTClaimsSet parseClaims(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            boolean isValid = jwt.verify(new MACVerifier(jwtProperties.secret().getBytes(StandardCharsets.UTF_8)));
            if (!isValid) {
                // business exception은 runtime exception의 자식이라 catch절에서 잡히지 않고 메서드 밖으로 그대로 전파됨
                throw new BusinessException(ErrorCode.INVALID_TOKEN);
            }
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            if (claims.getExpirationTime().before(new Date())) {
                throw new BusinessException(ErrorCode.INVALID_TOKEN);
            }
            return claims;
        } catch (ParseException | JOSEException e) {
            // ParseException : token값을 parse할 수 없을 때
            // JOSEException : 검증과정에서 라이브러리 문제를 만났을 때
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    public SignupClaims parseSignupToken(String token) {

        JWTClaimsSet claims = parseClaims(token);

        if(!SIGNUP_TYPE.equals(claims.getClaim(TYPE_CLAIM))){
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        return new SignupClaims(
                claims.getSubject(),
                String.valueOf(claims.getClaim("nickname")));
	}
}
