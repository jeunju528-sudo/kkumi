package com.kkumi.portfolio;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PortfolioSnapshotRepository extends JpaRepository<PortfolioSnapshot, Long> {

    // 같은 날 기록 여부 (재실행 시 건너뛰기용)
    boolean existsByMemberIdAndSnapshotDate(Long memberId, LocalDate snapshotDate);

    // 기간 기록, 오래된 날부터 (차트 왼쪽 → 오른쪽)
    @Query("""
            select s from PortfolioSnapshot s
            where s.member.id = :memberId and s.snapshotDate >= :from
            order by s.snapshotDate asc
            """)
    List<PortfolioSnapshot> findAllByMemberIdSince(@Param("memberId") Long memberId, @Param("from") LocalDate from);
}
