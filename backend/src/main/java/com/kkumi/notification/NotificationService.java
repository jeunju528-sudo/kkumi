package com.kkumi.notification;

import com.kkumi.global.error.BusinessException;
import com.kkumi.global.error.ErrorCode;
import com.kkumi.notification.dto.NotificationResponse;
import com.kkumi.notification.dto.UnreadCountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 알림함: 목록, 안 읽은 수, 읽음, 삭제, 7일 보관 (ADR 059, 063)
 * 읽음·삭제는 내 알림만. 남의 알림 id는 없는 알림과 같은 404
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    /** 보관 날짜 수 (오늘 포함) */
    static final int KEEP_DAYS = 7;

    private final NotificationRepository notificationRepository;

    public List<NotificationResponse> findMyNotifications(Long memberId) {
        return notificationRepository.findAllByMemberIdWithStock(memberId).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    public UnreadCountResponse countUnread(Long memberId) {
        return new UnreadCountResponse(notificationRepository.countByMemberIdAndReadAtIsNull(memberId));
    }

    // 줄 클릭 시 그 1건만 읽음 (ADR 063). 시각은 createdAt과 같은 기준
    @Transactional
    public void readNotification(Long memberId, Long notificationId) {
        getMyNotification(memberId, notificationId).markAsRead(LocalDateTime.now());
    }

    // 모두 읽음 버튼, 변경 행 수 반환
    @Transactional
    public int readAllNotifications(Long memberId) {
        return notificationRepository.markAllAsRead(memberId, LocalDateTime.now());
    }

    @Transactional
    public void deleteNotification(Long memberId, Long notificationId) {
        notificationRepository.delete(getMyNotification(memberId, notificationId));
    }

    // 오늘 포함 7일치만 남김 (오늘 − 6일보다 이전 날짜 삭제), 삭제 행 수 반환
    @Transactional
    public int deleteExpired(LocalDate today) {
        return notificationRepository.deleteAlertedBefore(today.minusDays(KEEP_DAYS - 1));
    }

    private Notification getMyNotification(Long memberId, Long notificationId) {
        return notificationRepository.findByIdAndMemberId(notificationId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
    }
}
