package com.eldersphere.config;

import com.eldersphere.security.WebSocketAuthChannelInterceptor;
import com.eldersphere.security.WebSocketJwtHandshakeInterceptor;
import com.eldersphere.security.WebSocketPrincipalHandshakeHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

/**
 * STOMP-over-WebSocket (with SockJS fallback) for real-time messaging and notifications.
 * See {@link WebSocketJwtHandshakeInterceptor} for how the handshake is authenticated and
 * {@link WebSocketAuthChannelInterceptor} for per-topic subscription authorization.
 *
 * Topics:
 *  - /topic/conversations/{conversationId}         — new messages in that conversation
 *  - /topic/conversations/{conversationId}/typing  — typing indicator events for that conversation
 *  - /topic/notifications/{userId}                 — new notifications for that user
 *  - /topic/presence                               — online/offline events for any user
 *
 * Application destinations (client publishes to, under the /app prefix below):
 *  - /app/conversations/{conversationId}/typing — {"typing": true|false}, see ChatWebSocketController
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketJwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final WebSocketAuthChannelInterceptor authChannelInterceptor;

    @Value("${app.frontend.url:http://localhost:3000}")
    private List<String> allowedOrigins;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] origins = allowedOrigins.stream()
                .map(String::trim)
                .map(o -> o.replace("\"", "").replace("'", ""))
                .filter(o -> !o.isBlank())
                .toArray(String[]::new);

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(origins)
                .addInterceptors(jwtHandshakeInterceptor)
                .setHandshakeHandler(new WebSocketPrincipalHandshakeHandler())
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptor);
    }
}
