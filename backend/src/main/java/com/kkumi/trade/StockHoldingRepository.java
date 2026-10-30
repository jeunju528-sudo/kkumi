package com.kkumi.trade;

import com.kkumi.member.Member;
import com.kkumi.stock.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// 보유 주식 관련 Repository
public interface StockHoldingRepository extends JpaRepository<StockHolding, Long> {
    // member랑 stock 으로 보유 주식 검색
    Optional<StockHolding> findByMemberAndStock(Member member, Stock stock);

    // 회원 보유 전체 + 종목 같이 조회 (N+1 방지), 코드순
    @Query("select h from StockHolding h join fetch h.stock where h.member.id = :memberId order by h.stock.code")
    List<StockHolding> findAllByMemberIdWithStock(@Param("memberId") Long memberId);
}
