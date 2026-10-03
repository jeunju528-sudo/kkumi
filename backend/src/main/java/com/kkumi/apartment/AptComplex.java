package com.kkumi.apartment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 아파트 단지 마스터 (단지 단위로 값이 같은 컬럼만)
 */
@Entity
@Table(name = "apt_complex",
	uniqueConstraints = @UniqueConstraint(name = "uk_complex",
		columnNames = {"sigungu_code", "dong", "jibun", "name"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AptComplex {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 시군구 코드 (LAWD_CD 5자리) */
	@Column(nullable = false, length = 5)
	private String sigunguCode;

	/** 법정동 */
	@Column(nullable = false, length = 50)
	private String dong;

	@Column(nullable = false, length = 20)
	private String jibun;

	/** 아파트명 */
	@Column(nullable = false, length = 100)
	private String name;

	private Integer builtYear;

	// TODO: 배치 - 단지 조회 or 신규 생성
}
