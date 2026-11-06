package com.kkumi.apartment;

// 집값 40% 내 돈 + 60% 가짜 대출 (도메인 규칙). 추천·집 사기 공통
public final class HousePriceRule {

    private static final long OWN_PERCENT = 40;
    private static final long PERCENT_BASE = 100;

    private HousePriceRule() {
    }

    // 내 돈 = 집값 × 40%, 원 미만 올림 (ADR 047)
    public static long ownAmount(long price) {
        validatePrice(price);
        return Math.ceilDiv(Math.multiplyExact(price, OWN_PERCENT), PERCENT_BASE);
    }

    // 가짜 대출 = 집값 − 내 돈
    public static long loanAmount(long price) {
        return price - ownAmount(price);
    }

    // 평가액으로 살 수 있는 최대 집값: 집값 × 40 ≤ 평가액 × 100 (ownAmount 올림과 같은 경계)
    public static long maxAffordablePrice(long totalAsset) {
        if (totalAsset <= 0) {
            return 0;
        }
        return Math.multiplyExact(totalAsset, PERCENT_BASE) / OWN_PERCENT;
    }

    private static void validatePrice(long price) {
        if (price < 1) {
            throw new IllegalArgumentException("집값은 1원 이상: " + price);
        }
    }
}
