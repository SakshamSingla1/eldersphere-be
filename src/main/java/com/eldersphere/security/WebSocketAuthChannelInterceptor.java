package com.eldersphere.security;

import com.eldersphere.dao.messaging.ConversationDao;
import com.eldersphere.entities.Conversation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Guards STOMP SUBSCRIBE frames so a connected client can only subscribe to
 * conversation/notification topics that belong to it (participants of that conversation,
 * or the target user for a notifications topic), with ADMIN/SUPER_ADMIN allowed to observe
 * anything. CONNECT is double-checked here too, though the handshake interceptor already
 * rejects unauthenticated sockets before they get this far.
 *
 * NOTE (documented scope simplification): this authorizes the *topic subscription*, not
 * every individual frame afterwards, and any destination that doesn't match the two known
 * patterns below is left unchecked. The REST endpoints under /api/v1/conversations remain
 * the authoritative, fully access-controlled source for message history.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final Pattern CONVERSATION_TOPIC = Pattern.compile("^/topic/conversations/(\\d+)$");
    private static final Pattern NOTIFICATION_TOPIC = Pattern.compile("^/topic/notifications/(\\d+)$");

    private final ConversationDao conversationDao;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        Long userId = sessionAttributes != null ? (Long) sessionAttributes.get(WebSocketJwtHandshakeInterceptor.ATTR_USER_ID) : null;
        String userType = sessionAttributes != null ? (String) sessionAttributes.get(WebSocketJwtHandshakeInterceptor.ATTR_USER_TYPE) : null;
        boolean isAdmin = "ADMIN".equals(userType) || "SUPER_ADMIN".equals(userType);

        if (StompCommand.CONNECT.equals(accessor.getCommand()) && userId == null) {
            throw new MessagingException("Unauthenticated WebSocket connection");
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            if (userId == null) {
                throw new MessagingException("Unauthenticated subscription attempt");
            }
            String destination = accessor.getDestination();
            if (destination == null) {
                return message;
            }

            Matcher conversationMatcher = CONVERSATION_TOPIC.matcher(destination);
            if (conversationMatcher.matches()) {
                Long conversationId = Long.valueOf(conversationMatcher.group(1));
                Conversation conversation = conversationDao.findById(conversationId, true);
                boolean participant = conversation != null
                        && (userId.equals(conversation.getUserAId()) || userId.equals(conversation.getUserBId()));
                if (!isAdmin && !participant) {
                    log.warn("Rejected WS subscribe to {} by user {}", destination, userId);
                    throw new MessagingException("Not a participant of this conversation");
                }
                return message;
            }

            Matcher notificationMatcher = NOTIFICATION_TOPIC.matcher(destination);
            if (notificationMatcher.matches()) {
                Long topicUserId = Long.valueOf(notificationMatcher.group(1));
                if (!isAdmin && !userId.equals(topicUserId)) {
                    log.warn("Rejected WS subscribe to {} by user {}", destination, userId);
                    throw new MessagingException("Cannot subscribe to another user's notifications");
                }
            }
        }

        return message;
    }
}
