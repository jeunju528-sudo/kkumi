package com.kkumi.notification;

import com.kkumi.trade.dto.HoldingChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 매수·매도 커밋 후 알림 장부 갱신
 * AFTER_COMMIT: 롤백된 거래는 장부에 안 들어감
 * 서버 1대라 서버 안 이벤트. 서버가 늘면 Kafka(키 = 종목코드)로 바꿔 같은 파티션 컨슈머가 갱신
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HoldingChangedListener {

    private final PriceAlertBook priceAlertBook;
    // TradeService buy, sell에서 publishEvent → 커밋 후 여기로 옴 (롤백되면 안 옴)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(HoldingChangedEvent event) {
        
        if (event.isSoldOut()) {
            priceAlertBook.remove(event.memberId(), event.stockCode());
            log.info("알림 장부 삭제: 회원 {}, {}", event.memberId(), event.stockCode());
            return;
        }
        priceAlertBook.put(event.memberId(), event.stockCode(), event.avgPrice());
        log.info("알림 장부 갱신: 회원 {}, {}, 평단 {}", event.memberId(), event.stockCode(), event.avgPrice());
    }
}
