package com.kkumi.portfolio;

import com.kkumi.global.error.BusinessException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioSnapshotScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final PortfolioSnapshotService snapshotService;

    // 매일 0시(KST), 0시 값 = 어제 마감 값 → 어제 날짜로 기록
    // zone 필수: EC2 컨테이너 기본 시간대가 UTC면 오전 9시에 실행됨
    // cron은 로컬 확인용으로 바꿀 수 있게 설정값 (기본 매일 0시)
    @Scheduled(cron = "${portfolio-snapshot.cron:0 0 0 * * *}", zone = "Asia/Seoul")
    public void recordDailySnapshots() {
        LocalDate yesterday = LocalDate.now(KST).minusDays(1);
        List<Long> memberIds = snapshotService.findAllMemberIds();
        int savedCount = 0;
        for (Long memberId : memberIds) {
            try {
                if (snapshotService.recordSnapshot(memberId, yesterday)) {
                    savedCount++;
                }
            } catch (BusinessException | DataIntegrityViolationException e) {
                // 회원 없음, 동시 실행으로 UK 충돌 → 이 회원만 건너뜀
                log.warn("평가액 기록 실패: memberId={}, reason={}", memberId, e.getMessage());
            }
        }
        log.info("평가액 기록: {}, {}명 저장 / 전체 {}명", yesterday, savedCount, memberIds.size());
    }
}
