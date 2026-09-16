package com.eldersphere.presence;

import com.eldersphere.security.WebSocketJwtHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class PresenceTracker {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConcurrentHashMap<Long, AtomicInteger> sessionCountsByUser = new ConcurrentHashMap<>();

    @EventListener
    public void onConnect(SessionConnectEvent event) {
        Long userId = resolveUserId(event.getMessage());
        if (userId == null) {
            return;
        }
        AtomicInteger count = sessionCountsByUser.computeIfAbsent(userId, id -> new AtomicInteger(0));
        // Only the first tab/device for a user should flip them online; later tabs just add to the count.
        if (count.incrementAndGet() == 1) {
            broadcast(userId, true);
        }
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        Long userId = resolveUserId(event.getMessage());
        if (userId == null) {
            return;
        }
        AtomicInteger count = sessionCountsByUser.get(userId);
        if (count == null) {
            return;
        }
        // Symmetric to onConnect: only broadcast offline once the user's last open tab/device disconnects.
        if (count.updateAndGet(v -> Math.max(0, v - 1)) == 0) {
            sessionCountsByUser.remove(userId, count);
            broadcast(userId, false);
        }
    }

    public boolean isOnline(Long userId) {
        AtomicInteger count = sessionCountsByUser.get(userId);
        return count != null && count.get() > 0;
    }

    private void broadcast(Long userId, boolean online) {
        messagingTemplate.convertAndSend("/topic/presence", Map.of("userId", userId, "online", online));
    }

    private Long resolveUserId(Message<byte[]> message) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        return sessionAttributes != null ? (Long) sessionAttributes.get(WebSocketJwtHandshakeInterceptor.ATTR_USER_ID) : null;
    }
}
