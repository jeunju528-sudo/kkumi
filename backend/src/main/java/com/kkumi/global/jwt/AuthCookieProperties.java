package com.kkumi.global.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

// 인증 쿠키 설정 (application.yaml의 auth.cookie.*)
@ConfigurationProperties(prefix = "auth.cookie")
public record AuthCookieProperties(boolean secure) {
}
