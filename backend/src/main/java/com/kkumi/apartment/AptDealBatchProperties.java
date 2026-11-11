package com.kkumi.apartment;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "apt-deal-batch")
public record AptDealBatchProperties(
        boolean enabled,
        boolean forceInitial
) {

}