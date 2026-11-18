package com.kkumi.notification;

import com.kkumi.member.Member;
import com.kkumi.member.MemberRepository;
import com.kkumi.member.SeedMoney;
import com.kkumi.stock.Stock;
import com.kkumi.stock.StockRepository;
import com.kkumi.stock.StockType;
import com.kkumi.stock.dto.StockPriceTickedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

// 하락 알림 판정·저장: 메모리 장부로 판정, 종목당 하루 1번, 30초 넘은 시세 건너뜀 (ADR 059)
@ExtendWith(MockitoExtension.class)
class PriceAlertServiceTest {

    private static final String CODE = "KK0001";
    // KST 2026-11-18 12:00
    private static final Instant NOW = Instant.parse("2026-11-18T03:00:00Z");
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 18);

    @Mock
    private StockRepository stockRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private NotificationRepository notificationRepository;

    private PriceAlertBook book;
    private PriceAlertService service;

    @BeforeEach
    void setUp() {
        // 장부는 실제 객체 (판정 규칙이 여기 있음)
        book = new PriceAlertBook();
        service = new PriceAlertService(book, stockRepository, memberRepository, notificationRepository);
    }

    @Test
    @DisplayName("기준가 이하로 떨어진 보유자만 알림 저장, 받은 목록에 표시")
    void saveOnlyDroppedHolders() {
        // given: 1번 평단 10,000(기준가 9,700), 2번 평단 9,000(기준가 8,730), 현재가 9,600
        book.put(1L, CODE, new BigDecimal("10000"));
        book.put(2L, CODE, new BigDecimal("9000"));
        given(stockRepository.findByCode(CODE)).willReturn(Optional.of(stock()));
        given(memberRepository.getReferenceById(1L)).willReturn(member());

        // when
        List<Notification> saved = service.checkPriceDrop(tick(9_600, NOW), NOW);

        // then
        assertThat(saved).hasSize(1);
        Notification notification = saved.get(0);
        assertThat(notification.getPrice()).isEqualTo(9_600);
        assertThat(notification.getAvgPrice()).isEqualByComparingTo("10000");
        assertThat(notification.getAlertDate()).isEqualTo(TODAY);
        verify(notificationRepository).saveAll(saved);
        assertThat(book.isAlerted(new AlertKey(1L, CODE, TODAY))).isTrue();
        assertThat(book.isAlerted(new AlertKey(2L, CODE, TODAY))).isFalse();
    }

    @Test
    @DisplayName("같은 날 두 번째 하락 시세는 저장 안 함 (중복 메시지 포함)")
    void onceADay() {
        // given
        book.put(1L, CODE, new BigDecimal("10000"));
        given(stockRepository.findByCode(CODE)).willReturn(Optional.of(stock()));
        given(memberRepository.getReferenceById(1L)).willReturn(member());
        service.checkPriceDrop(tick(9_600, NOW), NOW);

        // when
        List<Notification> second = service.checkPriceDrop(tick(9_500, NOW.plusSeconds(5)), NOW.plusSeconds(5));

        // then
        assertThat(second).isEmpty();
        verify(notificationRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("다음 날(KST 자정 지나면) 다시 알림")
    void alertAgainNextDay() {
        // given
        book.put(1L, CODE, new BigDecimal("10000"));
        given(stockRepository.findByCode(CODE)).willReturn(Optional.of(stock()));
        given(memberRepository.getReferenceById(1L)).willReturn(member());
        service.checkPriceDrop(tick(9_600, NOW), NOW);
        // KST 2026-11-19 00:00:01
        Instant nextDay = Instant.parse("2026-11-18T15:00:01Z");

        // when
        List<Notification> saved = service.checkPriceDrop(tick(9_600, nextDay), nextDay);

        // then
        assertThat(saved).extracting(Notification::getAlertDate).containsExactly(TODAY.plusDays(1));
    }

    @Test
    @DisplayName("30초 넘은 시세는 판정 안 함, DB도 안 감")
    void skipStale() {
        // given
        book.put(1L, CODE, new BigDecimal("10000"));
        Instant tickedAt = NOW.minusSeconds(31);

        // when
        List<Notification> saved = service.checkPriceDrop(tick(9_000, tickedAt), NOW);

        // then
        assertThat(saved).isEmpty();
        verify(stockRepository, never()).findByCode(anyString());
        verify(notificationRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("대상이 없으면 DB 안 감")
    void noTargetNoDb() {
        // given
        book.put(1L, CODE, new BigDecimal("10000"));

        // when
        List<Notification> saved = service.checkPriceDrop(tick(9_701, NOW), NOW);

        // then
        assertThat(saved).isEmpty();
        verify(stockRepository, never()).findByCode(anyString());
        verify(memberRepository, never()).getReferenceById(anyLong());
        verify(notificationRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("저장 대상 회원 확인")
    void savedForTargetMember() {
        // given
        book.put(1L, CODE, new BigDecimal("10000"));
        Member member = member();
        given(stockRepository.findByCode(CODE)).willReturn(Optional.of(stock()));
        given(memberRepository.getReferenceById(1L)).willReturn(member);

        // when
        service.checkPriceDrop(tick(9_700, NOW), NOW);

        // then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(Notification::getMember).containsExactly(member);
    }

    private static StockPriceTickedEvent tick(long price, Instant tickedAt) {
        return new StockPriceTickedEvent(CODE, price, tickedAt);
    }

    private static Stock stock() {
        return Stock.of(CODE, "세르반반도체", StockType.ROLLER_COASTER, 51_000L);
    }

    private static Member member() {
        return Member.join("kakao-1", "꿈이", SeedMoney.SMALL);
    }
}
