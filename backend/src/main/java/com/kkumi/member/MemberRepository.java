package com.kkumi.member;

import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

	Optional<Member> findByKakaoId(String kakaoId);

    boolean existsByKakaoId(String kakaoId);

    // 비관적 락
    @Lock(LockModeType.PESSIMISTIC_WRITE) // 조회 쿼리 끝에 for update를 붙임
    @Query("select m from Member m where m.id = :id")
    Optional<Member> findByIdForUpdate(@Param("id") Long id);
}
