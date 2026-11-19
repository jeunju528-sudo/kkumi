package com.kkumi.notification;

import com.kkumi.global.error.BusinessException;
import com.kkumi.global.error.ErrorCode;
import com.kkumi.member.Member;
import com.kkumi.member.MemberRepository;
import com.kkumi.member.SeedMoney;
import com.kkumi.notification.dto.NotificationResponse;
import com.kkumi.stock.Stock;
import com.kkumi.stock.StockRepository;
import com.kkumi.stock.StockType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// 알림함: 내 알림만 읽음·삭제, 모두 읽음, 7일 보관 (ADR 063)
@SpringBootTest
@Transactional
class NotificationServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 19);

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private StockRepository stockRepository;

    private Member me;
    private Member other;
    private Stock stockA;
    private Stock stockB;

    @BeforeEach
    void setUp() {
        me = memberRepository.save(Member.join("kakao-me", "나", SeedMoney.SMALL));
        other = memberRepository.save(Member.join("kakao-other", "남", SeedMoney.SMALL));
        // 서버 시작 때 등록되는 KK 종목과 겹치지 않는 코드
        stockA = stockRepository.save(Stock.of("TT0001", "테스트A", StockType.STABLE, 10_000L));
        stockB = stockRepository.save(Stock.of("TT0002", "테스트B", StockType.STABLE, 10_000L));
    }

    private Notification saveAlert(Member member, Stock stock, LocalDate alertDate) {
        return notificationRepository.save(
                Notification.ofPriceDrop(member, stock, 9_000L, new BigDecimal("10000"), alertDate));
    }

    @Test
    @DisplayName("목록은 내 알림만 최신순")
    void findMyNotifications_onlyMineNewestFirst() {
        // given
        Notification older = saveAlert(me, stockA, TODAY);
        Notification newer = saveAlert(me, stockB, TODAY);
        saveAlert(other, stockA, TODAY);

        // when
        List<NotificationResponse> result = notificationService.findMyNotifications(me.getId());

        // then
        assertThat(result).extracting(NotificationResponse::id)
                .containsExactly(newer.getId(), older.getId());
    }

    @Test
    @DisplayName("내 알림 1건 읽음 → 안 읽은 수 1 감소")
    void readNotification_marksOnlyThatOne() {
        // given
        Notification target = saveAlert(me, stockA, TODAY);
        saveAlert(me, stockB, TODAY);

        // when
        notificationService.readNotification(me.getId(), target.getId());

        // then
        assertThat(target.isRead()).isTrue();
        assertThat(notificationService.countUnread(me.getId()).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("남의 알림 읽음은 404, 그 알림은 안 읽음 그대로")
    void readNotification_failsForOthers() {
        // given
        Notification othersAlert = saveAlert(other, stockA, TODAY);

        // when, then
        assertThatThrownBy(() -> notificationService.readNotification(me.getId(), othersAlert.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
        assertThat(othersAlert.isRead()).isFalse();
    }

    @Test
    @DisplayName("남의 알림 삭제는 404, 행 그대로")
    void deleteNotification_failsForOthers() {
        // given
        Notification othersAlert = saveAlert(other, stockA, TODAY);

        // when, then
        assertThatThrownBy(() -> notificationService.deleteNotification(me.getId(), othersAlert.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
        assertThat(notificationRepository.existsById(othersAlert.getId())).isTrue();
    }

    @Test
    @DisplayName("내 알림 삭제")
    void deleteNotification_deletesMine() {
        // given
        Notification mine = saveAlert(me, stockA, TODAY);

        // when
        notificationService.deleteNotification(me.getId(), mine.getId());

        // then
        assertThat(notificationRepository.existsById(mine.getId())).isFalse();
    }

    @Test
    @DisplayName("모두 읽음은 내 안 읽은 알림만 바꾸고 이미 읽은 알림·남의 알림은 그대로")
    void readAllNotifications_onlyMyUnread() {
        // given: 내 알림 3건 중 1건은 이미 읽음
        Notification alreadyRead = saveAlert(me, stockA, TODAY);
        saveAlert(me, stockB, TODAY);
        saveAlert(me, stockA, TODAY.minusDays(1));
        saveAlert(other, stockA, TODAY);
        notificationService.readNotification(me.getId(), alreadyRead.getId());

        // when
        int changed = notificationService.readAllNotifications(me.getId());

        // then: readAt null 조건이라 이미 읽은 1건은 대상 아님
        assertThat(changed).isEqualTo(2);
        assertThat(notificationService.countUnread(me.getId()).count()).isZero();
        assertThat(notificationService.countUnread(other.getId()).count()).isEqualTo(1);
    }

    @Test
    @DisplayName("7일 보관 경계: 오늘 − 6일은 남고 오늘 − 7일은 삭제")
    void deleteExpired_keepsSevenDays() {
        // given
        Notification today = saveAlert(me, stockA, TODAY);
        Notification lastKept = saveAlert(me, stockB, TODAY.minusDays(6));
        Notification expired = saveAlert(me, stockA, TODAY.minusDays(7));

        // when
        int deleted = notificationService.deleteExpired(TODAY);

        // then
        assertThat(deleted).isEqualTo(1);
        assertThat(notificationRepository.existsById(today.getId())).isTrue();
        assertThat(notificationRepository.existsById(lastKept.getId())).isTrue();
        assertThat(notificationRepository.existsById(expired.getId())).isFalse();
    }
}
