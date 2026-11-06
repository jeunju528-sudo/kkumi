package com.kkumi.apartment;

// 카드 상태 + 부족액 (NEED_SELL: 현금 부족액, SHORT: 평가액 부족액, OK: 0)
public record AffordabilityCheck(Affordability affordability, long shortage) {

    // 추천은 평가액, 결제는 현금 기준
    public static AffordabilityCheck of(long ownAmount, long cash, long totalAsset) {
        if (ownAmount <= cash) {
            return new AffordabilityCheck(Affordability.OK, 0);
        }
        if (ownAmount <= totalAsset) {
            return new AffordabilityCheck(Affordability.NEED_SELL, ownAmount - cash);
        }
        return new AffordabilityCheck(Affordability.SHORT, ownAmount - totalAsset);
    }
}
