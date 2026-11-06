package com.kkumi.portfolio;

import com.kkumi.global.error.BusinessException;
import com.kkumi.portfolio.dto.PortfolioResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// 브라우저 연결과 상관없이 스케쥴러는 계속 돌고
// 브라우저 연결은 접속자 명단에 사람을 추가하는 것 뿐임

@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioPushScheduler {
    // 브로커는 /queue로 시작하는 주소만 배달해서
    // 주소를 /que 로 잘못쓰면 담당자가 없는 주소라서 메세지가 그냥 버려짐 
    private static final String PORTFOLIO_DESTINATION = "/queue/portfolio";
    
    // WebSocket으로 연결된 사용자 명단
    private final SimpUserRegistry simpUserRegistry;
    private final SimpMessagingTemplate messagingTemplate;
    private final PortfolioService portfolioService;

    @Scheduled(fixedRateString = "${virtual-price.tick-interval}")
    public void pushPortfolios() {
        for (SimpUser user : simpUserRegistry.getUsers()) {
            // handshake 때 쿠키 필터가 넣어둔 인증 정보의 이름
            String memberId = user.getName();
            try {
                PortfolioResponse portfolio = portfolioService.getMyPortfolio(Long.valueOf(memberId));
                // 응답 record를 JSON으로 바꿔서 개인 사용자에게 보내줌
                messagingTemplate.convertAndSendToUser(memberId, PORTFOLIO_DESTINATION, portfolio);
            } catch (BusinessException e) {
                log.warn("평가액 푸시 실패: memberId={}, code={}", memberId, e.getErrorCode());
            }
        }
    }
}
