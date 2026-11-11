package com.kkumi.apartment;

import com.kkumi.apartment.molit.MolitClient;
import com.kkumi.apartment.molit.MolitDealItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "apt-deal-batch.enabled", havingValue = "true", matchIfMissing = true)
public class AptDealBatchScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int DAILY_MONTHS = 2;
    private static final int INITIAL_MONTHS = AptDeal.MARKET_PRICE_MONTHS + 1;

    private final AptDealBatchProperties properties;
    private final MolitClient molitClient;
    private final AptDealBatchService batchService;
    private final SigunguCodes sigunguCodes;
    // AtomicBoolean : 여러 스레드가 동시에 읽고 바꿔도 안전한 boolean
    // compareAndSet(false, true) : 지금 값이 false면 true로 바꾸고 true를 반환
    // 아니면 아무것도 안하고 false를 반환 , 이거를 쪼갤 수 없는 한 동작으로 함
    // DB의 카운트로는 저장 중인지, 저장이 완료된 값인지를 알 수 없어서 별도 표시가 필요함
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    // 거래가 비어 있거나 강제 재실행이면 첫 적재 (별도 스레드)
    @EventListener(ApplicationReadyEvent.class)
    public void collectInitialIfEmpty() {
        if (properties.forceInitial() || batchService.hasNoDeals()) {
            // 우리가 직접 만드는 스레드, 첫 적재 7개월치 수행
            // main 스레드의 ApplicationReadyEvent Listener에서 시작되는데
            // 같은 이벤트를 VirtualPriceProvider도 받아서 종목 시세를 메모리에 올림
            // 리스너는 main에서 하나씩 차례로 실행되는데 순서가 정해져있지않아서
            // 첫 적재 때 걸리는 시간만큼 시세 로딩이 뒤로 밀려 시세가 비어있을까봐
            // 스레드를 별도로 만들어서 일만 넘기고 바로 다음 리스너로 넘어간 것
            Thread.ofPlatform().name("apt-deal-initial").start(() -> collect(INITIAL_MONTHS));
        }
    }

    // 매일 새벽 4시(KST) 이번 달 + 지난 달
    @Scheduled(cron = "${apt-deal-batch.cron:0 0 4 * * *}", zone = "Asia/Seoul")
    public void collectDaily() {
        collect(batchService.hasNoDeals() ? INITIAL_MONTHS : DAILY_MONTHS);
    }

    private void collect(int monthCount) {
        if(!isRunning.compareAndSet(false, true)){
            log.warn("실거래가 수집 이미 실행 중, 건너뜀");
            return;
        }
        try {
            log.info("실거래가 수집 시작: {}개월, 시군구 {}개", monthCount, sigunguCodes.getAllCodes().size());

            YearMonth thisMonth = YearMonth.now(KST);
            int inserted = 0;
            int updated = 0;
            int skipped = 0;
            int failed = 0;
            for(String code : sigunguCodes.getAllCodes()) {
                for(int i=0; i<monthCount; i++){
                    YearMonth month = thisMonth.minusMonths(i);
                    try {
                        List<MolitDealItem> items = molitClient.fetchDeals(code, month);
                        DealSaveResult result = batchService.saveDeals(items);
                        inserted += result.inserted();
                        updated += result.updated();
                        skipped += result.skipped();
                    } catch (RuntimeException e) {
                        failed++;
                        log.warn("실거래가 수집 실패: {} {}, reason={}", code, month, e.getMessage());
                    }
                }
            }
            log.info("실거래가 수집: {}개월, 신규 {}건, 해제 갱신 {}건, 건너뜀 {}건, 실패 {}단위",
                    monthCount, inserted, updated, skipped, failed);
        } finally {
            isRunning.set(false);
        }
    }

}
