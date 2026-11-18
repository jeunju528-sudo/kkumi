package com.kkumi.notification;

import com.kkumi.global.config.KafkaTopicConfig;
import com.kkumi.stock.dto.StockPriceTickedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
// 컨슈머가 가져온 시세를 받아 판정하는 리스너
public class PriceAlertListener {
    private final PriceAlertService priceAlertService;

    @KafkaListener(topics = KafkaTopicConfig.STOCK_PRICE_TICKED, groupId = "price-alert")
    public void onPriceTicked(StockPriceTickedEvent event) {
        List<Notification> saved = priceAlertService.checkPriceDrop(event, Instant.now());
        if(!saved.isEmpty()){
            log.info("하락 알림 {}건: {}", saved.size(), event.code());
        }

    }

}
