package com.kkumi.apartment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AptDealRepository extends JpaRepository<AptDeal, Long> {

    // 거래 1건 + 단지 (계약서 화면)
    @Query("select d from AptDeal d join fetch d.complex where d.id = :id")
    Optional<AptDeal> findWithComplexById(@Param("id") Long id);

    // 살 수 있는 집만, 비싼 순 (최근 6개월·해제 제외, keyword null이면 조건 없음)
    @Query("""
            select d from AptDeal d join fetch d.complex c
            where d.canceled = false
              and d.contractDate >= :since
              and d.price <= :maxPrice
              and (:keyword is null
                   or c.name like concat('%', :keyword, '%')
                   or c.regionName like concat('%', :keyword, '%')
                   or c.dong like concat('%', :keyword, '%'))
            order by d.price desc, d.id desc
            """)
    Slice<AptDeal> findAffordableSlice(@Param("since") LocalDate since, @Param("maxPrice") long maxPrice,
                                       @Param("keyword") String keyword, Pageable pageable);

    // 못 사는 집 포함, 싼 순
    @Query("""
            select d from AptDeal d join fetch d.complex c
            where d.canceled = false
              and d.contractDate >= :since
              and (:keyword is null
                   or c.name like concat('%', :keyword, '%')
                   or c.regionName like concat('%', :keyword, '%')
                   or c.dong like concat('%', :keyword, '%'))
            order by d.price asc, d.id asc
            """)
    Slice<AptDeal> findRecentSlice(@Param("since") LocalDate since, @Param("keyword") String keyword,
                                   Pageable pageable);

    Optional<AptDeal> findByComplexAndAreaAndFloorAndPriceAndContractDate(AptComplex complex, BigDecimal area, int floor, long price, LocalDate contractDate);

}
