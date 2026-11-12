package com.kkumi.stock;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockDailyCandleRepository extends JpaRepository<StockDailyCandle, Long> {

    // 1분 저장 때 오늘 일봉 한 번에 조회 (종목마다 select 안 하려고)
    List<StockDailyCandle> findByTradeDateIn(Collection<LocalDate> tradeDates);

    // 종목 하나의 since 이후 일봉, 오래된 순
    @Query("""
            select c from StockDailyCandle c
            where c.stockCode = :code and c.tradeDate >= :since
            order by c.tradeDate asc
            """)
    List<StockDailyCandle> findRecent(@Param("code") String code, @Param("since") LocalDate since);
}
