package com.kkumi.trade;

import com.kkumi.trade.dto.StockProfitResponse;
import com.kkumi.trade.dto.TradeHistorySummaryResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TradeHistoryRepository extends JpaRepository<TradeHistory, Long> {

    // 기간·구분별 거래내역 + 종목 같이 조회 (join fetch로 N+1 방지), from·type null이면 조건 없음
    @Query("""
            select t from TradeHistory t join fetch t.stock
            where t.member.id = :memberId
              and (:from is null or t.tradedAt >= :from)
              and (:type is null or t.tradeType = :type)
            """)
    Slice<TradeHistory> findHistorySlice(@Param("memberId") Long memberId,
                                         @Param("from") LocalDateTime from, @Param("type") TradeType type, Pageable pageable);

    // 기간 합계 (실현손익·매도 원가·매수·매도 금액·건수), 구분 필터 없음
    // coalesce : 기간 안에 거래가 0건이면 sum 결과가 null, record의 long에는 못 넣기 때문에 0으로 바꿔줌
    @Query("""
            select new com.kkumi.trade.dto.TradeHistorySummaryResponse(
              coalesce(sum(t.realizedProfit), 0L),
              coalesce(sum(case when t.tradeType = com.kkumi.trade.TradeType.SELL
                                then t.price * t.quantity - t.realizedProfit else 0L end), 0L),
              coalesce(sum(case when t.tradeType = com.kkumi.trade.TradeType.BUY
                                then t.price * t.quantity else 0L end), 0L),
              coalesce(sum(case when t.tradeType = com.kkumi.trade.TradeType.SELL
                                then t.price * t.quantity else 0L end), 0L),
              count(t))
            from TradeHistory t
            where t.member.id = :memberId
              and (:from is null or t.tradedAt >= :from)
            """)
    TradeHistorySummaryResponse findSummary(@Param("memberId") Long memberId,
                                            @Param("from") LocalDateTime from);

    // 종목별 실현손익 합계, 실현손익은 매도에만 있어서 매도만 집계
    @Query("""
            select new com.kkumi.trade.dto.StockProfitResponse(
              s.code, s.name, sum(t.realizedProfit),
              sum(t.price * t.quantity - t.realizedProfit), count(t))
            from TradeHistory t join t.stock s
            where t.member.id = :memberId
              and t.tradeType = com.kkumi.trade.TradeType.SELL
              and (:from is null or t.tradedAt >= :from)
            group by s.id, s.code, s.name
            order by sum(t.realizedProfit) desc
            """)
    List<StockProfitResponse> findProfitByStock(@Param("memberId") Long memberId,
                                                @Param("from") LocalDateTime from);

}
