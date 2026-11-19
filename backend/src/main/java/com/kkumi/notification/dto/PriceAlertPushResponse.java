package com.kkumi.notification.dto;

// /user/queue/alerts 푸시 본문 (토스트 + 종 배지)
public record PriceAlertPushResponse(NotificationResponse notification, long unreadCount) {
}
