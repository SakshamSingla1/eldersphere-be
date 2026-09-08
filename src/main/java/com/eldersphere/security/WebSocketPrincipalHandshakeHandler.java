package com.eldersphere.security;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * Assigns the STOMP session a {@link Principal} derived from the userId/email that
 * {@link WebSocketJwtHandshakeInterceptor} already validated and stashed in the handshake
 * attributes, so authenticated per-session identity is available downstream (e.g. for
 * future convertAndSendToUser use) without a second auth lookup.
 */
public class WebSocketPrincipalHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                       Map<String, Object> attributes) {
        Object email = attributes.get(WebSocketJwtHandshakeInterceptor.ATTR_EMAIL);
        Object userId = attributes.get(WebSocketJwtHandshakeInterceptor.ATTR_USER_ID);
        if (email == null || userId == null) {
            return super.determineUser(request, wsHandler, attributes);
        }
        String name = userId + ":" + email;
        return () -> name;
    }
}
