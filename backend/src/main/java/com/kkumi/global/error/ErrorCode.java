package com.kkumi.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // enum :: 들어갈 수 있는 값이 정해져 있는 경우 그 목록을 type으로 만들어두는 JAVA 문법
    // 사용이유 : 정해진 값 말고 다른 값이 들어오는 경우를 컴파일러가 막아줌
    /*
     * 1. 이름만 있는 경우
     * public enum TradeType {
     *   BUY,
     *   SELL;
     * }
     * 2. 인자가 하나인 경우 -> SeedMoney.SMALL.getAmount();
     * public enum SeedMoney {
     *   SMALL(500000L),
     *   LARGE(500000000L);
     *   private final long amount;
     * }
     * */

    // 상수명(A,B) : A,B enum 생성자에 넘기는 인자, 필드 2개에 순서대로 들어감 status, message
    // @RequiredArgsConstructor 가 생성자 만들어줌

    // 토큰 위조·만료·종류 불일치
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다"),
    ALREADY_JOINED(HttpStatus.CONFLICT, "이미 가입한 회원입니다"),
    INSUFFICIENT_CASH(HttpStatus.BAD_REQUEST, "현금이 부족합니다"),
    INSUFFICIENT_HOLDING(HttpStatus.BAD_REQUEST, "보유 수량이 부족합니다"),
    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "수량은 1주 이상이어야 합니다"),
    // 회원 ID로 조회 실패 (탈퇴·잘못된 토큰 등)
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원을 찾을 수 없습니다"),
    // 요청한 종목 코드 없음
    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "종목을 찾을 수 없습니다");

    private final HttpStatus status;
    private final String message;
}
