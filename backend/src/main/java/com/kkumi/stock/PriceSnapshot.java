package com.kkumi.stock;

import java.time.LocalDate;

// 1분 주기 DB 저장용 시세
public record PriceSnapshot(long currentPrice, long dayOpenPrice, LocalDate priceDate) {
}
