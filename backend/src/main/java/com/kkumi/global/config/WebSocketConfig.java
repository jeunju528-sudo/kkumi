package com.kkumi.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// @Configuration도 컴포넌트라 컴포넌트 스캔할 때 스캔됨
// 이 때 @EnableWebSocketMessageBroker가 브로커, SimpMessageTemplate, SimpUserRegistry 같은 부품 빈도 같이 만들어줌
// 이 부품 빈이 스케쥴러에 주임됨

// 웹 소켓은 처음에 http 요청으로 가서 accessToken Cookie를 자동으로 보내주고
// security의 CookieAuthenticationFilter 단계에서 나온 memberId를 스프링이 세션 Principal에 저장하고
// 이 후 STOMP메세지와 사용자별 전송에 사용
// http -> websocket으로 switching되고나서부터는
// websocket은 http메세지가 아닌 프레임 단위로 통신하기 때문에 security를 다시 타지 않음
// 토큰 만료는 재연결 때 다시 검사됨

// WebSocket : 한 번 연결하면 서버가 먼저 클라이언트한테 데이터를 보낼 수 있는 통로
// STOMP : WebSocket 위에서 어느 주소로 보내고 어느 주소를 구독할 지 정하는 규칙
// 메세지 브로커 (Simple Broker) : 구독자 목록을 들고있다가 메세지를 받으면 맞는 구독자한테 보내줌
// /topic : 전체 구독자한테 발송
// /user/queue : 특정 구독자 한명한테 보내줌
@Configuration
@EnableWebSocketMessageBroker // STOMP 메세지 처리 기능과 내장 브로커 ON
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // 서버가 켜질 때 한 번만 실행되어서 /ws 주소는 STOMP 연결 창구라고 등록해두는 설정
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 브라우저가 처음 연결(handshake)하는 주소
        // nginx location /ws 가 이 주소로 넘김
        registry.addEndpoint("/ws");
    }

    // 서버가 켜질 때 한 번만 실행되어서 /queue로 시작하는 주소는 메모리 브로커가 배달한다고 등록해줌
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // /queue로 시작하는 주소는 메모리 브로커가 구독자에게 나눠줌
        // /user 접두사는 Spring기본값이라 따로 안씀
        // 서버는 convertAndSendToUser(memberId, "/queue/portfolio") 로 보내면
        // 구독자는 /user/queue/portfolio 로 받음
        registry.enableSimpleBroker("/queue");
    }
}
