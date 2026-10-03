# 테스트 작성 규칙 예시

올바른 패턴을 따르고 금지 패턴은 쓰지 않음.

규칙 목록은 CLAUDE.md "테스트" 참고.

## 테스트: 도메인 규칙 → 테스트
```java
// house/TransformStageTest.java — 변신 단계: 0채 걸뱅이 / 1채 서민 / 2~4채 졸부 / 5채 이상 갑부
class TransformStageTest {

	@Test
	@DisplayName("집이 없으면 걸뱅이")
	void zeroHouse() {
		// given
		int houseCount = 0;

		// when
		TransformStage stage = TransformStage.of(houseCount);

		// then
		assertThat(stage).isEqualTo(TransformStage.BEGGAR);
	}

	@ParameterizedTest
	@ValueSource(ints = {2, 3, 4})
	@DisplayName("2~4채면 졸부")
	void nouveauRiche(int houseCount) {
		assertThat(TransformStage.of(houseCount)).isEqualTo(TransformStage.NOUVEAU_RICHE);
	}

	@Test
	@DisplayName("집 수가 음수면 예외")
	void negativeCount() {
		assertThatThrownBy(() -> TransformStage.of(-1))
			.isInstanceOf(IllegalArgumentException.class);
	}
}
```
