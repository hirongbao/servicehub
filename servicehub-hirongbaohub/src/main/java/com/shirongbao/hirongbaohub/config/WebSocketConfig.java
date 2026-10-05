package com.shirongbao.hirongbaohub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final AtomicInteger onlineCount = new AtomicInteger(0);

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        int current = onlineCount.incrementAndGet();
        // Since we scale to baseline (10-30), let's just add a base number
        messagingTemplate.convertAndSend("/topic/online-count", current + 12);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        int current = onlineCount.decrementAndGet();
        if (current < 0) {
            onlineCount.set(0);
            current = 0;
        }
        messagingTemplate.convertAndSend("/topic/online-count", current + 12);
    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/notify")
                .setAllowedOriginPatterns("*")
                .withSockJS();
                
        // For online count
        registry.addEndpoint("/ws/online")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
