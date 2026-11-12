package com.kkumi.portfolio;

import java.math.BigDecimal;

// 오늘 증감 (어제 마감 순자산 대비, rate 0.0123 = +1.23%, 기준 0이면 null)
public record DailyChange(long amount, BigDecimal rate) {
}
