package com.kkumi.apartment.molit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "external.molit")
public record MolitProperties(
        String baseUrl,
        String serviceKey
) {
}
