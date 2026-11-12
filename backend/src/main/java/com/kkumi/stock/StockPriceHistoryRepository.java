package com.kkumi.stock;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockPriceHistoryRepository extends JpaRepository<StockPriceHistory, Long> {

    // 종목 하나의 since 이후 기록, 오래된 순 (차트 왼쪽 → 오른쪽)
    @Query("""
            select h from StockPriceHistory h
            where h.stockCode = :code and h.recordedAt >= :since
            order by h.recordedAt asc, h.id asc
            """)
    List<StockPriceHistory> findRecent(@Param("code") String code, @Param("since") LocalDateTime since);

    // 이 분의 기록이 이미 있는지 (서버가 멈췄다 풀려 1분 저장이 몰아서 실행될 때 중복 방지)
    boolean existsByRecordedAt(LocalDateTime recordedAt);

    // before 이전 기록 일괄 삭제 (엔티티 로딩 없이 delete 1번), 삭제 행 수 반환
    @Modifying
    @Query("delete from StockPriceHistory h where h.recordedAt < :before")
    int deleteRecordedBefore(@Param("before") LocalDateTime before);
}
