package com.eldersphere.controllers;

import com.eldersphere.security.WebSocketJwtHandshakeInterceptor;
import com.eldersphere.services.MessagingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final MessagingService messagingService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/conversations/{conversationId}/typing")
    public void typing(@DestinationVariable Long conversationId, @Payload Map<String, Object> payload,
                        SimpMessageHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        Long senderId = sessionAttributes != null
                ? (Long) sessionAttributes.get(WebSocketJwtHandshakeInterceptor.ATTR_USER_ID)
                : null;
        if (senderId == null) {
            return;
        }
        if (!messagingService.hasAccess(senderId, conversationId)) {
            log.warn("Rejected typing event on conversation {} from user {}", conversationId, senderId);
            return;
        }

        boolean typing = Boolean.TRUE.equals(payload.get("typing"));
        messagingTemplate.convertAndSend("/topic/conversations/" + conversationId + "/typing",
                Map.of("userId", senderId, "typing", typing));
    }
}
