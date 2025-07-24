package com.salon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{

    @Override
    public void registerStompEndpoints (StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // 연결할 엔드 포인트
                .setAllowedOriginPatterns("*") // 프론트 도메인 허용(일단 전체허용)
                .withSockJS();  // socJS로 호환성 확보
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // 메세지의 목적지 설정.
        config.setApplicationDestinationPrefixes("/app");   // 메세지를 서버로 보낼때 @MessageMapping메서드로 라우팅
    }

}
