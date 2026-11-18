package com.kkumi.notification;

import com.kkumi.global.error.BusinessException;
import com.kkumi.global.error.ErrorCode;
import com.kkumi.member.MemberRepository;
import com.kkumi.stock.Stock;
import com.kkumi.stock.StockRepository;
import com.kkumi.stock.dto.StockPriceTickedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * 시세 1건 → 하락 알림 판정·저장 (ADR 059)
 * 판정은 메모리 장부, DB는 알림이 생길 때만 (종목 조회 1번 + 알림 저장)
 * 같은 종목은 늘 같은 파티션 → 같은 컨슈머 스레드 → 같은 종목 판정이 동시에 안 돎
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PriceAlertService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final PriceAlertBook priceAlertBook;
    private final StockRepository stockRepository;
    private final MemberRepository memberRepository;
    private final NotificationRepository notificationRepository;

    // 새로 저장한 알림 반환 (토스트 푸시 대상)
    @Transactional
    public List<Notification> checkPriceDrop(StockPriceTickedEvent event, Instant now) {
        // 밀린 메시지로 지난 가격 알림 방지
        if (PriceAlertRule.isStale(event.tickedAt(), now)) {
            log.debug("30초 지난 시세 건너뜀: {} {}", event.code(), event.tickedAt());
            return List.of();
        }

        LocalDate today = LocalDate.ofInstant(now, KST);
        priceAlertBook.forgetAlertsBefore(today);

        // 기준가 ≥ 현재가 중 오늘 안 받은 회원
        List<AlertTarget> targets = priceAlertBook.findTargets(event.code(), event.price()).stream()
                .filter(target -> !priceAlertBook.isAlerted(new AlertKey(target.memberId(), event.code(), today)))
                .toList();
        if (targets.isEmpty()) {
            return List.of();
        }

        Stock stock = stockRepository.findByCode(event.code())
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_NOT_FOUND));

        // getReferenceById: 회원 select 없이 FK용 프록시만
        List<Notification> notifications = targets.stream()
                .map(target -> Notification.ofPriceDrop(
                        memberRepository.getReferenceById(target.memberId()),
                        stock, event.price(), target.avgPrice(), today))
                .toList();
        notificationRepository.saveAll(notifications);

        targets.forEach(target -> priceAlertBook.markAlerted(new AlertKey(target.memberId(), event.code(), today)));
        return notifications;
    }
}
