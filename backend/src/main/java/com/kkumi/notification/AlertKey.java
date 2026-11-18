package com.kkumi.notification;

import java.time.LocalDate;

// 받은 목록 한 줄 (회원, 종목, 날짜). 날짜가 키에 있어서 날짜가 바뀌면 자동으로 "안 받음"
public record AlertKey(Long memberId, String stockCode, LocalDate alertDate) {
}
