package com.kkumi.stock;

// 가상 종목 변동 유형 (ADR 029, 041)
public enum StockType {
	// 안정형: 변동성 작음
	STABLE,
	// 롤러코스터형: 변동성 큼
	ROLLER_COASTER,
	// 한방형: 평소엔 작고 낮은 확률로 급등락
	JACKPOT
}
