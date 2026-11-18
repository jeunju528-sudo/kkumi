package com.kkumi.stock.dto;

import java.time.Instant;

// Instant : 시간대(timezone) 없이 세계 공통 시계로 몇 시 몇 분 몇 초였다는 한 순간을 나타내는 자바 타입
/*
* LocalDate : 날짜만 갖고있음, LocalDate.now(KST) 처럼 서울임을 알려줘야함
* LocalDateTime : 날짜 + 시각(시간대는 없음)
* ZoneId : 시간대 정보 (Asia/Seoul)
* Instant : 세계 공통 한 순간, 서버끼리 주고받는 시각에는 Instant를 많이 씀
* */
public record StockPriceTickedEvent(String code, long price, Instant tickedAt) {
}
