package com.kkumi.notification;

import com.kkumi.notification.dto.NotificationResponse;
import com.kkumi.notification.dto.PriceAlertPushResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PriceAlertPusher {
    private static final String ALERT_DESTINATION = "/queue/alerts";

    private final SimpUserRegistry simpUserRegistry;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    
    // PriceAlertListener에서 호출하는 함수
    public void push(List<Notification> notificationList) {
        for(Notification notification : notificationList) {
            Long memberId = notification.getMember().getId();
            String userName = String.valueOf(memberId);
            // 접속하지 않은 회원한테는 토스트 푸시 안해줌
            if(simpUserRegistry.getUser(userName) == null){
                continue;
            }
            try {
                long unreadCount = notificationRepository.countByMemberIdAndReadAtIsNull(memberId);
                PriceAlertPushResponse response = new PriceAlertPushResponse(NotificationResponse.from(notification), unreadCount);
                messagingTemplate.convertAndSendToUser(userName, ALERT_DESTINATION, response);
            } catch (RuntimeException e) {
                // 예외를 삼키는 이유는 던지면 kafka가 재시도를 하는데 장부에 이미 받음으로 기록되어있어서 다시 해도 소용이 없음
                // 알람은 DB에 남아있음
                log.warn("하락 알림 푸시 실패: memberId={}", memberId, e);
            }
        }
    }
}
