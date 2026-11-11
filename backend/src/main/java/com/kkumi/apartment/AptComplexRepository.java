package com.kkumi.apartment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AptComplexRepository extends JpaRepository<AptComplex, Long> {
    Optional<AptComplex> findBySigunguCodeAndDongAndJibunAndName(String sigunguCode, String dong, String jibun, String name);
}
