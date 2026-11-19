package com.kkumi.notification;

import com.kkumi.notification.dto.NotificationResponse;
import com.kkumi.notification.dto.UnreadCountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    // 종 드롭다운 목록, 최신순
    @GetMapping
    public List<NotificationResponse> getMyNotifications(@AuthenticationPrincipal Long memberId) {
        return notificationService.findMyNotifications(memberId);
    }

    // 종 배지
    @GetMapping("/unread-count")
    public UnreadCountResponse getUnreadCount(@AuthenticationPrincipal Long memberId) {
        return notificationService.countUnread(memberId);
    }

    // 상태 변경이라 GET 아닌 POST (ADR 031)
    @PostMapping("/{id}/read")
    public void readNotification(@AuthenticationPrincipal Long memberId, @PathVariable Long id) {
        notificationService.readNotification(memberId, id);
    }

    @PostMapping("/read-all")
    public void readAllNotifications(@AuthenticationPrincipal Long memberId) {
        notificationService.readAllNotifications(memberId);
    }

    @DeleteMapping("/{id}")
    public void deleteNotification(@AuthenticationPrincipal Long memberId, @PathVariable Long id) {
        notificationService.deleteNotification(memberId, id);
    }
}
