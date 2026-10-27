package com.kkumi.stock;


// PriceRules 는 기억할 값이 하나도 없고 계산 함수만 모아 둔 클래스
// 그래서 객체를 만들 필요가 없고, 실수로라도 객체를 만들지 못하게 하려고
// class에 final과 private 생성자를 만들어 둠
public final class PriceRules {
    // 클래스 밖에서 new PriceRules 하는 거 막아줌
    private PriceRules() {
        
    }
    
    // 호가 tick 사이즈
    public static long tickSize(long price) {
        if (price < 1) {
            throw new IllegalArgumentException("가격은 1원 이상: " + price);
        }
        if (price < 2_000) {
            return 1;
        }
        if (price < 5_000) {
            return 5;
        }
        if (price < 20_000) {
            return 10;
        }
        if (price < 50_000) {
            return 50;
        }
        if (price < 200_000) {
            return 100;
        }
        if (price < 500_000) {
            return 500;
        }
        return 1_000;
    }

    // rawPrice: 엔진 내부의 반올림 전 가격 (호가에 안 맞는 값, ADR 042)
    // 호가 단위 값으로 치환해주기 위해 해당 함수 사용
    public static long roundToTick(double rawPrice){
        // 호가 간격을 알아내고
        long tick = tickSize(Math.round(rawPrice)); // tickSize가 long만 받아서 1원 단위로 반올림해 넘김
        // 호가 간격으로 원래 금액을 나눈걸 round 처리후 tick을 곱해주면
        // 호가 단위 금액으로 변경됨
        return Math.round(rawPrice / tick) * tick;
    }

    // 상한가: 오늘 시작가 +30% (호가 단위로 내림)
    public static long upperLimit(long dayOpenPrice){
        long limit = dayOpenPrice * 130 / 100;
        // 호가 단위에 맞추려고 나머지를 자른것
        return limit - limit % tickSize(limit);
    }
    // 하한가: 오늘 시작가 -30% (호가 단위로 올림)
    public static long lowerLimit(long dayOpenPrice){

        // 하한을 내림으로 처리하면 -30% 범위보다 작아질 수도 있기 때문에
        // 하한은 올림으로 처리하는 것
        // double을 사용하지 않고 정수 나눗셈 올림 처리를 하기 위해서
        // 정수 나눗셈은 소숫점을 버리기 때문에 99를 더하면
        // 나머지가 1이라도 있을 때 다음 정수로 올려주는 효과를 줌
        // 100으로 나누니까 100-1 = 99 를 더하는 공식, 정수로 올림하는 방법
        // 70을 곱하는 이유 : 0.7 (-30%) 계산
        long limit = (dayOpenPrice * 70 + 99) / 100;
        long tick = tickSize(limit);
        long remainder = limit % tick;
        // 8730원의 하한 = 6111원, 호가 10원 
        // limit - remainder = 6110 < 하한보다 작음
        // 그래서 1 tick을 올려 하한보다 위로 맞춰줌
        return remainder == 0 ? limit : limit - remainder + tick;
    }
}
