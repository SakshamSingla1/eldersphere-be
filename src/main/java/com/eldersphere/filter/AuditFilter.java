package com.eldersphere.filter;

import com.eldersphere.security.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuditFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            preProcessRequest(request);
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String userId = jwtUtil.extractUserId(token);
                    if (userId != null) {
                        request.setAttribute("currentUserId", userId);
                    }
                } catch (Exception ignored) {
                    // Malformed/expired token — leave audit context empty, JwtAuthFilter/security
                    // layer is responsible for rejecting the request itself.
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            postProcessRequest();
        }
    }

    private void preProcessRequest(HttpServletRequest request) {
        String existingTraceId = (String) request.getAttribute("traceId");
        if (existingTraceId == null || existingTraceId.isBlank()) {
            request.setAttribute("traceId", UUID.randomUUID().toString());
        }
    }

    private void postProcessRequest() {
        // Reserved for future audit-context cleanup.
    }
}
