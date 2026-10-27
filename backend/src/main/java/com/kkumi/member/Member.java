package com.kkumi.member;

import com.kkumi.global.error.BusinessException;
import com.kkumi.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 카카오 소셜로그인 키
     */
    @Column(nullable = false, unique = true, length = 64)
    private String kakaoId;

    @Column(nullable = false, length = 30)
    private String nickname;

    /**
     * 초기 자금 (500만 / 5,000만 원)
     */
    @Column(nullable = false)
    private long initialCash;

    /**
     * 현재 현금 잔고 (원)
     */
    @Column(nullable = false)
    private long cash;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Member(String kakaoId, String nickname, SeedMoney seedMoney) {
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        this.initialCash = seedMoney.getAmount();
        this.cash = seedMoney.getAmount();
        this.createdAt = LocalDateTime.now();
    }

    // 인스턴스 필드 : 객체마다 따로 갖는 값, static 필드는 모든 객체가 하나의 값을 공유함
    // 정적 팩토리 메서드 : 생성자 대신 객체를 만들어 돌려주는 static 메서드
    // 정적 팩토리 메서드 쓰는 이유 : 생성자를 private로 숨겨서 정해진 방법으로만 객체를 만들게 하기 위함
    // 그리고 join과 같은 의미있는 이름을 붙일 수도 있고 상황에 따라 다른 객체를 반환할 수도 있음

    // 객체가 아직 없을 때 부르는 메서드는 static으로 만듦
    // static 메서드에서는 this나 인스턴스 필드를 못 쓰는데 그 이유는 객체없이 호출되는 메서드여서
    // 어떤 객체의 값인지를 모르기 때문에 쓸 수 없음
    public static Member join(String kakaoId, String nickname, SeedMoney seedMoney) {
        return new Member(kakaoId, nickname, seedMoney);
    }

    // 이미 있는 특정 객체의 값을 다룰 때는 static X
    // 값 인출
    public void withdraw(long amount) {
        if (this.cash < amount) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_CASH);
        }
        this.cash -= amount;
    }
    
    // 값 입금
    public void deposit(long amount) {
        this.cash += amount;
    }
}
