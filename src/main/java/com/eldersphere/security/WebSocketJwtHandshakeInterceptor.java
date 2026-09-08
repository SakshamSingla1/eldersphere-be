package com.eldersphere.security;

import com.eldersphere.dao.user.UserDao;
import com.eldersphere.entities.User;
import com.eldersphere.enums.UserTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

/**
 * Validates the JWT on the STOMP/SockJS handshake. Browsers can't attach custom headers to
 * the initial SockJS HTTP handshake request, so the token travels as a query param
 * (?token=...) instead of the Authorization header {@link JwtAuthFilter} reads for normal
 * REST calls (an Authorization header is still honored as a fallback for non-browser
 * clients). Reuses the same {@link JwtUtil} validation logic as JwtAuthFilter rather than
 * duplicating it.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketJwtHandshakeInterceptor implements HandshakeInterceptor {

    public static final String ATTR_USER_ID = "userId";
    public static final String ATTR_EMAIL = "email";
    public static final String ATTR_USER_TYPE = "userType";

    private final JwtUtil jwtUtil;
    private final UserDao userDao;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = extractToken(request);
        if (token == null || token.isBlank()) {
            log.warn("WebSocket handshake rejected: no token supplied");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        String email;
        try {
            email = jwtUtil.extractEmail(token);
        } catch (Exception e) {
            log.warn("WebSocket handshake rejected: unparsable token");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        if (email == null || !jwtUtil.validateToken(token, email)) {
            log.warn("WebSocket handshake rejected: invalid/expired token");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        User user = userDao.findByEmail(email).orElse(null);
        if (user == null) {
            log.warn("WebSocket handshake rejected: user not found for token subject");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        attributes.put(ATTR_USER_ID, user.getId());
        attributes.put(ATTR_EMAIL, user.getEmail());
        attributes.put(ATTR_USER_TYPE,
                user.getUserType() != null ? user.getUserType().name() : UserTypeEnum.FAMILY_MEMBER.name());
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }

    private String extractToken(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            String param = servletRequest.getServletRequest().getParameter("token");
            if (param != null && !param.isBlank()) return param.trim();
        }
        List<String> authHeader = request.getHeaders().get("Authorization");
        if (authHeader != null && !authHeader.isEmpty()) {
            String value = authHeader.get(0);
            if (value != null && value.startsWith("Bearer ")) return value.substring(7).trim();
        }
        return null;
    }
}
