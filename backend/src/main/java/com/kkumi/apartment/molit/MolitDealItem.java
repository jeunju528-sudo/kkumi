package com.kkumi.apartment.molit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

// 레코드 컴포넌트명을 JSON 키와 동일하게 지으면
// @JsonProperty 없이도 자동으로 매핑됨

// @JsonIgnoreProperties : API 통신에서 필요없는 나머지 필드를 무시하라는 뜻
@JsonIgnoreProperties(ignoreUnknown = true)
public record MolitDealItem(
        String sggCd,
        String umdNm,
        String jibun,
        String aptNm,
        String buildYear,
        String excluUseAr,
        String floor,
        String dealAmount,
        String dealYear,
        String dealMonth,
        String dealDay,
        String cdealType
) {
    private static final long MAN_WON = 10_000L;
    private static final String CANCELED = "O";
    private static final int AREA_SCALE = 2;

    public String toSigunguCode() {
        return clean(sggCd);
    }

    public String toDong() {
        return clean(umdNm);
    }

    public String toJibun() {
        return clean(jibun);
    }

    public String toComplexName() {
        return clean(aptNm);
    }

    public Integer toBuiltYear() {
        String value = clean(buildYear);
        if (value.isEmpty()) {
            return null;
        }
        return Integer.parseInt(value);
    }

    public BigDecimal toArea() {
        return new BigDecimal(clean(excluUseAr)).setScale(AREA_SCALE, RoundingMode.HALF_UP);
    }

    public int toFloor() {
        return Integer.parseInt(clean(floor));
    }

    public long toPriceWon() {
        String manWon = clean(dealAmount).replace(",", "");
        try {
            return Math.multiplyExact(Long.parseLong(manWon), MAN_WON);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("거래금액 형식 오류: " + dealAmount, e);
        }
    }

    public LocalDate toContractDate() {
        return LocalDate.of(
                Integer.parseInt(clean(dealYear)),
                Integer.parseInt(clean(dealMonth)),
                Integer.parseInt(clean(dealDay)));
    }

    public boolean isCanceled() {
        return CANCELED.equals(clean(cdealType));
    }

    private static String clean(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

}
