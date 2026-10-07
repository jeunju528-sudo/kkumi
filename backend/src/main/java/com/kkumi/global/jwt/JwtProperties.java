package com.kkumi.global.jwt;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

// 서명 키와 만료 시간 (application.yaml의 jwt.*)
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret, Duration accessExpiration, Duration signupExpiration) {
}
