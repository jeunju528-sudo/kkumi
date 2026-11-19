package com.kkumi.global.config;

import com.kkumi.global.error.BusinessException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaErrorConfig {
    private static final long RETRY_INTERVAL_MS = 1000L;
    private static final long MAX_RETRIES = 2L;
    // SpringBoot가 CommonErrorHandler 빈이 하나뿐이면 자동으로 리스너 컨테이너에 붙임
    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaOperations<?,?> kafkaOperations) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaOperations);
        // FixedBackOff(1000,2) : 처음 1번 + 재시도 2번 해서 총 3번 실행
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(RETRY_INTERVAL_MS, MAX_RETRIES));
        // BusinessException은 재시도 하지 않도록 설정
        handler.addNotRetryableExceptions(BusinessException.class);
        return handler;
    }
}
