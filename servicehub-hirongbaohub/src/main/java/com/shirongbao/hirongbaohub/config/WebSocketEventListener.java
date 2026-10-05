package com.shirongbao.hirongbaohub.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final AtomicInteger onlineCount = new AtomicInteger(0);

    @Autowired
    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        int current = onlineCount.incrementAndGet();
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
}
