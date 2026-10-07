package com.kkumi;

import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;
import jakarta.persistence.Entity;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

/**
 * CLAUDE.md 백엔드 규칙 자동 검사
 */
@AnalyzeClasses(packages = "com.kkumi", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

	// 계층
	@ArchTest
	static final ArchRule controller_repository_직접참조_금지 = noClasses()
		.that().haveSimpleNameEndingWith("Controller")
		.should().dependOnClassesThat().areAssignableTo(Repository.class)
		.because("Controller → Service → Repository 순서로만 호출")
		.allowEmptyShould(true);

	@ArchTest
	static final ArchRule controller_엔티티_반환_금지 = noMethods()
		.that().areDeclaredInClassesThat().areAnnotatedWith(RestController.class)
		.should().haveRawReturnType(annotatedWith(Entity.class))
		.because("API 응답은 Response DTO로 변환")
		.allowEmptyShould(true);

	@ArchTest
	static final ArchRule global_도메인참조_금지 = noClasses()
		.that().resideInAPackage("com.kkumi.global..")
		.should().dependOnClassesThat().resideInAnyPackage(
			"com.kkumi.member..", "com.kkumi.stock..", "com.kkumi.trade..",
			"com.kkumi.portfolio..", "com.kkumi.apartment..", "com.kkumi.house..")
		.because("global은 config, error, jwt, 공통 응답만");

	// 서비스
	@ArchTest
	static final ArchRule service_클래스_Transactional_필수 = classes()
		.that().areAnnotatedWith(Service.class)
		.should().beAnnotatedWith(Transactional.class)
		.because("클래스에 @Transactional(readOnly = true), 쓰기 메서드만 @Transactional")
		.allowEmptyShould(true);

	@ArchTest
	static final ArchRule jakarta_Transactional_금지 = noClasses()
		.should().dependOnClassesThat().haveFullyQualifiedName("jakarta.transaction.Transactional")
		.because("org.springframework.transaction.annotation.Transactional만 사용");

	// 엔티티
	@ArchTest
	static final ArchRule 엔티티_setter_금지 = methods()
		.that().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
		.should().haveNameNotStartingWith("set")
		.because("상태 변경은 의미 있는 메서드로 (e.g. withdraw)")
		.allowEmptyShould(true);

	@ArchTest
	static final ArchRule 금액_실수타입_금지 = noFields()
		.that().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
		.or().areDeclaredInClassesThat().resideInAPackage("..dto..")
		.should().haveRawType(double.class)
		.orShould().haveRawType(Double.class)
		.orShould().haveRawType(float.class)
		.orShould().haveRawType(Float.class)
		.because("금액은 long, 평균 매입가만 BigDecimal")
		.allowEmptyShould(true);

	// 공통
	@ArchTest
	static final ArchRule 필드주입_금지 = GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;

	@ArchTest
	static final ArchRule System_out_금지 = GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;

	@ArchTest
	static final ArchRule java_util_logging_금지 = GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;
}
