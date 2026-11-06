package com.kkumi.house;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HouseRepository extends JpaRepository<House, Long> {

    long countByMemberId(Long memberId);

    // 같은 회원의 같은 거래 중복 구매 확인 (다른 회원끼리는 공유, ADR 016)
    boolean existsByMemberIdAndDealId(Long memberId, Long dealId);

    // 내 마을: 산 집 + 거래 + 단지 (join fetch로 N+1 방지), 먼저 산 순
    @Query("""
            select h from House h join fetch h.deal d join fetch d.complex
            where h.member.id = :memberId
            order by h.purchasedAt asc, h.id asc
            """)
    List<House> findAllByMemberIdWithDeal(@Param("memberId") Long memberId);
}
