package com.kkumi.apartment.molit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MolitResponseParseTest {

    private static final String ONE_ITEM = """
		{"response":{"header":{"resultCode":"000","resultMsg":"OK"},"body":{"items":{"item":
		{"aptDong":" ","aptNm":"현대뜨레비앙","aptSeq":"11110-102","buildYear":2003,"cdealType":" ",
		"dealAmount":"38,000","dealDay":29,"dealMonth":10,"dealYear":2026,"excluUseAr":36.08,"floor":7,
		"jibun":55,"sggCd":11110,"umdNm":"익선동"}},"numOfRows":1,"pageNo":1,"totalCount":17}}}
		""";

    private static final String TWO_ITEMS = """
		{"response":{"header":{"resultCode":"000","resultMsg":"OK"},"body":{"items":{"item":[
		{"aptNm":"현대뜨레비앙","buildYear":2003,"cdealType":" ","dealAmount":"38,000","dealDay":29,
		"dealMonth":10,"dealYear":2026,"excluUseAr":36.08,"floor":7,"jibun":55,"sggCd":11110,"umdNm":"익선동"},
		{"aptNm":"거성","buildYear":2016,"cdealType":"O","dealAmount":"21,700","dealDay":22,
		"dealMonth":5,"dealYear":2026,"excluUseAr":83.7457,"floor":14,"jibun":"1003-2","sggCd":12110,"umdNm":"상동"}
		]},"numOfRows":2,"pageNo":1,"totalCount":2}}}
		""";

    private static final String EMPTY_MONTH = """
		{"response":{"header":{"resultCode":"000","resultMsg":"OK"},"body":{"items":"","numOfRows":10,"pageNo":1,"totalCount":0}}}
		""";

    private static final String ERROR_XML = """
		<OpenAPI_ServiceResponse><cmmMsgHeader><returnReasonCode>30</returnReasonCode></cmmMsgHeader></OpenAPI_ServiceResponse>
		""";

    private List<MolitDealItem> parse(String json) {
        return MolitClient.parse(json.getBytes(StandardCharsets.UTF_8)).response().body().deals();
    }

    @Test
    @DisplayName("item 하나면 객체로 와도 목록 1건")
    void oneItemAsObject() {
        // when
        List<MolitDealItem> deals = parse(ONE_ITEM);

        // then
        assertThat(deals).hasSize(1);
        assertThat(deals.get(0).toComplexName()).isEqualTo("현대뜨레비앙");
    }

    @Test
    @DisplayName("item 여러 건이면 배열 그대로")
    void itemsAsArray() {
        // when
        List<MolitDealItem> deals = parse(TWO_ITEMS);

        // then
        assertThat(deals).hasSize(2);
        assertThat(deals.get(1).isCanceled()).isTrue();
    }

    @Test
    @DisplayName("숫자로 온 지번·면적·층도 문자열로 받음")
    void numbersAsString() {
        // when
        List<MolitDealItem> deals = parse(TWO_ITEMS);

        // then
        assertThat(deals.get(0).jibun()).isEqualTo("55");
        assertThat(deals.get(0).toFloor()).isEqualTo(7);
        assertThat(deals.get(1).jibun()).isEqualTo("1003-2");
        assertThat(deals.get(1).toArea()).hasToString("83.75");
    }

    @Test
    @DisplayName("거래 없는 달은 빈 목록")
    void emptyMonth() {
        assertThat(parse(EMPTY_MONTH)).isEmpty();
    }

    @Test
    @DisplayName("JSON이 아닌 에러 응답이면 예외")
    void errorXml() {
        assertThatThrownBy(() -> parse(ERROR_XML))
                .isInstanceOf(IllegalStateException.class);
    }
}