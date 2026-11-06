package com.kkumi.house;

// 변신 4단계: 보유 집 수로 결정 (걸뱅이 0채 / 서민 1채 / 졸부 2~4채 / 갑부 5채 이상)
public enum Tier {
    GEOLBAENGI,
    SEOMIN,
    JOKBU,
    GAPBU;

    private static final long JOKBU_MAX = 4;

    public static Tier of(long houseCount) {
        if (houseCount < 0) {
            throw new IllegalArgumentException("집 수는 0 이상: " + houseCount);
        }
        if (houseCount == 0) {
            return GEOLBAENGI;
        }
        if (houseCount == 1) {
            return SEOMIN;
        }
        if (houseCount <= JOKBU_MAX) {
            return JOKBU;
        }
        return GAPBU;
    }
}
