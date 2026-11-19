package com.kkumi.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 알림 정리: 매일 1번 7일 지난 알림 삭제 (알림 목록 "7일 지나면 사라져요")
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationCleanupScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final NotificationService notificationService;

    // 새벽 3시 (시세 기록 정리 3:30, 실거래가 배치 4시와 겹치지 않게)
    @Scheduled(cron = "${notification.cleanup-cron:0 0 3 * * *}", zone = "Asia/Seoul")
    public void deleteExpiredNotifications() {
        int deleted = notificationService.deleteExpired(LocalDate.now(KST));
        log.info("알림 정리: {}행 삭제", deleted);
    }
}
