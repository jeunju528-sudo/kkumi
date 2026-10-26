package com.kkumi.trade;

import com.kkumi.member.Member;
import com.kkumi.stock.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 보유 주식 관련 Repository
public interface StockHoldingRepository extends JpaRepository<StockHolding, Long> {
    // member랑 stock 으로 보유 주식 검색
    Optional<StockHolding> findByMemberAndStock(Member member, Stock stock);
}
