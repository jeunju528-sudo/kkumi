package com.kkumi.stock;

import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 시세 기록 정리: 매일 1번 24시간 지난 기록 삭제 (차트는 최근 1시간만 사용, ADR 056)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockPriceHistoryScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final long KEEP_HOURS = 24;

    private final StockPriceSnapshotService snapshotService;

    // 새벽 3시 30분 (실거래가 배치 4시와 겹치지 않게)
    @Scheduled(cron = "${stock-price-history.cleanup-cron:0 30 3 * * *}", zone = "Asia/Seoul")
    public void deleteOldHistory() {
        int deleted = snapshotService.deleteHistoryBefore(LocalDateTime.now(KST).minusHours(KEEP_HOURS));
        log.info("시세 기록 정리: {}행 삭제", deleted);
    }
}
