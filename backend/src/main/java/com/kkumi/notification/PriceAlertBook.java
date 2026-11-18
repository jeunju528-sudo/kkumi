package com.kkumi.notification;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 하락 알림 메모리 장부 (시세 판정에 DB 조회 없음)
 * ① 알림 장부: 종목 → 기준가 순 정렬 → 회원. 매수·매도 커밋 후에만 바뀜
 * ② 받은 목록: (회원, 종목, 날짜). 알림 저장 후 추가
 * 서버 재시작 시 DB에서 다시 채움 (PriceAlertBookLoader)
 */
@Component
public class PriceAlertBook {

    // 종목코드 → 종목별 장부. 종목 단위로만 잠금 → 다른 종목끼리는 안 기다림
    // 종목코드, 갖고있는멤버id, 평균가
    private final Map<String, StockAlertBook> books = new ConcurrentHashMap<>();

    // 오늘 알림을 이미 받은 목록(회원, 종목, 날짜)
    private final Set<AlertKey> alerted = ConcurrentHashMap.newKeySet();

    // 받은 목록을 마지막으로 정리한 날짜
    private volatile LocalDate cleanedDate = LocalDate.MIN;

    // 매수·일부 매도 후 기준가 등록·갱신
    public void put(Long memberId, String stockCode, BigDecimal avgPrice) {
        books.computeIfAbsent(stockCode, code -> new StockAlertBook()).put(memberId, avgPrice);
    }

    // 전량 매도
    public void remove(Long memberId, String stockCode) {
        StockAlertBook book = books.get(stockCode);
        if (book != null) {
            book.remove(memberId);
        }
    }

    // 기준가 ≥ 현재가인 회원 (정렬돼 있어서 해당 구간만 꺼냄)
    public List<AlertTarget> findTargets(String stockCode, long price) {
        StockAlertBook book = books.get(stockCode);
        if (book == null) {
            return List.of();
        }
        return book.findTargets(price);
    }

    public boolean isAlerted(AlertKey key) {
        return alerted.contains(key);
    }

    public void markAlerted(AlertKey key) {
        alerted.add(key);
    }

    // 날짜가 바뀐 뒤 첫 호출에만 지난 날짜 줄 삭제 (메모리 정리)
    public void forgetAlertsBefore(LocalDate today) {
        if (today.equals(cleanedDate)) {
            return;
        }
        alerted.removeIf(key -> key.alertDate().isBefore(today));
        cleanedDate = today;
    }

    // 종목 하나의 장부. 메서드마다 synchronized → 컨슈머 스레드와 거래 스레드가 동시에 고쳐도 안전
    private static final class StockAlertBook {

        private record Entry(long threshold, BigDecimal avgPrice) {
        }

        // 기준가 → 회원들 (같은 기준가 여러 명 가능)
        private final TreeMap<Long, Set<Long>> membersByThreshold = new TreeMap<>();
        // 회원 → 현재 기준가 (갱신·삭제 때 옛 기준가 찾기용)
        private final Map<Long, Entry> entryByMember = new HashMap<>();

        synchronized void put(Long memberId, BigDecimal avgPrice) {
            remove(memberId);
            long threshold = PriceAlertRule.thresholdOf(avgPrice);
            entryByMember.put(memberId, new Entry(threshold, avgPrice));
            membersByThreshold.computeIfAbsent(threshold, t -> new HashSet<>()).add(memberId);
        }

        synchronized void remove(Long memberId) {
            Entry entry = entryByMember.remove(memberId);
            if (entry == null) {
                return;
            }
            Set<Long> members = membersByThreshold.get(entry.threshold());
            members.remove(memberId);
            // 빈 기준가 칸 삭제
            if (members.isEmpty()) {
                membersByThreshold.remove(entry.threshold());
            }
        }

        synchronized List<AlertTarget> findTargets(long price) {
            List<AlertTarget> targets = new ArrayList<>();
            // tailMap(price, true): 기준가 ≥ 현재가 구간만
            for (Set<Long> members : membersByThreshold.tailMap(price, true).values()) {
                for (Long memberId : members) {
                    targets.add(new AlertTarget(memberId, entryByMember.get(memberId).avgPrice()));
                }
            }
            return targets;
        }
    }
}
