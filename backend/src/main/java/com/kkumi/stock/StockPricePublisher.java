package com.kkumi.stock;

import com.kkumi.global.config.KafkaTopicConfig;
import com.kkumi.stock.dto.StockPriceTickedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockPricePublisher {
    private final KafkaTemplate<String, StockPriceTickedEvent> kafkaTemplate;
    
    public void publish(List<StockPriceTickedEvent> events){
        try {
            for(StockPriceTickedEvent event : events) {
                // 토픽 발송
                // {토픽명, 키, 값}
                // 같은 키는 매번 같은 파티션으로 가서 가격 순서가 유지됨
                kafkaTemplate.send(KafkaTopicConfig.STOCK_PRICE_TICKED, event.code(), event);
            }
        }
        catch (RuntimeException e){
            log.warn("시세 발행 실패, 이번 tick 건너뜀: {}", e.toString());
        }
    }
}
