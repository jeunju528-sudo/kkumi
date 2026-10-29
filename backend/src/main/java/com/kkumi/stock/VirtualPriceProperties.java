package com.kkumi.stock;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Map;


/*
 * @ConfigurationProperties 이 어노테이션이 붙으면
 * 스프링 부트가 기동하면서
 * yaml 을 읽어 키 목록으로 만들고
 * @ConfigurationPropertiesScan 으로 이 record를 찾아서 스프링 빈에 등록해주고 => backendApplication에 붙어있음
 * record의 파라미터와 application.yaml의 키를 찾아서(느슨한 바인딩)
 * 접두사 + record 파라미터 이름으로 키를 짝지어서 record의 생성자를 호출해서 빈(싱글톤)으로 등록해줌
 * "5s" → Duration, 0.3 -> double
 * */
// 값이 2~3개면 @Value로 가져다 써도 됨 값이 여러개여서 record로 묶어서 씀

/*
= public final class VirtualPriceProperties extends java.lang.Record {
    private final Duration tickInterval;
    ...
    public double ticksPerDay(){
        return Duration.ofDays(1).toMillis() / (double) tickInterval.toMillis();
    }
  }
*/
@ConfigurationProperties(prefix = "virtual-price")
public record VirtualPriceProperties(
        Duration tickInterval,
        double marketWeight,
        double halfLifeDays,
        Jackpot jackpot,
        Map<StockType, Double> dailyVolatility
) {
    // 한방형 급등락
    // 중첩 record도 가능함
    public record Jackpot(double eventsPerDay, double minSize, double maxSize) {

    }

    // 하루 갱신 횟수(하루 / 갱신 주기, 60*60*24 / 5 = 17280)
    public double ticksPerDay() {
        return Duration.ofDays(1).toMillis() / (double) tickInterval.toMillis();
    }

    // 유형별 하루 변동성
    public double volatilityOf(StockType type) {
        return dailyVolatility.get(type);
    }
}

// 가상 시세 변동 설정 (application.yaml의 virtual-price.*와 매핑됨)


