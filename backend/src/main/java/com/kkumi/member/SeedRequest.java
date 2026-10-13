package com.kkumi.member;

// 시드 선택 요청 본문: {"seedMoney": "SMALL"} 또는 "LARGE"
// recode 에는 파라미터 적는 부분에다가 변수를 적으면 됨
public record SeedRequest(SeedMoney seedMoney) {
}
