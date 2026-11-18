package com.kkumi.notification;

import com.kkumi.trade.StockHoldingRepository;
import com.kkumi.trade.dto.HoldingAvgPrice;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * 서버 시작 시 알림 장부 채우기
 * @PostConstruct: 컨슈머·웹 서버가 뜨기 전에 실행 → 빈 장부로 판정하거나 로드 중 거래가 섞이지 않음
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PriceAlertBookLoader {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final StockHoldingRepository stockHoldingRepository;
    private final NotificationRepository notificationRepository;
    private final PriceAlertBook priceAlertBook;

    @PostConstruct
    void load() {
        List<HoldingAvgPrice> holdings = stockHoldingRepository.findAllAvgPrices();
        holdings.forEach(h -> priceAlertBook.put(h.memberId(), h.stockCode(), h.avgPrice()));

        // 오늘 이미 보낸 알림 → 재시작 후 같은 날 중복 알림 방지
        List<AlertKey> alertedToday = notificationRepository.findAlertKeysByAlertDate(LocalDate.now(KST));
        alertedToday.forEach(priceAlertBook::markAlerted);

        log.info("알림 장부 로드: 보유 {}건, 오늘 받은 알림 {}건", holdings.size(), alertedToday.size());
    }
}
