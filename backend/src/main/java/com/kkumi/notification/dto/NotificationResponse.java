package com.kkumi.notification.dto;

import com.kkumi.notification.Notification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// 알림 1건 (알림 목록, 토스트 공용). 필드명은 프론트 types/api.ts와 동일
public record NotificationResponse(Long id, String stockCode, String stockName, BigDecimal dropRate,
                                   boolean isRead, LocalDateTime createdAt) {

    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(notification.getId(),
                notification.getStock().getCode(), notification.getStock().getName(),
                notification.calculateDropRate(), notification.isRead(), notification.getCreatedAt());
    }
}
